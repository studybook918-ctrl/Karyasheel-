package com.example.notification

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.model.DeviceTokenEntity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Production FCM Token Manager - Master PRD Section 4 & 5
 * Handles stable deviceId generation and atomic Firestore updates to users/{uid}/devices/{deviceId}.
 * Checks Google Play Services availability and graceful error handling.
 * Keeps autoInitEnabled = false by default to prevent hard failure exceptions in browser/emulator
 * environments without valid Google Play Services accounts.
 */
object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    fun getDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                "device_${BuildHashCode(context)}"
            }
        } catch (e: Exception) {
            "device_${BuildHashCode(context)}"
        }
    }

    private fun BuildHashCode(context: Context): String {
        return (context.packageName.hashCode() xor android.os.Build.MODEL.hashCode()).toString()
    }

    /**
     * Checks if Google Play Services is available on the device before querying FCM.
     * In emulator or stripped AOSP environments without Play Services, FCM token registration fails with hard exception.
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return try {
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetch current FCM token and sync to users/{uid}/devices/{deviceId}.
     * Only invoked when user is authenticated and Play Services are present.
     * Keeps autoInitEnabled false so FCM's internal daemon doesn't automatically trigger failed retries.
     */
    fun syncCurrentToken(context: Context, targetUid: String? = null) {
        if (!isGooglePlayServicesAvailable(context)) {
            Log.d(TAG, "Google Play Services not available in current environment; skipping FCM token fetch.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    Log.d(TAG, "Firebase not yet initialized; skipping token sync.")
                    return@launch
                }

                val messaging = FirebaseMessaging.getInstance()
                // Do NOT force isAutoInitEnabled = true, which triggers internal daemon retries
                messaging.token
                    .addOnCompleteListener { task ->
                        try {
                            if (task.isSuccessful && !task.result.isNullOrBlank()) {
                                val token = task.result
                                val uid = targetUid ?: FirebaseAuth.getInstance().currentUser?.uid
                                if (!uid.isNullOrBlank()) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveTokenToFirestore(context, uid, token)
                                    }
                                } else {
                                    Log.d(TAG, "FCM token ready: $token (User not yet authenticated)")
                                }
                            } else {
                                Log.d(TAG, "FCM token retrieval notice (Play Services account/network): ${task.exception?.message}")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "FCM token task notice: ${e.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.d(TAG, "FCM sync notice: ${e.message}")
            }
        }
    }

    suspend fun saveTokenToFirestore(context: Context, uid: String, token: String): Boolean {
        return try {
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser == null || authUser.uid != uid) {
                Log.d(TAG, "User not authenticated in Firebase Auth; skipping Firestore token write for $uid")
                return false
            }

            val deviceId = getDeviceId(context)
            val now = System.currentTimeMillis()
            val deviceEntity = DeviceTokenEntity(
                deviceId = deviceId,
                token = token,
                platform = "android",
                createdAt = now,
                updatedAt = now,
                lastUsedAt = now
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("devices")
                .document(deviceId)
                .set(deviceEntity, SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced FCM token for user $uid on device $deviceId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "FCM token write notice (permissions or offline): ${e.message}")
            false
        }
    }
}
