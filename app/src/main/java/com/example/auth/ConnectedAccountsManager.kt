package com.example.auth

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.gateway.GoogleDriveSyncManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AccountAuthSource(val label: String) {
    GOOGLE_CREDENTIAL_MANAGER("Google OAuth Verified"),
    FIREBASE_AUTH("Firebase + Google Verified"),
    LOCAL_FALLBACK("Local Workspace Profile")
}

/**
 * Model representing the user's connected account identity across Google and Creative services.
 */
data class ConnectedUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isGoogleConnected: Boolean = true,
    val isDriveConnected: Boolean = true,
    val isYouTubeConnected: Boolean = true,
    val cloudSyncStatus: CloudSyncStatus = CloudSyncStatus.SYNCHRONIZED,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val authSource: AccountAuthSource = AccountAuthSource.LOCAL_FALLBACK,
    val googleSubjectId: String? = null,
    val isFirebaseLinked: Boolean = false
)

enum class CloudSyncStatus(val label: String) {
    SYNCHRONIZED("Synchronized"),
    SYNCING("Syncing to Cloud..."),
    OFFLINE("Offline Mode"),
    ERROR("Sync Error")
}

/**
 * ConnectedAccountsManager:
 * Provides a unified Google Credential Manager OAuth Sign-In and linked accounts hub.
 * Bridges AndroidX Credential Manager, Firebase Auth, Google Workspace identity, and cloud storage sync.
 */
