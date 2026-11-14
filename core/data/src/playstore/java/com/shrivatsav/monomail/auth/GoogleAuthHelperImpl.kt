package com.shrivatsav.monomail.core.data.auth

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.SecureRandom

object GoogleAuthHelperImpl : GoogleAuthHelper {
    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

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
        val targetContext = context.findActivity() ?: context
        val credentialManager = CredentialManager.create(targetContext)
        val nonce = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            .joinToString("") { "%02x".format(it) }

        val result = try {
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId)
                .setNonce(nonce)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            credentialManager.getCredential(
                request = request,
                context = targetContext
            )
        } catch (e: Exception) {
            if (e is GetCredentialCancellationException) {
                throw GoogleAuthException("Sign-in cancelled")
            }
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .setNonce(nonce)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                credentialManager.getCredential(
                    request = fallbackRequest,
                    context = targetContext
                )
            } catch (fallbackError: Exception) {
                if (fallbackError is GetCredentialCancellationException) {
                    throw GoogleAuthException("Sign-in cancelled")
                }
                val errorMsg = e.message ?: fallbackError.message ?: e::class.java.simpleName
                throw GoogleAuthException(
                    "Google sign-in failed: $errorMsg. " +
                    "Make sure a Google account is added in Settings > Accounts."
                )
            }
        }

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(result.credential.data)
            return GoogleSignInData(
                email = googleIdTokenCredential.id,
                displayName = googleIdTokenCredential.displayName ?: "User",
                photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
            )
        } catch (e: Exception) {
            throw GoogleAuthException("Failed to parse Google sign-in response: ${e.message}")
        }
    }

    override suspend fun clearCredentialState(context: Context) {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

fun provideGoogleAuthHelper(): GoogleAuthHelper = GoogleAuthHelperImpl
