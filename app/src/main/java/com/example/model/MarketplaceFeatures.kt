package com.example.model

/**
 * MASTER PRD SECTIONS 4, 5, 6, 7, 8, 9, 10
 * 5 Main User Types / Categories:
 * 1. 🔧 Skilled Worker
 * 2. 👷 General Worker / Multi-Work
 * 3. 👨‍💼 Professional (Teacher, Tutor, Lawyer, Developer, Accountant, etc.)
 * 4. 🏢 Business / Company / Self-Employed
 * 5. 👷‍♂️ Contractor (Construction, Electrical, Plumbing, Painting, Civil, Labour)
 */
enum class MainCategoryType(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val iconEmoji: String,
    val descriptionHindi: String
) {
    SKILLED_WORKER(
        "cat_skilled",
        "कुशल कामगार",
        "Skilled Worker",
        "🔧",
        "इलेक्ट्रीशियन, प्लंबर, बढ़ई, पेंटर, मिस्त्री, वेल्डर, AC रिपेयर आदि"
    ),
    GENERAL_WORKER(
        "cat_general",
        "सामान्य कामगार",
        "General Worker",
        "👷",
        "हेल्पर, लोडिंग/अनलोडिंग, पैकिंग, सफाई, निर्माण हेल्पर, लेबर"
    ),
    PROFESSIONAL(
        "cat_professional",
        "प्रोफेशनल्स",
        "Professional",
        "👨‍💼",
        "शिक्षक, ट्यूटर, वकील, सॉफ्टवेयर डेवलपर, अकाउंटेंट, इंजीनियर, डिजाइनर"
    ),
    BUSINESS(
        "cat_business",
        "व्यवसाय / कंपनी",
        "Business / Company",
        "🏢",
        "दुकानदार, सर्विस बिजनेस, स्थानीय व्यापार, एजेंसी, सेल्फ-एम्प्लॉयड"
    ),
    CONTRACTOR(
        "cat_contractor",
        "कांट्रैक्टर / ठेकेदार",
        "Contractor",
        "👷‍♂️",
        "कंस्ट्रक्शन, इलेक्ट्रिकल, प्लंबिंग, पेंटिंग, लेबर, सिविल ठेकेदार"
    )
}

data class ProfessionItem(
    val id: String,
    val mainCategoryType: MainCategoryType,
    val nameHindi: String,
    val nameEnglish: String,
    val iconEmoji: String,
    val descriptionHindi: String
)

data class WorkRequestEntity(
    val requestId: String = "",
    val customerUid: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val workerUid: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workType: String = "",
    val workDescription: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val budget: String = "",
    val numberOfWorkers: String = "1",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessageEntity(
    val messageId: String = "",
    val connectionId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ContractorTeamMember(
    val memberId: String = "",
    val contractorUid: String = "",
    val workerUid: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val professionHindi: String = "",
    val city: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED
    val addedAt: Long = System.currentTimeMillis()
)
