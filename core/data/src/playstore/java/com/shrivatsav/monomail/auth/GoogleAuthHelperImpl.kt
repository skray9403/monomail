package com.shrivatsav.monomail.core.data.auth

import android.accounts.Account
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.SecureRandom

object GoogleAuthHelperImpl : GoogleAuthHelper {
    override suspend fun clearToken(context: Context, token: String) {
        GoogleAuthUtil.clearToken(context, token)
    }

    override suspend fun getToken(context: Context, email: String, scope: String): String {
        try {
            return GoogleAuthUtil.getToken(context, Account(email, "com.google"), scope)
        } catch (e: UserRecoverableAuthException) {
            throw GoogleAuthException(e.message ?: "UserRecoverableAuthException", e.intent)
        } catch (e: Exception) {
            throw GoogleAuthException(e.message ?: "Unknown error")
        }
    }

    override suspend fun signIn(context: Context, clientId: String): GoogleSignInData {
        if (clientId.isBlank()) {
            throw GoogleAuthException(
                "Google Web OAuth Client ID is not configured. Check the GOOGLE_CLIENT_ID build secret."
            )
        }

        val credentialManager = CredentialManager.create(context)

        fun generateNonce(): String =
            ByteArray(32)
                .apply { SecureRandom().nextBytes(this) }
                .joinToString("") { "%02x".format(it) }

        fun parseGoogleCredential(result: GetCredentialResponse): GoogleSignInData {
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            return GoogleSignInData(
                email = credential.id,
                displayName = credential.displayName ?: "User",
                photoUrl = credential.profilePictureUri?.toString()
            )
        }

        try {
            // Preferred flow: Credential Manager's standard Google account picker.
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            return parseGoogleCredential(
                credentialManager.getCredential(request = request, context = context)
            )
        } catch (e: NoCredentialException) {
            // Fallback recommended for cases where the standard picker has no eligible
            // credentials: show the explicit Sign in with Google flow.
            android.util.Log.w(
                "GoogleAuth",
                "No credentials from GetGoogleIdOption; trying explicit Google sign-in.",
                e
            )

            try {
                val signInOption = GetSignInWithGoogleOption.Builder(clientId)
                    .setNonce(generateNonce())
                    .build()

                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()

                return parseGoogleCredential(
                    credentialManager.getCredential(
                        request = fallbackRequest,
                        context = context
                    )
                )
            } catch (fallbackError: GetCredentialException) {
                android.util.Log.e(
                    "GoogleAuth",
                    "Explicit Sign in with Google failed: ${fallbackError.type}",
                    fallbackError
                )
                // Clear a possibly stale provider session after a failed reauth attempt.
                // This is particularly important for AccountReauth failures on Android 17.
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (clearError: Exception) {
                    android.util.Log.w(
                        "GoogleAuth",
                        "Could not clear Credential Manager state after failed sign-in.",
                        clearError
                    )
                }
                throw GoogleAuthException(
                    "Google sign-in failed: ${fallbackError.message ?: fallbackError.type}"
                )
            } catch (fallbackError: Exception) {
                android.util.Log.e(
                    "GoogleAuth",
                    "Unexpected error in explicit Google sign-in.",
                    fallbackError
                )
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (clearError: Exception) {
                    android.util.Log.w(
                        "GoogleAuth",
                        "Could not clear Credential Manager state after failed sign-in.",
                        clearError
                    )
                }
                throw GoogleAuthException(
                    "Google sign-in failed: ${fallbackError.message ?: "Unknown error"}"
                )
            }
        } catch (e: GetCredentialException) {
            android.util.Log.e("GoogleAuth", "Credential Manager failed: ${e.type}", e)
            throw GoogleAuthException("Google sign-in failed: ${e.message ?: e.type}")
        } catch (e: Exception) {
            android.util.Log.e("GoogleAuth", "Unexpected Google sign-in error.", e)
            throw GoogleAuthException("Google sign-in failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun clearCredentialState(context: Context) {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

fun provideGoogleAuthHelper(): GoogleAuthHelper = GoogleAuthHelperImpl
