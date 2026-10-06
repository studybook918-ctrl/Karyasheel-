package com.example.model

/**
 * PRODUCTION FIREBASE SCHEMA - Sections 2, 3, 4, 5, 12, 13, 20
 */

enum class UserRole {
    worker,
    customer,
    admin
}

enum class AccountStatus {
    active,
    suspended,
    blocked,
    deleted
}

enum class WorkerAvailability {
    available,
    busy,
    not_available
}

enum class VerificationStatus {
    registered,   // Default on signup
    verified,     // Platform Verified
    trusted,      // Proven Platform Track Record
    suspended
}

enum class NotificationType {
    new_job,
    job_request,
    job_accepted,
    job_rejected,
    new_message,
    review_received,
    profile_verified,
    account_warning,
    system_notification
}

// 1. users/{uid}
data class UserEntity(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "worker", // worker, customer, admin
    val language: String = "hi", // hi, en
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val area: String = "",
    val accountStatus: String = "active", // active, suspended, blocked, deleted
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

// 2. workers/{uid}
data class WorkerEntity(
    val uid: String = "",
    val workerId: String = "", // Exactly 8 numeric digits
    val name: String = "",
    val photoUrl: String = "",
    val categoryId: String = "",
    val categoryNameHindi: String = "",
    val categoryNameEnglish: String = "",
    val professionHindi: String = "",
    val professionEnglish: String = "",
    val experienceYears: Int = 0,
    val skills: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val about: String = "",
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val area: String = "",
    val availability: String = "available", // available, busy, not_available
    val verificationStatus: String = "registered", // registered, verified, trusted, suspended
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val completedJobsCount: Int = 0,
    val portfolioImageUrls: List<String> = emptyList(),
    val profileCompletionPercent: Int = 85,
    val phoneNumber: String = "",
    val startingPrice: String = "₹250",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

// 3. customers/{uid}
data class CustomerEntity(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val area: String = "",
    val preferredLanguage: String = "hi",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 4. workerSearch/{workerId} (Lightweight public search index - Section 20)
data class WorkerSearchIndexEntity(
    val workerId: String = "",
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val categoryId: String = "",
    val profession: String = "",
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val area: String = "",
    val availability: String = "available",
    val ratingAverage: Double = 0.0,
    val experienceYears: Int = 0,
    val verificationStatus: String = "registered",
    val searchKeywords: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 5. categories/{categoryId}
data class CategoryEntity(
    val categoryId: String = "",
    val nameHindi: String = "",
    val nameEnglish: String = "",
    val iconEmoji: String = "",
    val colorHex: Long = 0xFFE65100,
    val descriptionHindi: String = "",
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)

// 6. notifications/{notificationId}
data class NotificationEntity(
    val notificationId: String = "",
    val userId: String = "",
    val type: String = "system_notification",
    val title: String = "",
    val body: String = "",
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 7. reports/{reportId}
data class ReportEntity(
    val reportId: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val reportedWorkerName: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = "open", // open, under_review, resolved, rejected
    val createdAt: Long = System.currentTimeMillis()
)

// 8. users/{uid}/devices/{deviceId}
data class DeviceTokenEntity(
    val deviceId: String = "",
    val token: String = "",
    val platform: String = "android",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)