class ConnectedAccountsManager internal constructor(
    context: Context,
    private val credentialAuthProvider: GoogleCredentialAuthProvider =
        AndroidGoogleCredentialAuthProvider(context.applicationContext),
    private val firebaseAuthBridge: FirebaseAuthBridge =
        DefaultFirebaseAuthBridge(context.applicationContext),
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    prefsName: String = DEFAULT_PREFS_NAME
) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<ConnectedUser?>(loadSavedUser())
    val currentUser: StateFlow<ConnectedUser?> = _currentUser.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, System.currentTimeMillis()))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _oauthClientId = MutableStateFlow(resolveEffectiveOAuthClientId())
    val oauthClientId: StateFlow<String> = _oauthClientId.asStateFlow()

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage.asStateFlow()

    init {
        try {
            firebaseAuthBridge.addAuthStateListener { fbSession ->
                if (fbSession != null && !prefs.getBoolean(KEY_IS_SIGNED_OUT, false)) {
                    updateUserFromFirebaseSession(fbSession)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error attaching auth state listener: ${e.message}")
        }
    }

    private fun loadSavedUser(): ConnectedUser? {
        if (prefs.getBoolean(KEY_IS_SIGNED_OUT, false)) return null
        val savedUid = prefs.getString(KEY_UID, null)?.takeIf { it.isNotBlank() } ?: return null
        val email = prefs.getString(KEY_EMAIL, DEFAULT_FALLBACK_EMAIL) ?: DEFAULT_FALLBACK_EMAIL
        val name = prefs.getString(KEY_NAME, DEFAULT_FALLBACK_NAME) ?: DEFAULT_FALLBACK_NAME
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)?.takeIf { it.isNotBlank() }
        val isGoogleConnected = prefs.getBoolean(KEY_GOOGLE_LINKED, false)
        val storedSourceName = prefs.getString(KEY_AUTH_SOURCE, null)
        val authSource = storedSourceName?.let { raw ->
            AccountAuthSource.entries.firstOrNull { it.name == raw }
        } ?: if (isGoogleConnected) AccountAuthSource.GOOGLE_CREDENTIAL_MANAGER else AccountAuthSource.LOCAL_FALLBACK

        return ConnectedUser(
            uid = savedUid,
            email = email,
            displayName = name,
            avatarUrl = avatarUrl,
            isGoogleConnected = isGoogleConnected,
            isDriveConnected = prefs.getBoolean(KEY_DRIVE_LINKED, true),
            isYouTubeConnected = prefs.getBoolean(KEY_YOUTUBE_LINKED, true),
            cloudSyncStatus = CloudSyncStatus.SYNCHRONIZED,
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, System.currentTimeMillis()),
            authSource = authSource,
            googleSubjectId = prefs.getString(KEY_GOOGLE_SUB, null)?.takeIf { it.isNotBlank() },
            isFirebaseLinked = prefs.getBoolean(KEY_FIREBASE_LINKED, false)
        )
    }

    private fun updateUserFromFirebaseSession(session: FirebaseLinkedSession) {
        val existing = _currentUser.value
        val driveLinked = existing?.isDriveConnected ?: prefs.getBoolean(KEY_DRIVE_LINKED, true)
        val youtubeLinked = existing?.isYouTubeConnected ?: prefs.getBoolean(KEY_YOUTUBE_LINKED, true)
        val googleSub = existing?.googleSubjectId ?: prefs.getString(KEY_GOOGLE_SUB, null)

        val updated = ConnectedUser(
            uid = session.uid,
            email = session.email?.takeIf { it.isNotBlank() } ?: existing?.email ?: DEFAULT_FALLBACK_EMAIL,
            displayName = session.displayName?.takeIf { it.isNotBlank() } ?: existing?.displayName ?: DEFAULT_FALLBACK_NAME,
            avatarUrl = session.avatarUrl?.takeIf { it.isNotBlank() } ?: existing?.avatarUrl,
            isGoogleConnected = true,
            isDriveConnected = driveLinked,
            isYouTubeConnected = youtubeLinked,
            cloudSyncStatus = existing?.cloudSyncStatus ?: CloudSyncStatus.SYNCHRONIZED,
            lastSyncTimestamp = existing?.lastSyncTimestamp ?: prefs.getLong(KEY_LAST_SYNC, System.currentTimeMillis()),
            authSource = AccountAuthSource.FIREBASE_AUTH,
            googleSubjectId = googleSub,
            isFirebaseLinked = true
        )
        saveUser(updated)
        _currentUser.value = updated
    }

    fun validateOAuthClientId(rawClientId: String): String? {
        val trimmed = rawClientId.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("AIza")) {
            return "This looks like a Gemini/Google Cloud API key (AIza...), not an OAuth 2.0 Web Client ID. Enter a client ID ending in .apps.googleusercontent.com."
        }
        if (!OAUTH_CLIENT_ID_REGEX.matches(trimmed)) {
            return "Invalid OAuth 2.0 Web Client ID format. Expected <client-id>.apps.googleusercontent.com."
        }
        return null
    }

    fun saveCustomOAuthClientId(rawClientId: String): Result<String> {
        val trimmed = rawClientId.trim()
        val validationError = validateOAuthClientId(trimmed)
        if (validationError != null) {
            _authStatusMessage.value = validationError
            return Result.failure(IllegalArgumentException(validationError))
        }

        return if (trimmed.isEmpty()) {
            prefs.edit().remove(KEY_OAUTH_CLIENT_ID).apply()
            val effective = resolveEffectiveOAuthClientId()
            _oauthClientId.value = effective
            _authStatusMessage.value = "Cleared custom OAuth 2.0 Web Client ID."
            Result.success(effective)
        } else {
            prefs.edit().putString(KEY_OAUTH_CLIENT_ID, trimmed).apply()
            _oauthClientId.value = trimmed
            _authStatusMessage.value = "OAuth 2.0 Web Client ID saved and ready for Google Sign-In."
            Result.success(trimmed)
        }
    }

    fun resolveEffectiveOAuthClientId(): String {
        val prefClientId = prefs.getString(KEY_OAUTH_CLIENT_ID, null)?.trim()
        if (!prefClientId.isNullOrEmpty() && validateOAuthClientId(prefClientId) == null) {
            return prefClientId
        }

        for (fieldName in listOf("GOOGLE_OAUTH_CLIENT_ID", "GOOGLE_WEB_CLIENT_ID")) {
            try {
                val field = BuildConfig::class.java.getDeclaredField(fieldName)
                val raw = (field.get(null) as? String)?.trim().orEmpty()
                if (raw.isNotEmpty() &&
                    !raw.startsWith("MY_") &&
                    !raw.startsWith("YOUR_") &&
                    validateOAuthClientId(raw) == null
                ) {
                    return raw
                }
            } catch (_: NoSuchFieldException) {
                // Optional BuildConfig secret field not present
            } catch (_: Throwable) {
                // Ignore reflection error
            }
        }

        val dynamicResId = appContext.resources.getIdentifier(
            "default_web_client_id",
            "string",
            appContext.packageName
        )
        if (dynamicResId != 0) {
            val fromGoogleServices = runCatching { appContext.getString(dynamicResId).trim() }.getOrDefault("")
            if (fromGoogleServices.isNotEmpty() && validateOAuthClientId(fromGoogleServices) == null) {
                return fromGoogleServices
            }
        }

        val fromStringsXml = runCatching {
            appContext.getString(R.string.google_oauth_client_id).trim()
        }.getOrDefault("")
        if (fromStringsXml.isNotEmpty() && validateOAuthClientId(fromStringsXml) == null) {
            return fromStringsXml
        }

        return ""
    }

    fun isOAuthClientConfigured(): Boolean = resolveEffectiveOAuthClientId().isNotEmpty()

    fun signInWithGoogle(
        customEmail: String = DEFAULT_FALLBACK_EMAIL,
        customName: String = DEFAULT_FALLBACK_NAME,
        onSuccess: (ConnectedUser) -> Unit,
        onError: (String) -> Unit
    ) = signInWithGoogle(
        activityContext = null,
        customEmail = customEmail,
        customName = customName,
        allowLocalFallback = true,
        onSuccess = onSuccess,
        onError = onError
    )

    fun signInWithGoogle(
        activityContext: Context?,
        customEmail: String = DEFAULT_FALLBACK_EMAIL,
        customName: String = DEFAULT_FALLBACK_NAME,
        allowLocalFallback: Boolean = true,
        onSuccess: (ConnectedUser) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            val result = signInWithGoogleSuspend(
                activityContext = activityContext,
                customEmail = customEmail,
                customName = customName,
                allowLocalFallback = allowLocalFallback
            )
            result.fold(
                onSuccess = onSuccess,
                onFailure = { err -> onError(err.message ?: "Google sign-in failed.") }
            )
        }
    }

    suspend fun signInWithGoogleSuspend(
        activityContext: Context?,
        customEmail: String = DEFAULT_FALLBACK_EMAIL,
        customName: String = DEFAULT_FALLBACK_NAME,
        allowLocalFallback: Boolean = true
    ): Result<ConnectedUser> {
        _isSigningIn.value = true
        return try {
            val clientId = resolveEffectiveOAuthClientId()
            _oauthClientId.value = clientId

            if (clientId.isEmpty()) {
                val message = "No OAuth 2.0 Web Client ID configured — connected with Local Workspace Profile. Configure a *.apps.googleusercontent.com client ID for live Google OAuth."
                _authStatusMessage.value = message
                return if (allowLocalFallback) {
                    Result.success(
                        connectLocalFallbackProfile(
                            customEmail = customEmail,
                            customName = customName,
                            reasonBanner = message
                        )
                    )
                } else {
                    Result.failure(IllegalStateException(message))
                }
            }

            when (val outcome = credentialAuthProvider.signIn(activityContext, clientId)) {
                is GoogleCredentialOutcome.Success -> {
                    val verified = outcome.credential
                    prefs.edit().putBoolean(KEY_IS_SIGNED_OUT, false).apply()
                    val fbSession = firebaseAuthBridge.signInWithGoogleIdToken(verified.idToken)
                    val driveLinked = _currentUser.value?.isDriveConnected ?: prefs.getBoolean(KEY_DRIVE_LINKED, true)
                    val youtubeLinked = _currentUser.value?.isYouTubeConnected ?: prefs.getBoolean(KEY_YOUTUBE_LINKED, true)

                    val user = ConnectedUser(
                        uid = fbSession?.uid ?: verified.subjectId,
                        email = fbSession?.email?.takeIf { it.isNotBlank() } ?: verified.email,
                        displayName = fbSession?.displayName?.takeIf { it.isNotBlank() } ?: verified.displayName,
                        avatarUrl = fbSession?.avatarUrl?.takeIf { it.isNotBlank() } ?: verified.avatarUrl,
                        isGoogleConnected = true,
                        isDriveConnected = driveLinked,
                        isYouTubeConnected = youtubeLinked,
                        cloudSyncStatus = CloudSyncStatus.SYNCHRONIZED,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        authSource = if (fbSession != null) {
                            AccountAuthSource.FIREBASE_AUTH
                        } else {
                            AccountAuthSource.GOOGLE_CREDENTIAL_MANAGER
                        },
                        googleSubjectId = verified.subjectId,
                        isFirebaseLinked = fbSession != null
                    )
                    saveUser(user)
                    _currentUser.value = user
                    _authStatusMessage.value = if (fbSession != null) {
                        "Signed in with Google OAuth & linked Firebase session (${user.email})."
                    } else {
                        "Signed in with verified Google OAuth ID token (${user.email})."
                    }
                    Result.success(user)
                }

                is GoogleCredentialOutcome.ProviderUnavailable -> {
                    handleProviderOrAccountUnavailable(
                        reason = outcome.reason,
                        customEmail = customEmail,
                        customName = customName,
                        allowLocalFallback = allowLocalFallback
                    )
                }

                is GoogleCredentialOutcome.NoCredentialsAvailable -> {
                    handleProviderOrAccountUnavailable(
                        reason = outcome.reason,
                        customEmail = customEmail,
                        customName = customName,
                        allowLocalFallback = allowLocalFallback
                    )
                }

                is GoogleCredentialOutcome.Cancelled -> {
                    _authStatusMessage.value = outcome.message
                    Result.failure(IllegalStateException(outcome.message))
                }

                is GoogleCredentialOutcome.InvalidCredential -> {
                    _authStatusMessage.value = outcome.reason
                    Result.failure(IllegalStateException(outcome.reason))
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Failed sign in: ${e.message}", e)
            val msg = e.message ?: "Unknown sign in error"
            _authStatusMessage.value = msg
            Result.failure(e)
        } finally {
            _isSigningIn.value = false
        }
    }

    private fun handleProviderOrAccountUnavailable(
        reason: String,
        customEmail: String,
        customName: String,
        allowLocalFallback: Boolean
    ): Result<ConnectedUser> {
        return if (allowLocalFallback) {
            val message = "Credential Manager unavailable ($reason) — using Local Workspace Profile fallback."
            _authStatusMessage.value = message
            Result.success(
                connectLocalFallbackProfile(
                    customEmail = customEmail,
                    customName = customName,
                    reasonBanner = message
                )
            )
        } else {
            val errMsg = "Credential Manager unavailable: $reason"
            _authStatusMessage.value = errMsg
            Result.failure(IllegalStateException(errMsg))
        }
    }

    fun connectLocalFallbackProfile(
        customEmail: String = DEFAULT_FALLBACK_EMAIL,
        customName: String = DEFAULT_FALLBACK_NAME,
        reasonBanner: String? = null,
        onSuccess: (ConnectedUser) -> Unit = {}
    ): ConnectedUser {
        val fbUser = firebaseAuthBridge.currentUser
        val driveLinked = _currentUser.value?.isDriveConnected ?: prefs.getBoolean(KEY_DRIVE_LINKED, true)
        val youtubeLinked = _currentUser.value?.isYouTubeConnected ?: prefs.getBoolean(KEY_YOUTUBE_LINKED, true)
        val user = ConnectedUser(
            uid = fbUser?.uid ?: "local_user_${System.currentTimeMillis() % 100000}",
            email = fbUser?.email?.takeIf { it.isNotBlank() } ?: customEmail,
            displayName = fbUser?.displayName?.takeIf { it.isNotBlank() } ?: customName,
            avatarUrl = fbUser?.avatarUrl,
            isGoogleConnected = false,
            isDriveConnected = driveLinked,
            isYouTubeConnected = youtubeLinked,
            cloudSyncStatus = CloudSyncStatus.SYNCHRONIZED,
            lastSyncTimestamp = System.currentTimeMillis(),
            authSource = AccountAuthSource.LOCAL_FALLBACK,
            googleSubjectId = null,
            isFirebaseLinked = fbUser != null
        )
        saveUser(user)
        _currentUser.value = user
        if (reasonBanner != null) {
            _authStatusMessage.value = reasonBanner
        }
        onSuccess(user)
        return user
    }

    fun signOut(onComplete: () -> Unit = {}) {
        firebaseAuthBridge.signOut()
        clearSessionPreferencesPreservingOAuthConfig()
        _currentUser.value = null
        _oauthClientId.value = resolveEffectiveOAuthClientId()
        _authStatusMessage.value = "Signed out of workspace account."

        scope.launch {
            try {
                credentialAuthProvider.clearCredentialState()
            } finally {
                onComplete()
            }
        }
    }

    suspend fun signOutSuspend() {
        firebaseAuthBridge.signOut()
        clearSessionPreferencesPreservingOAuthConfig()
        _currentUser.value = null
        _oauthClientId.value = resolveEffectiveOAuthClientId()
        _authStatusMessage.value = "Signed out of workspace account."
        credentialAuthProvider.clearCredentialState()
    }

    private fun clearSessionPreferencesPreservingOAuthConfig() {
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_OUT, true)
            .remove(KEY_UID)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_AVATAR_URL)
            .remove(KEY_GOOGLE_LINKED)
            .remove(KEY_DRIVE_LINKED)
            .remove(KEY_YOUTUBE_LINKED)
            .remove(KEY_LAST_SYNC)
            .remove(KEY_AUTH_SOURCE)
            .remove(KEY_GOOGLE_SUB)
            .remove(KEY_FIREBASE_LINKED)
            .apply()
    }

    fun toggleServiceLink(service: String, isLinked: Boolean) {
        val current = _currentUser.value ?: return
        val updated = when (service.lowercase()) {
            "drive" -> current.copy(isDriveConnected = isLinked)
            "youtube" -> current.copy(isYouTubeConnected = isLinked)
            else -> current
        }
        saveUser(updated)
        _currentUser.value = updated
    }

    fun triggerCloudSync(onComplete: (Boolean, String) -> Unit) {
        _isSyncing.value = true
        _currentUser.value = _currentUser.value?.copy(cloudSyncStatus = CloudSyncStatus.SYNCING)

        GoogleDriveSyncManager.performSyncAll(
            context = appContext,
            scope = scope
        ) { success, message ->
            val now = System.currentTimeMillis()
            _isSyncing.value = false
            _lastSyncTime.value = now
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()

            _currentUser.value = _currentUser.value?.copy(
                cloudSyncStatus = if (success) CloudSyncStatus.SYNCHRONIZED else CloudSyncStatus.ERROR,
                lastSyncTimestamp = now
            )
            onComplete(
                success,
                if (success) {
                    "All workspace notes, frequency waveforms, and creative studio items backed up!"
                } else {
                    message
                }
            )
        }
    }

    private fun saveUser(user: ConnectedUser) {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_SIGNED_OUT, false)
            .putString(KEY_UID, user.uid)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_NAME, user.displayName)
            .putBoolean(KEY_GOOGLE_LINKED, user.isGoogleConnected)
            .putBoolean(KEY_DRIVE_LINKED, user.isDriveConnected)
            .putBoolean(KEY_YOUTUBE_LINKED, user.isYouTubeConnected)
            .putLong(KEY_LAST_SYNC, user.lastSyncTimestamp)
            .putString(KEY_AUTH_SOURCE, user.authSource.name)
            .putBoolean(KEY_FIREBASE_LINKED, user.isFirebaseLinked)

        if (!user.avatarUrl.isNullOrBlank()) {
            editor.putString(KEY_AVATAR_URL, user.avatarUrl)
        } else {
            editor.remove(KEY_AVATAR_URL)
        }

        if (!user.googleSubjectId.isNullOrBlank()) {
            editor.putString(KEY_GOOGLE_SUB, user.googleSubjectId)
        } else {
            editor.remove(KEY_GOOGLE_SUB)
        }

        editor.apply()
    }

    companion object {
        private const val TAG = "ConnectedAccountsManager"
        internal const val DEFAULT_PREFS_NAME = "connected_accounts_prefs"
        private const val DEFAULT_FALLBACK_EMAIL = "operator@athena.local"
        private const val DEFAULT_FALLBACK_NAME = "ATHENA Researcher"

        private const val KEY_IS_SIGNED_OUT = "user_signed_out"
        private const val KEY_UID = "user_uid"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR_URL = "user_avatar_url"
        private const val KEY_GOOGLE_LINKED = "google_linked"
        private const val KEY_DRIVE_LINKED = "drive_linked"
        private const val KEY_YOUTUBE_LINKED = "youtube_linked"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_AUTH_SOURCE = "user_auth_source"
        private const val KEY_GOOGLE_SUB = "user_google_sub"
        private const val KEY_FIREBASE_LINKED = "user_firebase_linked"
        internal const val KEY_OAUTH_CLIENT_ID = "oauth_web_client_id"

        private val OAUTH_CLIENT_ID_REGEX =
            Regex("^[A-Za-z0-9][A-Za-z0-9._-]*\\.apps\\.googleusercontent\\.com$")

        @Volatile
        private var INSTANCE: ConnectedAccountsManager? = null

        fun getInstance(context: Context): ConnectedAccountsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConnectedAccountsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
