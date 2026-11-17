package com.shrivatsav.monomail.core.data.auth

import android.accounts.Account
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.SecureRandom

object GoogleAuthHelperImpl : GoogleAuthHelper {
    private const val TAG = "GoogleAuth"
    private const val EXPECTED_WEB_CLIENT_ID = "193693002878-ajpu4r5m7f5qjr20v9a1g7qi0c58iqod.apps.googleusercontent.com"

    override suspend fun clearToken(context: Context, token: String) {
        android.util.Log.d(TAG, "clearToken: Clearing token from GoogleAuthUtil cache")
        GoogleAuthUtil.clearToken(context, token)
    }

    override suspend fun getToken(context: Context, email: String, scope: String): String {
        try {
            android.util.Log.d(TAG, "getToken: Acquiring Google API token for $email (scope: $scope)")
            val token = GoogleAuthUtil.getToken(context, Account(email, "com.google"), scope)
            android.util.Log.i(TAG, "getToken: Successfully acquired Google API token for $email")
            return token
        } catch (e: UserRecoverableAuthException) {
            android.util.Log.w(TAG, "getToken: User consent required for $email: ${e.message}", e)
            throw GoogleAuthException("Google API token acquisition requires user consent: ${e.message ?: "UserRecoverableAuthException"}", e.intent, e)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getToken: Google API token acquisition failed for $email: [${e::class.java.simpleName}] ${e.message}", e)
            throw GoogleAuthException("Google API token acquisition failed: ${e.message ?: e::class.java.simpleName}", null, e)
        }
    }

    private fun generateNonce(): String =
        ByteArray(32)
            .apply { SecureRandom().nextBytes(this) }
            .joinToString("") { "%02x".format(it) }

    private fun parseGoogleCredential(result: GetCredentialResponse, stepName: String): GoogleSignInData {
        try {
            android.util.Log.d(TAG, "[$stepName] Parsing credential data of type: ${result.credential.type}")
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            android.util.Log.i(TAG, "[$stepName] Successfully parsed Google ID token for: ${credential.id}")
            return GoogleSignInData(
                email = credential.id,
                displayName = credential.displayName ?: "User",
                photoUrl = credential.profilePictureUri?.toString()
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[$stepName] Google ID token parsing failed: ${e::class.java.simpleName}: ${e.message}", e)
            throw GoogleAuthException("Google ID token parsing failure ($stepName): ${e.message ?: e::class.java.simpleName}", null, e)
        }
    }

    override suspend fun signIn(context: Context, clientId: String): GoogleSignInData {
        val cleanClientId = clientId.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removePrefix("https://")
            .removePrefix("http://")
            .removeSuffix("/")
            .trim()
        if (cleanClientId.isBlank()) {
            throw GoogleAuthException(
                "Google Web OAuth Client ID is not configured. Check the GOOGLE_CLIENT_ID build secret."
            )
        }

        val isExpectedId = cleanClientId == EXPECTED_WEB_CLIENT_ID
        val clientDiagnostic = "clientIdLen=${cleanClientId.length}, matchesExpected=$isExpectedId"
        android.util.Log.i(TAG, "signIn: Starting Google sign-in ($clientDiagnostic)")

        val credentialManager = CredentialManager.create(context)

        // Stage 1: Attempt GetGoogleIdOption (bottom sheet / one-tap sign-in)
        try {
            android.util.Log.d(TAG, "[GetGoogleIdOption] Building request (filterByAuthorizedAccounts=false)...")
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(cleanClientId)
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            android.util.Log.d(TAG, "[GetGoogleIdOption] Calling credentialManager.getCredential...")
            val result = credentialManager.getCredential(request = request, context = context)
            android.util.Log.i(TAG, "[GetGoogleIdOption] Credential received successfully.")
            return parseGoogleCredential(result, "GetGoogleIdOption")
        } catch (e: GetCredentialCancellationException) {
            android.util.Log.i(TAG, "[GetGoogleIdOption] Account selection cancelled or dismissed: ${e.message}")
            throw GoogleAuthException("Google sign-in cancelled: ${e.message ?: "User cancelled"} (GetGoogleIdOption)", null, e)
        } catch (e: Exception) {
            android.util.Log.w(
                TAG,
                "[GetGoogleIdOption] Failed with [${e::class.java.simpleName}] ${e.message}; attempting explicit GetSignInWithGoogleOption fallback...",
                e
            )
        }

        // Stage 2: Fallback to GetSignInWithGoogleOption (explicit account picker dialog)
        try {
            android.util.Log.d(TAG, "[GetSignInWithGoogleOption] Building fallback request...")
            val signInOption = GetSignInWithGoogleOption.Builder(cleanClientId)
                .setNonce(generateNonce())
                .build()

            val fallbackRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            android.util.Log.d(TAG, "[GetSignInWithGoogleOption] Calling credentialManager.getCredential...")
            val result = credentialManager.getCredential(
                request = fallbackRequest,
                context = context
            )
            android.util.Log.i(TAG, "[GetSignInWithGoogleOption] Credential received successfully.")
            return parseGoogleCredential(result, "GetSignInWithGoogleOption")
        } catch (fallbackError: GetCredentialCancellationException) {
            android.util.Log.i(TAG, "[GetSignInWithGoogleOption] Sign-in cancelled or dismissed: ${fallbackError.message}")
            throw GoogleAuthException("Google sign-in cancelled: ${fallbackError.message ?: "User cancelled"} (GetSignInWithGoogleOption)", null, fallbackError)
        } catch (fallbackError: GetCredentialException) {
            android.util.Log.e(
                TAG,
                "[GetSignInWithGoogleOption] Explicit Sign in with Google failed: type=${fallbackError.type}, message=${fallbackError.message}",
                fallbackError
            )
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (clearError: Exception) {
                android.util.Log.w(TAG, "Could not clear Credential Manager state after failed sign-in.", clearError)
            }
            throw GoogleAuthException(
                "Google sign-in failed (GetSignInWithGoogleOption): [${fallbackError.type}] ${fallbackError.message ?: fallbackError.type} ($clientDiagnostic)",
                null,
                fallbackError
            )
        } catch (fallbackError: Exception) {
            android.util.Log.e(
                TAG,
                "[GetSignInWithGoogleOption] Unexpected error: ${fallbackError::class.java.simpleName}: ${fallbackError.message}",
                fallbackError
            )
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (clearError: Exception) {
                android.util.Log.w(TAG, "Could not clear Credential Manager state after failed sign-in.", clearError)
            }
            throw GoogleAuthException(
                "Google sign-in failed (GetSignInWithGoogleOption): ${fallbackError.message ?: fallbackError::class.java.simpleName} ($clientDiagnostic)",
                null,
                fallbackError
            )
        }
    }

    override suspend fun clearCredentialState(context: Context) {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

fun provideGoogleAuthHelper(): GoogleAuthHelper = GoogleAuthHelperImpl
