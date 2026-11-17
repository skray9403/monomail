package com.shrivatsav.monomail.push

import android.util.Log
import com.shrivatsav.monomail.core.data.auth.UserProfile
import com.shrivatsav.monomail.core.data.push.PushNotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManagerImpl @Inject constructor() : PushNotificationManager {
    override suspend fun registerForPushNotifications(account: UserProfile) {
        Log.i("PushManager", "Push notifications disabled by build configuration.")
    }

    override suspend fun unregisterForPushNotifications(account: UserProfile) {
        Log.i("PushManager", "Push notifications disabled; unregister is a no-op.")
    }

    override suspend fun onTokenRefresh(newToken: String) {
        Log.i("PushManager", "Push notifications disabled; token refresh is a no-op.")
    }
}
