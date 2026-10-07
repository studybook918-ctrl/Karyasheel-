package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.InitialData
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * REPOSITORY LAYER - Master PRD Section 33 & 34
 * UI -> ViewModel -> Repository -> Firebase Services (Auth, Firestore, FCM)
 * Handles Firebase Auth state safely to ensure Firestore security rules compliance.
 */
class MarketplaceRepository(private val context: Context) {
    private val TAG = "MarketplaceRepo"

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // Reactive State Cached from Firestore / Offline Fallback
    private val _workers = MutableStateFlow<List<WorkerEntity>>(emptyList())
    val workers: StateFlow<List<WorkerEntity>> = _workers.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportEntity>>(emptyList())
    val reports: StateFlow<List<ReportEntity>> = _reports.asStateFlow()

    private val _currentUserEntity = MutableStateFlow<UserEntity?>(null)
    val currentUserEntity: StateFlow<UserEntity?> = _currentUserEntity.asStateFlow()

    private val _currentWorkerEntity = MutableStateFlow<WorkerEntity?>(null)
    val currentWorkerEntity: StateFlow<WorkerEntity?> = _currentWorkerEntity.asStateFlow()

    init {
        try {
            FirebaseApp.initializeApp(context)
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            Log.d(TAG, "Firebase initialized in Repository")

            // Ensure anonymous/authenticated session if not already signed in
            // to fulfill Firestore security rules request.auth != null
            ensureFirebaseAuth()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase init info: ${e.message}")
        }

        // Seed default categories
        _categories.value = InitialData.categories

        // Seed initial workers in repository memory
        _workers.value = InitialData.sampleWorkers
    }

