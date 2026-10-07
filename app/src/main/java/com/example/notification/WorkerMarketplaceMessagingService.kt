package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Production Firebase Messaging Service - Master PRD Section 3, 6, 8, 9
 * Safely processes FCM data payloads, displays notifications in foreground/background,
 * and maintains device tokens in users/{uid}/devices/{deviceId}.
 */
class WorkerMarketplaceMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token received: $token")
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                FcmTokenManager.saveTokenToFirestore(applicationContext, currentUid, token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        // 1. Extract payload safely (PRD Section 8)
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data["title"] ?: notification?.title ?: "कार्यशील (Karyasheel)"
        val body = data["body"] ?: notification?.body ?: "आपके पास नया अपडेट है।"
        val type = data["type"] ?: "SYSTEM"
        val targetId = data["targetId"] ?: ""
        val notificationId = data["notificationId"] ?: System.currentTimeMillis().toString()

        // 2. Select appropriate channel (PRD Section 6)
        val channelId = when (type.uppercase()) {
            "NEW_JOB", "NEARBY_JOB", "JOB_ACCEPTED", "JOB_COMPLETED" -> NotificationChannels.CHANNEL_JOBS
            "PROFILE_VERIFIED", "PROFILE_UPDATE", "ACCOUNT_WARNING" -> NotificationChannels.CHANNEL_ACCOUNT
            else -> NotificationChannels.CHANNEL_GENERAL
        }

        // 3. Build Intent with navigation deep link data (PRD Section 9)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Ensure notification channels are registered
        NotificationChannels.createChannels(this)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_launcher_icon_1791270030897)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyId = (notificationId.hashCode() and 0x7FFFFFFF)
        notificationManager.notify(notifyId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "WorkerFCMService"
    }
}
