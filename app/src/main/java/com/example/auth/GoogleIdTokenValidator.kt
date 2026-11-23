package com.example.auth

import org.json.JSONObject
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class ValidatedGoogleIdClaims(
    val subject: String,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val displayName: String? = null,
    val pictureUrl: String? = null,
    val issuer: String? = null,
    val audience: String? = null,
    val expiresAtEpochSeconds: Long? = null,
    val nonce: String? = null
)

class GoogleIdTokenValidationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

object GoogleIdTokenValidator {
    private val VALID_ISSUERS = setOf("https://accounts.google.com", "accounts.google.com")
    private val secureRandom = SecureRandom()

    fun generateNonce(byteLength: Int = 16): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun parseAndValidate(
        idToken: String,
        expectedClientId: String? = null,
        expectedNonce: String? = null,
        nowEpochSeconds: Long? = System.currentTimeMillis() / 1000L
    ): ValidatedGoogleIdClaims {
        val segments = idToken.trim().split('.')
        if (segments.size != 3 || segments[0].isBlank() || segments[1].isBlank()) {
            throw GoogleIdTokenValidationException("Malformed Google ID token JWT structure")
        }

        val payloadJson = try {
            val decoded = Base64.UrlSafe
                .withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(segments[1])
            JSONObject(String(decoded, Charsets.UTF_8))
        } catch (e: Exception) {
            throw GoogleIdTokenValidationException("Invalid Google ID token payload encoding", e)
        }

        val sub = payloadJson.optString("sub", "").trim()
        if (sub.isEmpty()) {
            throw GoogleIdTokenValidationException("Google ID token missing required 'sub' claim")
        }

        val iss = payloadJson.optString("iss", "").trim().takeIf { it.isNotEmpty() }
        if (iss != null && iss !in VALID_ISSUERS) {
            throw GoogleIdTokenValidationException("Untrusted Google ID token issuer: $iss")
        }

        val audArray = payloadJson.optJSONArray("aud")
        val resolvedAudience: String? = when {
            audArray != null && audArray.length() > 0 -> audArray.optString(0, "").trim().takeIf { it.isNotEmpty() }
            else -> payloadJson.optString("aud", "").trim().takeIf { it.isNotEmpty() }
        }
        if (!expectedClientId.isNullOrBlank() && payloadJson.has("aud")) {
            val matchesAud = if (audArray != null) {
                (0 until audArray.length()).any { audArray.optString(it) == expectedClientId }
            } else {
                payloadJson.optString("aud", "") == expectedClientId
            }
            if (!matchesAud) {
                throw GoogleIdTokenValidationException(
                    "Google ID token 'aud' claim does not match configured OAuth Web Client ID"
                )
            }
        }

        val exp = if (payloadJson.has("exp")) payloadJson.optLong("exp", -1L) else null
        if (exp != null && exp <= 0L) {
            throw GoogleIdTokenValidationException("Invalid Google ID token 'exp' claim")
        }
        if (nowEpochSeconds != null && exp != null && exp < (nowEpochSeconds - 60L)) {
            throw GoogleIdTokenValidationException("Google ID token has expired")
        }

        val tokenNonce = payloadJson.optString("nonce", "").trim().takeIf { it.isNotEmpty() }
        if (!expectedNonce.isNullOrBlank() && tokenNonce != null && tokenNonce != expectedNonce) {
            throw GoogleIdTokenValidationException("Google ID token nonce mismatch")
        }

        return ValidatedGoogleIdClaims(
            subject = sub,
            email = payloadJson.optString("email", "").trim().takeIf { it.isNotEmpty() },
            emailVerified = payloadJson.optBoolean("email_verified", false),
            displayName = payloadJson.optString("name", "").trim().takeIf { it.isNotEmpty() },
            pictureUrl = payloadJson.optString("picture", "").trim().takeIf { it.isNotEmpty() },
            issuer = iss,
            audience = resolvedAudience,
            expiresAtEpochSeconds = exp,
            nonce = tokenNonce
        )
    }
}
