package com.shrivatsav.monomail.core.data.auth

import android.content.Context
import android.content.Intent

interface GoogleAuthHelper {
    suspend fun clearToken(context: Context, token: String)
    suspend fun getToken(context: Context, email: String, scope: String): String
    suspend fun signIn(context: Context, clientId: String): GoogleSignInData
    suspend fun clearCredentialState(context: Context)
}

data class GoogleSignInData(
    val email: String,
    val displayName: String,
    val photoUrl: String?
)

class GoogleAuthException(
    message: String,
    val intent: Intent? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    constructor(message: String, cause: Throwable?) : this(message, null, cause)
}
