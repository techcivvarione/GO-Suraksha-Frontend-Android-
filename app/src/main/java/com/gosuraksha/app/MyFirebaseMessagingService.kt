package com.gosuraksha.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.navigation.Screen
import com.gosuraksha.app.data.remote.dto.auth.RegisterDeviceRequest
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.scam.ScamAlertNavigationStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "GO Suraksha"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Security alert"

        if (BuildConfig.DEBUG) {
            Log.d("GO_SURAKSHA_PUSH", "Push notification received")
        }

        val launchIntent = buildScamNetworkIntent(remoteMessage)
        showNotification(title, body, launchIntent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (BuildConfig.DEBUG) {
            Log.d("GO_SURAKSHA_PUSH", "FCM token refreshed")
        }
        registerDeviceToken(token)
    }

    private fun registerDeviceToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                ApiClient.authApi.registerDevice(
                    RegisterDeviceRequest(
                        device_token = token,
                        device_type = "android"
                    )
                )
            }.onFailure { error ->
                Log.e("GO_SURAKSHA_PUSH", "Failed to register device token", error)
            }
        }
    }

    private fun showNotification(title: String, message: String, launchIntent: Intent?) {
        val channelId = CHANNEL_ID
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "GO Suraksha Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildScamNetworkIntent(remoteMessage: RemoteMessage): Intent? {
        if (remoteMessage.data["feature"] != "scam_alert_network") return null
        val alertId = remoteMessage.data["alert_id"]
        val route = if (alertId.isNullOrBlank()) {
            Screen.ScamAlertsFeed.route
        } else {
            Screen.ScamAlertDetail.route
        }
        return Intent(this, MainActivity::class.java).apply {
            putExtra(ScamAlertNavigationStore.EXTRA_SCAM_ROUTE, route)
            putExtra(ScamAlertNavigationStore.EXTRA_SCAM_ALERT_ID, alertId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private companion object {
        private const val CHANNEL_ID = "gosuraksha_alerts"
    }
}
