package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Production Notification Channels - Master PRD Section 6
 * 1. General (सामान्य सूचनाएँ)
 * 2. Jobs (नए काम और अवसर)
 * 3. Account (पहचान व खाता सुरक्षा)
 */
object NotificationChannels {
    const val CHANNEL_GENERAL = "channel_general"
    const val CHANNEL_JOBS = "channel_jobs"
    const val CHANNEL_ACCOUNT = "channel_account"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "सामान्य सूचनाएं (General)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "कार्यशील ऐप से जुड़ी सामान्य घोषणाएं व अपडेट्स"
                enableLights(true)
            }

            val jobsChannel = NotificationChannel(
                CHANNEL_JOBS,
                "काम व अवसर (Jobs & Work)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "नया काम, नजदीकी काम के अवसर और जॉब से जुड़े अपडेट्स"
                enableLights(true)
                enableVibration(true)
            }

            val accountChannel = NotificationChannel(
                CHANNEL_ACCOUNT,
                "खाता व सत्यापन (Account & Verification)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "डिजिटल पहचान-पत्र, सत्यापन और सुरक्षा अलर्ट"
                enableLights(true)
            }

            notificationManager.createNotificationChannels(listOf(generalChannel, jobsChannel, accountChannel))
        }
    }
}