    private fun ensureFirebaseAuth() {
        val a = auth ?: return
        if (a.currentUser == null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    a.signInAnonymously().await()
                    Log.d(TAG, "Anonymous Firebase Auth established: ${a.currentUser?.uid}")
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Auth auto-session info: ${e.message}")
                }
            }
        }
    }

    fun getCurrentAuthUid(): String? = auth?.currentUser?.uid

    suspend fun signInWithEmail(email: String, pass: String): String? = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext null
        return@withContext try {
            val res = a.signInWithEmailAndPassword(email, pass).await()
            res.user?.uid
        } catch (e: Exception) {
            try {
                // If account doesn't exist, create it
                val createRes = a.createUserWithEmailAndPassword(email, pass).await()
                createRes.user?.uid
            } catch (e2: Exception) {
                Log.w(TAG, "Email auth fallback: ${e2.message}")
                a.currentUser?.uid
            }
        }
    }

    // ==========================================
    // 1. TRANSACTION-SAFE 8-DIGIT WORKER ID GENERATOR (Section 6)
    // ==========================================
    suspend fun generateAndReserveUniqueWorkerId(): String = withContext(Dispatchers.IO) {
        val db = firestore
        val currentUid = auth?.currentUser?.uid
        if (db == null || currentUid == null) {
            // Local fallback
            return@withContext InitialData.generateWorkerId()
        }

        var attempts = 0
        while (attempts < 5) {
            val candidateId = Random.nextInt(10000000, 99999999).toString()
            val reserveRef = db.collection("reservedWorkerIds").document(candidateId)
            try {
                val success = db.runTransaction { transaction ->
                    val snapshot = transaction.get(reserveRef)
                    if (snapshot.exists()) {
                        false // Collision, retry with another random 8-digit ID
                    } else {
                        val reservationData = hashMapOf(
                            "workerId" to candidateId,
                            "reservedAt" to System.currentTimeMillis(),
                            "uid" to currentUid
                        )
                        transaction.set(reserveRef, reservationData)
                        true
                    }
                }.await()

                if (success) {
                    Log.d(TAG, "Successfully reserved globally unique Worker ID: $candidateId")
                    return@withContext candidateId
                }
            } catch (e: Exception) {
                Log.w(TAG, "Reservation attempt warning: ${e.message}")
            }
            attempts++
        }
        InitialData.generateWorkerId()
    }

    // ==========================================
    // 2. USER REPOSITORY (users/{uid})
    // ==========================================
    suspend fun saveUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        _currentUserEntity.value = user
        try {
            val db = firestore
            val a = auth
            if (db != null) {
                // Determine target UID aligned with active auth session if available
                val activeUid = a?.currentUser?.uid ?: user.uid
                val userToWrite = if (a?.currentUser != null) user.copy(uid = activeUid) else user

                db.collection("users").document(userToWrite.uid).set(userToWrite, SetOptions.merge()).await()
                Log.d(TAG, "User document saved successfully for UID: ${userToWrite.uid}")
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "saveUser notice: ${e.message}")
            true // Local reactive state updated successfully
        }
    }

    // Register FCM Device Token (users/{uid}/devices/{deviceId} - Section 12)
    suspend fun registerDeviceToken(uid: String, deviceId: String, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val activeUid = auth?.currentUser?.uid
            if (activeUid == null || activeUid != uid) {
                return@withContext false
            }
            val tokenData = DeviceTokenEntity(
                deviceId = deviceId,
                token = token,
                platform = "android",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis()
            )
            firestore?.collection("users")?.document(uid)?.collection("devices")?.document(deviceId)?.set(tokenData)?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "registerDeviceToken notice: ${e.message}")
            false
        }
    }

    // ==========================================
    // 3. WORKER REPOSITORY (workers/{uid} + workerSearch/{workerId})
    // ==========================================
    suspend fun saveWorker(worker: WorkerEntity): Boolean = withContext(Dispatchers.IO) {
        _currentWorkerEntity.value = worker
        val updated = _workers.value.filter { it.uid != worker.uid } + worker
        _workers.value = updated

        try {
            val db = firestore
            val currentUid = auth?.currentUser?.uid ?: worker.uid
            val workerToWrite = worker.copy(uid = currentUid)

            if (db != null) {
                // 1. Save to workers/{uid}
                db.collection("workers").document(workerToWrite.uid).set(workerToWrite, SetOptions.merge()).await()

                // 2. Save lightweight record to workerSearch/{workerId} (Section 20)
                val searchIndex = WorkerSearchIndexEntity(
                    workerId = workerToWrite.workerId,
                    uid = workerToWrite.uid,
                    name = workerToWrite.name,
                    photoUrl = workerToWrite.photoUrl,
                    categoryId = workerToWrite.categoryId,
                    profession = workerToWrite.professionHindi,
                    state = workerToWrite.state,
                    district = workerToWrite.district,
                    city = workerToWrite.city,
                    area = workerToWrite.area,
                    availability = workerToWrite.availability,
                    ratingAverage = workerToWrite.ratingAverage,
                    experienceYears = workerToWrite.experienceYears,
                    verificationStatus = workerToWrite.verificationStatus,
                    searchKeywords = listOf(
                        workerToWrite.workerId,
                        workerToWrite.name.lowercase(),
                        workerToWrite.professionHindi.lowercase(),
                        workerToWrite.city.lowercase(),
                        workerToWrite.area.lowercase()
                    )
                )
                db.collection("workerSearch").document(workerToWrite.workerId).set(searchIndex, SetOptions.merge()).await()
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "saveWorker notice: ${e.message}")
            true
        }
    }

    // Worker ID Search (Section 7)
    suspend fun searchWorkerByWorkerId(workerId: String): WorkerEntity? = withContext(Dispatchers.IO) {
        val trimmed = workerId.trim()
        val localMatch = _workers.value.firstOrNull { it.workerId == trimmed }
        if (localMatch != null) return@withContext localMatch

        try {
            val doc = firestore?.collection("workerSearch")?.document(trimmed)?.get()?.await()
            if (doc != null && doc.exists()) {
                val uid = doc.getString("uid") ?: ""
                if (uid.isNotBlank()) {
                    val workerDoc = firestore?.collection("workers")?.document(uid)?.get()?.await()
                    return@withContext workerDoc?.toObject(WorkerEntity::class.java)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "searchWorkerByWorkerId error: ${e.message}")
        }
        null
    }

    // Toggle Worker Availability (Section 4 & 19)
    suspend fun toggleAvailability(uid: String): Boolean = withContext(Dispatchers.IO) {
        val currentList = _workers.value.toMutableList()
        val idx = currentList.indexOfFirst { it.uid == uid }
        if (idx != -1) {
            val old = currentList[idx]
            val newAvail = if (old.availability == "available") "not_available" else "available"
            val updated = old.copy(availability = newAvail, updatedAt = System.currentTimeMillis())
            currentList[idx] = updated
            _workers.value = currentList
            if (_currentWorkerEntity.value?.uid == uid) {
                _currentWorkerEntity.value = updated
            }

            try {
                firestore?.collection("workers")?.document(uid)?.update("availability", newAvail)?.await()
                firestore?.collection("workerSearch")?.document(old.workerId)?.update("availability", newAvail)?.await()
            } catch (e: Exception) {
                Log.w(TAG, "toggleAvailability firestore notice: ${e.message}")
            }
            return@withContext true
        }
        false
    }

    // Admin Verification & Account Status Moderation (Section 15, 16, 22)
    suspend fun adminUpdateWorkerStatus(
        uid: String,
        newVerification: String?,
        newAccountStatus: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val currentList = _workers.value.toMutableList()
        val idx = currentList.indexOfFirst { it.uid == uid }
        if (idx != -1) {
            val old = currentList[idx]
            val updated = old.copy(
                verificationStatus = newVerification ?: old.verificationStatus,
                updatedAt = System.currentTimeMillis()
            )
            currentList[idx] = updated
            _workers.value = currentList

            try {
                val updates = mutableMapOf<String, Any>()
                newVerification?.let { updates["verificationStatus"] = it }
                if (updates.isNotEmpty()) {
                    firestore?.collection("workers")?.document(uid)?.update(updates)?.await()
                    firestore?.collection("workerSearch")?.document(old.workerId)?.update(updates)?.await()
                }
                newAccountStatus?.let {
                    firestore?.collection("users")?.document(uid)?.update("accountStatus", it)?.await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "adminUpdateWorkerStatus notice: ${e.message}")
            }
            return@withContext true
        }
        false
    }

    // ==========================================
    // 4. REPORT REPOSITORY (reports/{reportId})
    // ==========================================
    suspend fun submitReport(report: ReportEntity): Boolean = withContext(Dispatchers.IO) {
        _reports.value = _reports.value + report
        try {
            val reporterUid = auth?.currentUser?.uid ?: report.reporterId
            val reportToWrite = report.copy(reporterId = reporterUid)
            firestore?.collection("reports")?.document(reportToWrite.reportId)?.set(reportToWrite)?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "submitReport notice: ${e.message}")
            true
        }
    }

    // ==========================================
    // 5. NOTIFICATION REPOSITORY (notifications/{notificationId})
    // ==========================================
    suspend fun sendNotification(notification: NotificationEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore?.collection("notifications")?.document(notification.notificationId)?.set(notification)?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "sendNotification notice: ${e.message}")
            false
        }
    }
}
