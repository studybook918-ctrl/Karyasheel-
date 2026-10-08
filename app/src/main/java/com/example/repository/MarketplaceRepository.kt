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
 * REPOSITORY LAYER - Master PRD & Real Firebase Integration
 * UI -> ViewModel -> Repository -> Firebase Services (Auth, Firestore, FCM)
 * Uses authoritative FirebaseAuth state and avoids unprovisioned anonymous sessions.
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

            // If user is already logged in on cold start, fetch their profile
            auth?.currentUser?.let { fbUser ->
                CoroutineScope(Dispatchers.IO).launch {
                    fetchUserProfile(fbUser.uid)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase init info: ${e.message}")
        }

        // Seed default categories
        _categories.value = InitialData.categories

        // Seed initial workers in repository memory
        _workers.value = InitialData.sampleWorkers
    }

    fun getCurrentAuthUid(): String? = auth?.currentUser?.uid

    // ==========================================
    // 1. TRANSACTION-SAFE 8-DIGIT WORKER ID GENERATOR (PRD Section 11)
    // ==========================================
    suspend fun generateAndReserveUniqueWorkerId(): String = withContext(Dispatchers.IO) {
        val db = firestore
        val currentUid = auth?.currentUser?.uid
        if (db == null || currentUid == null) {
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
    // Preserves existing createdAt and updates lastActiveAt/updatedAt
    // ==========================================
    suspend fun fetchUserProfile(uid: String): UserEntity? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
            if (doc != null && doc.exists()) {
                val entity = doc.toObject(UserEntity::class.java)
                _currentUserEntity.value = entity
                return@withContext entity
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchUserProfile error: ${e.message}")
        }
        null
    }

    suspend fun saveUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        _currentUserEntity.value = user
        try {
            val db = firestore
            val a = auth
            if (db != null) {
                val activeUid = a?.currentUser?.uid ?: user.uid
                val userRef = db.collection("users").document(activeUid)

                // Check existing doc to preserve createdAt
                val existingDoc = userRef.get().await()
                val finalUser = if (existingDoc.exists()) {
                    val existingCreatedAt = existingDoc.getLong("createdAt") ?: user.createdAt
                    user.copy(
                        uid = activeUid,
                        createdAt = existingCreatedAt,
                        updatedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )
                } else {
                    user.copy(
                        uid = activeUid,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )
                }

                userRef.set(finalUser, SetOptions.merge()).await()
                _currentUserEntity.value = finalUser
                Log.d(TAG, "User profile saved to Firestore for UID: $activeUid")
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "saveUser notice: ${e.message}")
            true
        }
    }

    // Register FCM Device Token (users/{uid}/devices/{deviceId})
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
                db.collection("workers").document(workerToWrite.uid).set(workerToWrite, SetOptions.merge()).await()

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

    // Worker ID Search
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

    // Toggle Worker Availability
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

    // Admin Verification & Account Status Moderation
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

    // ==========================================
    // 6. WORK REQUEST REPOSITORY (workRequests/{requestId})
    // ==========================================
    private val _workRequests = MutableStateFlow<List<WorkRequestEntity>>(emptyList())
    val workRequests: StateFlow<List<WorkRequestEntity>> = _workRequests.asStateFlow()

    suspend fun createWorkRequest(request: WorkRequestEntity): Boolean = withContext(Dispatchers.IO) {
        _workRequests.value = _workRequests.value + request
        try {
            firestore?.collection("workRequests")?.document(request.requestId)?.set(request, SetOptions.merge())?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "createWorkRequest notice: ${e.message}")
            true
        }
    }

    suspend fun updateWorkRequestStatus(requestId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val list = _workRequests.value.toMutableList()
        val idx = list.indexOfFirst { it.requestId == requestId }
        if (idx != -1) {
            list[idx] = list[idx].copy(status = newStatus)
            _workRequests.value = list
            try {
                firestore?.collection("workRequests")?.document(requestId)?.update("status", newStatus)?.await()
            } catch (e: Exception) {
                Log.w(TAG, "updateWorkRequestStatus notice: ${e.message}")
            }
            return@withContext true
        }
        false
    }

    // ==========================================
    // 7. CHAT REPOSITORY (connections/{connectionId}/messages)
    // ==========================================
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    suspend fun sendChatMessage(msg: ChatMessageEntity): Boolean = withContext(Dispatchers.IO) {
        _chatMessages.value = _chatMessages.value + msg
        try {
            firestore?.collection("connections")?.document(msg.connectionId)?.collection("messages")?.document(msg.messageId)?.set(msg)?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "sendChatMessage notice: ${e.message}")
            true
        }
    }

    // ==========================================
    // 8. CONTRACTOR TEAM REPOSITORY (contractorTeams/{contractorUid}/members)
    // ==========================================
    private val _contractorTeam = MutableStateFlow<List<ContractorTeamMember>>(emptyList())
    val contractorTeam: StateFlow<List<ContractorTeamMember>> = _contractorTeam.asStateFlow()

    suspend fun addWorkerToTeam(member: ContractorTeamMember): Boolean = withContext(Dispatchers.IO) {
        _contractorTeam.value = _contractorTeam.value + member
        try {
            firestore?.collection("contractorTeams")?.document(member.contractorUid)?.collection("members")?.document(member.memberId)?.set(member, SetOptions.merge())?.await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "addWorkerToTeam notice: ${e.message}")
            true
        }
    }
}
