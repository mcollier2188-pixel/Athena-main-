package com.example.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.CancellationSignal
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreateCredentialRequest
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PrepareGetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.ConnectedAccountSheet
import com.example.ui.theme.MyApplicationTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID
import java.util.concurrent.Executor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConnectedAccountsManagerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var appContext: Context
    private lateinit var prefsName: String

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        prefsName = "test_connected_accounts_${UUID.randomUUID()}"
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun buildSyntheticJwt(claims: Map<String, Any?>): String {
        val header = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode("""{"alg":"RS256","typ":"JWT"}""".toByteArray(Charsets.UTF_8))
        val json = JSONObject()
        claims.forEach { (k, v) ->
            if (v != null) json.put(k, v)
        }
        val payload = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(json.toString().toByteArray(Charsets.UTF_8))
        val signature = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode("signature-bytes".toByteArray(Charsets.UTF_8))
        return "$header.$payload.$signature"
    }

    private class FakeCredentialManager(
        private val onGetCredential: suspend (Context, GetCredentialRequest) -> GetCredentialResponse = { _, _ ->
            throw NoCredentialException("No credentials configured")
        }
    ) : CredentialManager {
        val recordedRequests = mutableListOf<GetCredentialRequest>()
        val clearRequests = mutableListOf<ClearCredentialStateRequest>()

        override suspend fun getCredential(
            context: Context,
            request: GetCredentialRequest
        ): GetCredentialResponse {
            recordedRequests.add(request)
            return onGetCredential(context, request)
        }

        override suspend fun clearCredentialState(request: ClearCredentialStateRequest) {
            clearRequests.add(request)
        }

        override fun getCredentialAsync(
            context: Context,
            request: GetCredentialRequest,
            cancellationSignal: CancellationSignal?,
            executor: Executor,
            callback: CredentialManagerCallback<GetCredentialResponse, GetCredentialException>
        ) {
            throw UnsupportedOperationException("Suspend getCredential is used directly")
        }

        override fun getCredentialAsync(
            context: Context,
            pendingGetCredentialHandle: PrepareGetCredentialResponse.PendingGetCredentialHandle,
            cancellationSignal: CancellationSignal?,
            executor: Executor,
            callback: CredentialManagerCallback<GetCredentialResponse, GetCredentialException>
        ) {
            throw UnsupportedOperationException()
        }

        override fun prepareGetCredentialAsync(
            request: GetCredentialRequest,
            cancellationSignal: CancellationSignal?,
            executor: Executor,
            callback: CredentialManagerCallback<PrepareGetCredentialResponse, GetCredentialException>
        ) {
            throw UnsupportedOperationException()
        }

        override fun createCredentialAsync(
            context: Context,
            request: CreateCredentialRequest,
            cancellationSignal: CancellationSignal?,
            executor: Executor,
            callback: CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>
        ) {
            throw UnsupportedOperationException()
        }

        override fun clearCredentialStateAsync(
            request: ClearCredentialStateRequest,
            cancellationSignal: CancellationSignal?,
            executor: Executor,
            callback: CredentialManagerCallback<Void?, ClearCredentialException>
        ) {
            clearRequests.add(request)
            callback.onResult(null)
        }

        override fun createSettingsPendingIntent(): PendingIntent {
            throw UnsupportedOperationException()
        }
    }

    private class FakeFirebaseAuthBridge(
        override var currentUser: FirebaseLinkedSession? = null,
        private val linkedSessionFactory: (String) -> FirebaseLinkedSession? = { null }
    ) : FirebaseAuthBridge {
        private val listeners = mutableListOf<(FirebaseLinkedSession?) -> Unit>()
        var signOutCount = 0
        val receivedIdTokens = mutableListOf<String>()

        override fun addAuthStateListener(onUserChanged: (FirebaseLinkedSession?) -> Unit) {
            listeners.add(onUserChanged)
        }

        fun emitAuthStateChange(session: FirebaseLinkedSession?) {
            currentUser = session
            listeners.forEach { it(session) }
        }

        override suspend fun signInWithGoogleIdToken(idToken: String): FirebaseLinkedSession? {
            receivedIdTokens.add(idToken)
            val linked = linkedSessionFactory(idToken)
            if (linked != null) {
                currentUser = linked
            }
            return linked
        }

        override fun signOut() {
            signOutCount += 1
            currentUser = null
        }
    }

    @Test
    fun jwtValidator_validToken_extractsClaimsSubjectEmailNamePictureNonce() {
        val clientId = "1234567890-athenaweb.apps.googleusercontent.com"
        val nonce = GoogleIdTokenValidator.generateNonce()
        assertEquals(32, nonce.length)

        val jwt = buildSyntheticJwt(
            mapOf(
                "sub" to "google-sub-42600",
                "email" to "researcher@athena.science",
                "email_verified" to true,
                "name" to "Dr. Evelyn Vance",
                "picture" to "https://lh3.googleusercontent.com/a/avatar42",
                "iss" to "https://accounts.google.com",
                "aud" to clientId,
                "exp" to 2_000_000_000L,
                "nonce" to nonce
            )
        )

        val claims = GoogleIdTokenValidator.parseAndValidate(
            idToken = jwt,
            expectedClientId = clientId,
            expectedNonce = nonce,
            nowEpochSeconds = 1_900_000_000L
        )

        assertEquals("google-sub-42600", claims.subject)
        assertEquals("researcher@athena.science", claims.email)
        assertTrue(claims.emailVerified)
        assertEquals("Dr. Evelyn Vance", claims.displayName)
        assertEquals("https://lh3.googleusercontent.com/a/avatar42", claims.pictureUrl)
        assertEquals("https://accounts.google.com", claims.issuer)
        assertEquals(clientId, claims.audience)
        assertEquals(nonce, claims.nonce)
    }

    @Test
    fun jwtValidator_invalidClaims_rejectsMissingSubExpiredWrongAudWrongIssWrongNonce() {
        val clientId = "1234567890-athenaweb.apps.googleusercontent.com"

        val cases = listOf(
            "missing sub" to buildSyntheticJwt(
                mapOf("email" to "a@b.com", "iss" to "https://accounts.google.com")
            ),
            "expired exp" to buildSyntheticJwt(
                mapOf("sub" to "u1", "iss" to "https://accounts.google.com", "exp" to 1_700_000_000L)
            ),
            "wrong aud" to buildSyntheticJwt(
                mapOf("sub" to "u1", "iss" to "https://accounts.google.com", "aud" to "other.apps.googleusercontent.com")
            ),
            "wrong iss" to buildSyntheticJwt(
                mapOf("sub" to "u1", "iss" to "https://evil.example.com", "aud" to clientId)
            ),
            "wrong nonce" to buildSyntheticJwt(
                mapOf("sub" to "u1", "iss" to "https://accounts.google.com", "aud" to clientId, "nonce" to "tampered")
            )
        )

        for ((label, token) in cases) {
            try {
                GoogleIdTokenValidator.parseAndValidate(
                    idToken = token,
                    expectedClientId = clientId,
                    expectedNonce = "expected-nonce-123",
                    nowEpochSeconds = 1_800_000_000L
                )
                fail("Expected GoogleIdTokenValidationException for case: $label")
            } catch (_: GoogleIdTokenValidationException) {
                // Expected
            }
        }
    }

    @Test
    fun androidCredentialProvider_authorizedAccountImmediateSuccess_parsesGoogleIdTokenCredential() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val clientId = "998877-web.apps.googleusercontent.com"
        val fixedNonce = "nonce-stage1"
        val jwt = buildSyntheticJwt(
            mapOf(
                "sub" to "sub-stage1-99",
                "email" to "athena.lead@google.com",
                "name" to "Athena Lead",
                "iss" to "https://accounts.google.com",
                "aud" to clientId,
                "exp" to 1_900_000_500L,
                "nonce" to fixedNonce
            )
        )

        val fakeCm = FakeCredentialManager { _, _ ->
            val cred = GoogleIdTokenCredential.Builder()
                .setId("athena.lead@google.com")
                .setIdToken(jwt)
                .setDisplayName("Athena Lead")
                .setProfilePictureUri(Uri.parse("https://example.com/pic.png"))
                .build()
            GetCredentialResponse(cred)
        }

        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm },
            nonceGenerator = { fixedNonce },
            clockEpochSeconds = { 1_900_000_000L }
        )

        val outcome = provider.signIn(activity, clientId)
        assertTrue("Expected Success outcome, got $outcome", outcome is GoogleCredentialOutcome.Success)
        val verified = (outcome as GoogleCredentialOutcome.Success).credential
        assertEquals("sub-stage1-99", verified.subjectId)
        assertEquals("athena.lead@google.com", verified.email)
        assertEquals("Athena Lead", verified.displayName)
        assertEquals("https://example.com/pic.png", verified.avatarUrl)

        assertEquals(1, fakeCm.recordedRequests.size)
        val firstOption = fakeCm.recordedRequests[0].credentialOptions.single() as GetGoogleIdOption
        assertTrue(firstOption.filterByAuthorizedAccounts)
        assertTrue(firstOption.autoSelectEnabled)
        assertEquals(clientId, firstOption.serverClientId)
    }

    @Test
    fun androidCredentialProvider_noCredentialRetryChain_fallsThroughToAllAccountsAndSiwgSubtype() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val clientId = "554433-siwg.apps.googleusercontent.com"
        val fixedNonce = "nonce-stage3"
        val jwt = buildSyntheticJwt(
            mapOf(
                "sub" to "sub-siwg-777",
                "email" to "siwg.user@gmail.com",
                "name" to "SIWG Explorer",
                "iss" to "accounts.google.com",
                "aud" to clientId,
                "exp" to 1_950_000_000L,
                "nonce" to fixedNonce
            )
        )

        var callIndex = 0
        val fakeCm = FakeCredentialManager { _, _ ->
            callIndex += 1
            when (callIndex) {
                1 -> throw NoCredentialException("No authorized accounts")
                2 -> throw NoCredentialException("No one-tap accounts")
                else -> {
                    val baseGoogleCred = GoogleIdTokenCredential.Builder()
                        .setId("siwg.user@gmail.com")
                        .setIdToken(jwt)
                        .setDisplayName("SIWG Explorer")
                        .build()
                    val siwgCustomCred = CustomCredential(
                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL,
                        baseGoogleCred.data
                    )
                    GetCredentialResponse(siwgCustomCred)
                }
            }
        }

        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm },
            nonceGenerator = { fixedNonce },
            clockEpochSeconds = { 1_900_000_000L }
        )

        val outcome = provider.signIn(activity, clientId)
        assertTrue("Expected Stage 3 SIWG Success, got $outcome", outcome is GoogleCredentialOutcome.Success)
        val verified = (outcome as GoogleCredentialOutcome.Success).credential
        assertEquals("sub-siwg-777", verified.subjectId)
        assertEquals("SIWG Explorer", verified.displayName)

        assertEquals(3, fakeCm.recordedRequests.size)
        val opt1 = fakeCm.recordedRequests[0].credentialOptions.single() as GetGoogleIdOption
        val opt2 = fakeCm.recordedRequests[1].credentialOptions.single() as GetGoogleIdOption
        val opt3 = fakeCm.recordedRequests[2].credentialOptions.single()
        assertTrue(opt1.filterByAuthorizedAccounts)
        assertFalse(opt2.filterByAuthorizedAccounts)
        assertTrue(opt3 is GetSignInWithGoogleOption)
    }

    @Test
    fun androidCredentialProvider_nonActivityContext_returnsProviderUnavailableWithoutCrashing() = runBlocking {
        val fakeCm = FakeCredentialManager()
        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm }
        )

        val outcome = provider.signIn(appContext, "12345-valid.apps.googleusercontent.com")
        assertTrue(outcome is GoogleCredentialOutcome.ProviderUnavailable)
        assertEquals(0, fakeCm.recordedRequests.size)
    }

    @Test
    fun androidCredentialProvider_clearCredentialState_dispatchesClearCredentialStateRequest() = runBlocking {
        val fakeCm = FakeCredentialManager()
        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm }
        )

        provider.clearCredentialState()
        assertEquals(1, fakeCm.clearRequests.size)
    }

    @Test
    fun connectedAccountsManager_oauthClientIdValidationAndPersistence_rejectsApiKeyAndSurvivesSignOut() {
        val fakeProvider = object : GoogleCredentialAuthProvider {
            var cleared = 0
            override suspend fun signIn(activityContext: Context?, webClientId: String): GoogleCredentialOutcome {
                return GoogleCredentialOutcome.ProviderUnavailable("Unused")
            }
            override suspend fun clearCredentialState() {
                cleared += 1
            }
        }
        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = fakeProvider,
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )

        // Rejects Gemini API key pasted by mistake
        val apiKeyError = manager.validateOAuthClientId("AIzaSyD-1234567890abcdef")
        assertNotNull(apiKeyError)
        assertTrue(apiKeyError!!.contains("API key", ignoreCase = true))
        assertTrue(manager.saveCustomOAuthClientId("AIzaSyD-1234567890abcdef").isFailure)

        // Accepts valid OAuth 2.0 Web Client ID
        val validClientId = "42600852-athenaclient.apps.googleusercontent.com"
        assertNull(manager.validateOAuthClientId(validClientId))
        assertTrue(manager.saveCustomOAuthClientId("  $validClientId  ").isSuccess)
        assertEquals(validClientId, manager.oauthClientId.value)
        assertTrue(manager.isOAuthClientConfigured())

        // Connect fallback user then sign out — custom OAuth Client ID must survive signOut()
        manager.connectLocalFallbackProfile()
        assertNotNull(manager.currentUser.value)

        var signOutDone = false
        manager.signOut { signOutDone = true }
        assertTrue(signOutDone)
        assertNull(manager.currentUser.value)
        assertEquals(1, fakeProvider.cleared)
        assertEquals(validClientId, manager.oauthClientId.value)

        val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        assertEquals(validClientId, prefs.getString(ConnectedAccountsManager.KEY_OAUTH_CLIENT_ID, null))
    }

    @Test
    fun connectedAccountsManager_realGoogleCredentialWithFirebaseLink_alignsUidPersistsAvatarWithoutRawToken() = runBlocking {
        val secretRawIdToken = "eyJhbGciOiJSUzI1NiJ9.SECRET_RAW_ID_TOKEN_PAYLOAD.SIGNATURE"
        val validClientId = "777888-prod.apps.googleusercontent.com"

        val fakeProvider = object : GoogleCredentialAuthProvider {
            override suspend fun signIn(activityContext: Context?, webClientId: String): GoogleCredentialOutcome {
                return GoogleCredentialOutcome.Success(
                    VerifiedGoogleCredential(
                        idToken = secretRawIdToken,
                        subjectId = "google-sub-001",
                        email = "polymath@athena.org",
                        displayName = "Polymath One",
                        avatarUrl = "https://lh3.googleusercontent.com/a/polymath1"
                    )
                )
            }
            override suspend fun clearCredentialState() {}
        }
        val fakeFirebase = FakeFirebaseAuthBridge(
            linkedSessionFactory = { token ->
                assertEquals(secretRawIdToken, token)
                FirebaseLinkedSession(
                    uid = "firebase-uid-aligned-900",
                    email = "polymath@athena.org",
                    displayName = "Polymath One",
                    avatarUrl = "https://lh3.googleusercontent.com/a/polymath1"
                )
            }
        )

        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = fakeProvider,
            firebaseAuthBridge = fakeFirebase,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )
        manager.saveCustomOAuthClientId(validClientId)

        val result = manager.signInWithGoogleSuspend(activityContext = null, allowLocalFallback = false)
        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("firebase-uid-aligned-900", user.uid)
        assertEquals("google-sub-001", user.googleSubjectId)
        assertTrue(user.isFirebaseLinked)
        assertTrue(user.isGoogleConnected)
        assertEquals(AccountAuthSource.FIREBASE_AUTH, user.authSource)
        assertEquals("https://lh3.googleusercontent.com/a/polymath1", user.avatarUrl)

        // Verify SharedPreferences persists avatarUrl & authSource, and NEVER stores raw idToken
        val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val serializedPrefs = prefs.all.entries.joinToString("\n") { "${it.key}=${it.value}" }
        assertFalse("Raw JWT idToken must never be persisted in SharedPreferences", serializedPrefs.contains(secretRawIdToken))

        // Reload in a fresh manager instance and verify full session restoration
        val reloadedManager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = fakeProvider,
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )
        val restored = reloadedManager.currentUser.value
        assertNotNull(restored)
        assertEquals("firebase-uid-aligned-900", restored!!.uid)
        assertEquals("https://lh3.googleusercontent.com/a/polymath1", restored.avatarUrl)
        assertEquals(AccountAuthSource.FIREBASE_AUTH, restored.authSource)
        assertTrue(restored.isFirebaseLinked)
    }

    @Test
    fun connectedAccountsManager_firebaseAuthListenerUpdate_preservesUserDriveAndYouTubeToggles() {
        val fakeFirebase = FakeFirebaseAuthBridge()
        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = object : GoogleCredentialAuthProvider {
                override suspend fun signIn(activityContext: Context?, webClientId: String) =
                    GoogleCredentialOutcome.ProviderUnavailable("Unused")
                override suspend fun clearCredentialState() {}
            },
            firebaseAuthBridge = fakeFirebase,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )

        manager.connectLocalFallbackProfile()
        manager.toggleServiceLink("drive", false)
        manager.toggleServiceLink("youtube", false)
        assertFalse(manager.currentUser.value!!.isDriveConnected)
        assertFalse(manager.currentUser.value!!.isYouTubeConnected)

        fakeFirebase.emitAuthStateChange(
            FirebaseLinkedSession(
                uid = "fb-listener-uid",
                email = "listener@athena.org",
                displayName = "Listener Operator",
                avatarUrl = null
            )
        )

        val updated = manager.currentUser.value!!
        assertEquals("fb-listener-uid", updated.uid)
        assertFalse("User-disabled Drive toggle must be preserved across Firebase AuthStateListener refresh", updated.isDriveConnected)
        assertFalse("User-disabled YouTube toggle must be preserved across Firebase AuthStateListener refresh", updated.isYouTubeConnected)
    }

    @Test
    fun connectedAccountsManager_unconfiguredClientId_fallsBackToLocalWorkspaceProfile() = runBlocking {
        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = object : GoogleCredentialAuthProvider {
                override suspend fun signIn(activityContext: Context?, webClientId: String): GoogleCredentialOutcome {
                    fail("Credential provider should not be called when client ID is empty")
                    return GoogleCredentialOutcome.ProviderUnavailable("Unreachable")
                }
                override suspend fun clearCredentialState() {}
            },
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )

        val res = manager.signInWithGoogleSuspend(activityContext = null, allowLocalFallback = true)
        assertTrue(res.isSuccess)
        val user = res.getOrThrow()
        assertFalse(user.isGoogleConnected)
        assertEquals(AccountAuthSource.LOCAL_FALLBACK, user.authSource)
        assertNotNull(manager.authStatusMessage.value)
        assertTrue(manager.authStatusMessage.value!!.contains("No OAuth 2.0 Web Client ID", ignoreCase = true))
    }

    @Test
    fun connectedAccountsManager_providerUnavailable_fallsBackGracefullyWithStatusBanner() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val fakeCm = FakeCredentialManager { _, _ ->
            throw GetCredentialProviderConfigurationException("Play Services provider missing")
        }
        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm }
        )
        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = provider,
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )
        manager.saveCustomOAuthClientId("112233-fallback.apps.googleusercontent.com")

        val res = manager.signInWithGoogleSuspend(activityContext = activity, allowLocalFallback = true)
        assertTrue(res.isSuccess)
        val user = res.getOrThrow()
        assertFalse(user.isGoogleConnected)
        assertEquals(AccountAuthSource.LOCAL_FALLBACK, user.authSource)
        assertTrue(manager.authStatusMessage.value!!.contains("unavailable", ignoreCase = true))
    }

    @Test
    fun connectedAccountsManager_userCancellation_doesNotForceLocalFallbackAndInvokesOnError() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val fakeCm = FakeCredentialManager { _, _ ->
            throw GetCredentialCancellationException("User dismissed account selector")
        }
        val provider = AndroidGoogleCredentialAuthProvider(
            context = appContext,
            credentialManagerFactory = { fakeCm }
        )
        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = provider,
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )
        manager.saveCustomOAuthClientId("990011-cancel.apps.googleusercontent.com")

        var capturedError: String? = null
        manager.signInWithGoogle(
            activityContext = activity,
            allowLocalFallback = true,
            onSuccess = { fail("User cancellation must NOT silently sign in a local fallback user") },
            onError = { capturedError = it }
        )

        assertNull(manager.currentUser.value)
        assertNotNull(capturedError)
        assertTrue(capturedError!!.contains("dismissed", ignoreCase = true) || capturedError!!.contains("cancelled", ignoreCase = true))
    }

    @Test
    fun connectedAccountSheet_composeUiFlow_showsUnconfiguredBannerSavesClientIdSignsInAndSignsOut() {
        var clearStateInvocations = 0
        val fakeProvider = object : GoogleCredentialAuthProvider {
            override suspend fun signIn(activityContext: Context?, webClientId: String): GoogleCredentialOutcome {
                return GoogleCredentialOutcome.Success(
                    VerifiedGoogleCredential(
                        idToken = "jwt-ui-test",
                        subjectId = "ui-google-sub-42",
                        email = "ui.verified@athena.ai",
                        displayName = "Athena Verified User",
                        avatarUrl = null
                    )
                )
            }
            override suspend fun clearCredentialState() {
                clearStateInvocations += 1
            }
        }

        val manager = ConnectedAccountsManager(
            context = appContext,
            credentialAuthProvider = fakeProvider,
            firebaseAuthBridge = FakeFirebaseAuthBridge(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            prefsName = prefsName
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                ConnectedAccountSheet(
                    accountsManager = manager,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // 1. Unconfigured notice banner is visible when signed out
        composeTestRule.onNodeWithTag("banner_auth_status").assertIsDisplayed()

        // 2. Enter & save a valid *.apps.googleusercontent.com Web Client ID
        val validClientId = "85200-athenaui.apps.googleusercontent.com"
        composeTestRule.onNodeWithTag("input_oauth_client_id").performScrollTo().performTextReplacement(validClientId)
        composeTestRule.onNodeWithTag("btn_save_oauth_client_id").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(validClientId, manager.oauthClientId.value)

        // 3. Click Sign In and verify Google OAuth Verified badge renders
        composeTestRule.onNodeWithTag("btn_google_signin_action").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Athena Verified User").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Google OAuth Verified").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_account_auth_source").performScrollTo().assertIsDisplayed()

        // 4. Click Sign Out and verify clearCredentialState() was invoked and client ID preserved
        composeTestRule.onNodeWithTag("btn_sign_out_account").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, clearStateInvocations)
        assertNull(manager.currentUser.value)
        assertEquals(validClientId, manager.oauthClientId.value)
    }
}
