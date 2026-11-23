package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class VerifiedGoogleCredential(
    val idToken: String,
    val subjectId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?
)

sealed interface GoogleCredentialOutcome {
    data class Success(val credential: VerifiedGoogleCredential) : GoogleCredentialOutcome
    data class Cancelled(val message: String = "Google sign-in cancelled by user.") : GoogleCredentialOutcome
    data class ProviderUnavailable(val reason: String) : GoogleCredentialOutcome
    data class NoCredentialsAvailable(val reason: String) : GoogleCredentialOutcome
    data class InvalidCredential(val reason: String) : GoogleCredentialOutcome
}

interface GoogleCredentialAuthProvider {
    suspend fun signIn(activityContext: Context?, webClientId: String): GoogleCredentialOutcome
    suspend fun clearCredentialState()
}

data class FirebaseLinkedSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?
)

interface FirebaseAuthBridge {
    val currentUser: FirebaseLinkedSession?
    fun addAuthStateListener(onUserChanged: (FirebaseLinkedSession?) -> Unit)
    suspend fun signInWithGoogleIdToken(idToken: String): FirebaseLinkedSession?
    fun signOut()
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext?.findActivity()
    else -> null
}

class AndroidGoogleCredentialAuthProvider(
    context: Context,
    private val credentialManagerFactory: (Context) -> CredentialManager = { CredentialManager.create(it) },
    private val nonceGenerator: () -> String = { GoogleIdTokenValidator.generateNonce() },
    private val clockEpochSeconds: () -> Long = { System.currentTimeMillis() / 1000L }
) : GoogleCredentialAuthProvider {

    private val appContext = context.applicationContext

    override suspend fun signIn(
        activityContext: Context?,
        webClientId: String
    ): GoogleCredentialOutcome {
        val trimmedClientId = webClientId.trim()
        if (trimmedClientId.isEmpty()) {
            return GoogleCredentialOutcome.ProviderUnavailable("OAuth 2.0 Web Client ID is empty.")
        }

        val activity = activityContext?.findActivity()
            ?: return GoogleCredentialOutcome.ProviderUnavailable(
                "An active Activity window is required for Google Credential Manager sign-in."
            )

        val credentialManager = try {
            credentialManagerFactory(activity)
        } catch (e: Exception) {
            return GoogleCredentialOutcome.ProviderUnavailable(
                e.message ?: "Unable to initialize AndroidX CredentialManager."
            )
        }

        val nonce = nonceGenerator()

        return try {
            val response = requestGoogleCredentialWithRetry(
                credentialManager = credentialManager,
                activity = activity,
                webClientId = trimmedClientId,
                nonce = nonce
            )
            extractVerifiedGoogleCredential(
                response = response,
                expectedClientId = trimmedClientId,
                expectedNonce = nonce
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: GetCredentialCancellationException) {
            GoogleCredentialOutcome.Cancelled(
                e.message?.takeIf { it.isNotBlank() } ?: "Google sign-in cancelled by user."
            )
        } catch (e: GetCredentialProviderConfigurationException) {
            GoogleCredentialOutcome.ProviderUnavailable(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Google Credential Manager provider is not configured on this device."
            )
        } catch (e: GetCredentialUnsupportedException) {
            GoogleCredentialOutcome.ProviderUnavailable(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Credential Manager is unsupported on this device."
            )
        } catch (e: NoCredentialException) {
            GoogleCredentialOutcome.NoCredentialsAvailable(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "No Google accounts available on this device."
            )
        } catch (e: GoogleIdTokenParsingException) {
            GoogleCredentialOutcome.InvalidCredential(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Failed to parse Google ID token credential bundle."
            )
        } catch (e: GoogleIdTokenValidationException) {
            GoogleCredentialOutcome.InvalidCredential(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Google ID token claim validation failed."
            )
        } catch (e: GetCredentialException) {
            GoogleCredentialOutcome.ProviderUnavailable(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Credential Manager error (${e.type})."
            )
        } catch (e: Exception) {
            GoogleCredentialOutcome.InvalidCredential(
                e.message?.takeIf { it.isNotBlank() }
                    ?: "Unexpected Google sign-in error."
            )
        }
    }

    private suspend fun requestGoogleCredentialWithRetry(
        credentialManager: CredentialManager,
        activity: Activity,
        webClientId: String,
        nonce: String
    ): GetCredentialResponse {
        val authorizedOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .setNonce(nonce)
            .build()
        val stage1Request = GetCredentialRequest.Builder()
            .addCredentialOption(authorizedOption)
            .build()

        return try {
            credentialManager.getCredential(activity, stage1Request)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: NoCredentialException) {
            val allAccountsOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()
            val stage2Request = GetCredentialRequest.Builder()
                .addCredentialOption(allAccountsOption)
                .build()

            try {
                credentialManager.getCredential(activity, stage2Request)
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: NoCredentialException) {
                val siwgOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .setNonce(nonce)
                    .build()
                val stage3Request = GetCredentialRequest.Builder()
                    .addCredentialOption(siwgOption)
                    .build()
                credentialManager.getCredential(activity, stage3Request)
            }
        }
    }

    private fun extractVerifiedGoogleCredential(
        response: GetCredentialResponse,
        expectedClientId: String,
        expectedNonce: String
    ): GoogleCredentialOutcome {
        val credential = response.credential
        if (credential is CustomCredential &&
            (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL)
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            val claims = GoogleIdTokenValidator.parseAndValidate(
                idToken = googleCred.idToken,
                expectedClientId = expectedClientId,
                expectedNonce = expectedNonce,
                nowEpochSeconds = clockEpochSeconds()
            )
            val resolvedEmail = claims.email?.takeIf { it.isNotBlank() }
                ?: googleCred.id.takeIf { it.isNotBlank() }
                ?: "${claims.subject}@google.oauth"
            val resolvedName = googleCred.displayName?.takeIf { it.isNotBlank() }
                ?: claims.displayName?.takeIf { it.isNotBlank() }
                ?: resolvedEmail.substringBefore('@')
            val resolvedAvatar = googleCred.profilePictureUri?.toString()?.takeIf { it.isNotBlank() }
                ?: claims.pictureUrl

            return GoogleCredentialOutcome.Success(
                VerifiedGoogleCredential(
                    idToken = googleCred.idToken,
                    subjectId = claims.subject,
                    email = resolvedEmail,
                    displayName = resolvedName,
                    avatarUrl = resolvedAvatar
                )
            )
        }
        return GoogleCredentialOutcome.InvalidCredential(
            "Unsupported credential type returned: ${credential.type}"
        )
    }

    override suspend fun clearCredentialState() {
        try {
            withTimeoutOrNull(3_000L) {
                credentialManagerFactory(appContext).clearCredentialState(ClearCredentialStateRequest())
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "clearCredentialState warning: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "GoogleCredAuthProvider"
    }
}

class DefaultFirebaseAuthBridge(context: Context) : FirebaseAuthBridge {
    private val appContext = context.applicationContext

    private fun obtainFirebaseAuthOrNull(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                null
            } else {
                FirebaseAuth.getInstance()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    private fun FirebaseUser.toLinkedSession(): FirebaseLinkedSession = FirebaseLinkedSession(
        uid = uid,
        email = email,
        displayName = displayName,
        avatarUrl = photoUrl?.toString()
    )

    override val currentUser: FirebaseLinkedSession?
        get() = obtainFirebaseAuthOrNull()?.currentUser?.toLinkedSession()

    override fun addAuthStateListener(onUserChanged: (FirebaseLinkedSession?) -> Unit) {
        try {
            obtainFirebaseAuthOrNull()?.addAuthStateListener { firebaseAuth ->
                onUserChanged(firebaseAuth.currentUser?.toLinkedSession())
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to attach Firebase AuthStateListener: ${e.message}")
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): FirebaseLinkedSession? {
        val auth = obtainFirebaseAuthOrNull() ?: return null
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.toLinkedSession()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase Google credential linking skipped/failed: ${e.message}")
            null
        }
    }

    override fun signOut() {
        try {
            obtainFirebaseAuthOrNull()?.signOut()
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase signOut warning: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "DefaultFirebaseAuthBridge"
    }
}
