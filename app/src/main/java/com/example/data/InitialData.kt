package com.example.data

import com.example.model.AccountStatus
import com.example.model.CategoryEntity
import com.example.model.VerificationStatus
import com.example.model.WorkerEntity
import kotlin.random.Random

object InitialData {
    val categories = listOf(
        CategoryEntity("cat_electrician", "बिजली का काम (इलेक्ट्रीशियन)", "Electrician", "⚡", 0xFFE65100, "पंखे, वायरिंग, स्विच, बोर्ड, इन्वर्टर रिपेयर"),
        CategoryEntity("cat_plumber", "नल व पानी का काम (प्लंबर)", "Plumber", "🚰", 0xFF0284C7, "पाइप फिटिंग, लीकेज, नल, सिंक, मोटर रिपेयर"),
        CategoryEntity("cat_carpenter", "बढ़ई का काम (कारपेंटर)", "Carpenter", "🪚", 0xFFB45309, "दरवाजे, अलमारी, खिड़कियां, बेड, फर्नीचर मरम्मत"),
        CategoryEntity("cat_painter", "रंग-रोगन (पेंटर)", "Painter", "🎨", 0xFF7C3AED, "दीवार पेंटिंग, पुट्टी, डिस्टेंपर, वॉटरप्रूफिंग"),
        CategoryEntity("cat_mason", "राजमिस्त्री (चिनाई/प्लास्टर)", "Mason", "🧱", 0xFF9A3412, "ईंट चिनाई, प्लास्टर, टाइल्स, फर्श, छत मरम्मत"),
        CategoryEntity("cat_driver", "गाड़ी ड्राइवर (चार पहिया/ऑटो)", "Driver", "🚗", 0xFF0D9488, "कार ड्राइवर, कमर्शियल वाहन, डिलीवरी, स्थानीय ट्रिप"),
        CategoryEntity("cat_ac", "AC / कूलर तकनीशियन", "AC Technician", "❄️", 0xFF2563EB, "AC सर्विसिंग, गैस रिफिल, कूलर रिपेयर, इंस्टॉलेशन"),
        CategoryEntity("cat_mechanic", "ऑटो मिस्त्री (बाइक/कार)", "Mechanic", "🔧", 0xFF475569, "टू-व्हीलर, कार सर्विस, पंक्चर, इंजन रिपेयर"),
        CategoryEntity("cat_welder", "वेल्डर (लोहे का काम)", "Welder", "🔩", 0xFFD97706, "गेट, ग्रिल, रेलिंग, वेल्डिंग, लोहे की शीट"),
        CategoryEntity("cat_cleaner", "सफाई कर्मी (क्लीनर)", "Cleaner", "🧹", 0xFF059669, "घर की गहरी सफाई, बाथरूम सफाई, ऑफिस क्लीनिंग"),
        CategoryEntity("cat_tailor", "दर्जी (सिलाई का काम)", "Tailor", "👕", 0xFFBE185D, "सूट, शर्ट-पैंट, ब्लाउज, अल्टरेशन, पर्दे"),
        CategoryEntity("cat_beautician", "ब्यूटीशियन / नाई", "Beautician", "💇", 0xFFDB2777, "हेयरकट, फेशियल, वैक्सिंग, ग्रूमिंग सर्विस")
    )

    fun generateWorkerId(): String {
        val num = Random.nextInt(10000000, 99999999)
        return num.toString()
    }

    val sampleWorkers = listOf(
        WorkerEntity(
            uid = "worker_1",
            workerId = "48271635",
            name = "राज कुमार (Raj Kumar)",
            categoryId = "cat_electrician",
            categoryNameHindi = "बिजली का काम",
            categoryNameEnglish = "Electrician",
            professionHindi = "इलेक्ट्रीशियन (बिजली मिस्त्री)",
            professionEnglish = "Electrician",
            experienceYears = 7,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "सिविल लाइंस (Civil Lines)",
            services = listOf("घरेलू वायरिंग", "पंखे लगाना", "स्विच बोर्ड मरम्मत", "इन्वर्टर कनेक्शन"),
            skills = listOf("वायरिंग", "पंखे लगाना", "स्विच बोर्ड मरम्मत"),
            about = "7 वर्षों से प्रयागराज में भरोसेमंद बिजली का काम कर रहा हूँ। उचित दाम और सुरक्षित काम की गारंटी।",
            availability = "available",
            verificationStatus = "verified",
            ratingAverage = 4.9,
            ratingCount = 38,
            completedJobsCount = 29,
            phoneNumber = "+91 98765 43210",
            startingPrice = "₹250"
        ),
        WorkerEntity(
            uid = "worker_2",
            workerId = "82940173",
            name = "रमेश शर्मा (Ramesh Sharma)",
            categoryId = "cat_plumber",
            categoryNameHindi = "नल व पानी का काम",
            categoryNameEnglish = "Plumber",
            professionHindi = "प्लंबर (नल मिस्त्री)",
            professionEnglish = "Plumber",
            experienceYears = 5,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "कटरा (Katra)",
            services = listOf("पाइप लीकेज ठीक करना", "नल रिप्लेसमेंट", "गीजर फिटिंग", "पानी की टंकी सफाई"),
            skills = listOf("पाइप लीकेज", "नल फिटिंग"),
            about = "कटरा और आसपास के क्षेत्रों में 5 साल का अनुभव। आपातकालीन प्लंबिंग में त्वरित सेवा।",
            availability = "available",
            verificationStatus = "registered",
            ratingAverage = 4.7,
            ratingCount = 22,
            completedJobsCount = 18,
            phoneNumber = "+91 98765 11223",
            startingPrice = "₹200"
        ),
        WorkerEntity(
            uid = "worker_3",
            workerId = "61938472",
            name = "मोहित बढ़ई (Mohit Vishwakarma)",
            categoryId = "cat_carpenter",
            categoryNameHindi = "बढ़ई का काम",
            categoryNameEnglish = "Carpenter",
            professionHindi = "बढ़ई (कारपेंटर)",
            professionEnglish = "Carpenter",
            experienceYears = 9,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "धूमनगंज (Dhoomanganj)",
            services = listOf("दरवाजे की चौखट", "मॉड्यूलर किचन मरम्मत", "अलमारी फिटिंग", "ताले लगाना"),
            skills = listOf("दरवाजे", "अलमारी", "फर्नीचर मरम्मत"),
            about = "लकड़ी और प्लाईवुड का उत्कृष्ट कार्य। उचित मूल्य और समय पर कार्य समापन।",
            availability = "available",
            verificationStatus = "trusted",
            ratingAverage = 5.0,
            ratingCount = 45,
            completedJobsCount = 42,
            phoneNumber = "+91 98765 99887",
            startingPrice = "₹350"
        ),
        WorkerEntity(
            uid = "worker_4",
            workerId = "30294819",
            name = "दिनेश पेंटर (Dinesh Kumar)",
            categoryId = "cat_painter",
            categoryNameHindi = "रंग-रोगन",
            categoryNameEnglish = "Painter",
            professionHindi = "पेंटर (रंग-रोगन)",
            professionEnglish = "Painter",
            experienceYears = 6,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "अल्लापुर (Allapur)",
            services = listOf("एशियन पेंट्स पुट्टी", "रॉयल शाइन पेंट", "दीवार सीलन वॉटरप्रूफिंग", "टेक्सचर पेंट"),
            skills = listOf("पुट्टी", "पेंटिंग", "वॉटरप्रूफिंग"),
            about = "मकान और दुकानों की सफाई के साथ सुंदर पेंटिंग का विश्वसनीय कारीगर।",
            availability = "not_available",
            verificationStatus = "verified",
            ratingAverage = 4.8,
            ratingCount = 19,
            completedJobsCount = 15,
            phoneNumber = "+91 98765 66778",
            startingPrice = "₹400"
        ),
        WorkerEntity(
            uid = "worker_5",
            workerId = "73928104",
            name = "सुरेश मिस्त्री (Suresh Rawat)",
            categoryId = "cat_ac",
            categoryNameHindi = "AC / कूलर",
            categoryNameEnglish = "AC Technician",
            professionHindi = "AC / कूलर मैकेनिक",
            professionEnglish = "AC Technician",
            experienceYears = 4,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "झूंसी (Jhunsi)",
            services = listOf("AC फोम जेट सर्विस", "गैस चार्जिंग R32/R410", "कूलर मोटर रिपेयर", "PCB फॉल्ट"),
            skills = listOf("AC सर्विसिंग", "गैस चार्जिंग"),
            about = "स्प्लिट और विंडो AC की सभी समस्याओं का तुरंत समाधान।",
            availability = "available",
            verificationStatus = "verified",
            ratingAverage = 4.8,
            ratingCount = 31,
            completedJobsCount = 25,
            phoneNumber = "+91 98765 33445",
            startingPrice = "₹300"
        ),
        WorkerEntity(
            uid = "worker_6",
            workerId = "91827364",
            name = "विजय ड्राइवर (Vijay Yadav)",
            categoryId = "cat_driver",
            categoryNameHindi = "ड्राइवर",
            categoryNameEnglish = "Driver",
            professionHindi = "अनुभवी कार ड्राइवर",
            professionEnglish = "Driver",
            experienceYears = 8,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "प्रयागराज (Prayagraj)",
            city = "प्रयागराज (Prayagraj)",
            area = "नैनी (Naini)",
            services = listOf("आउटस्टेशन ट्रिप", "डेली ऑफिस पिक-ड्रॉप", "मैनुअल व ऑटोमैटिक कार", "स्थानीय शहर"),
            skills = listOf("कार ड्राइविंग", "हाईवे ड्राइविंग"),
            about = "वैध कमर्शियल लाइसेंस, शांत स्वभाव, शहर और हाईवे का संपूर्ण ज्ञान।",
            availability = "available",
            verificationStatus = "trusted",
            ratingAverage = 4.9,
            ratingCount = 52,
            completedJobsCount = 48,
            phoneNumber = "+91 98765 88990",
            startingPrice = "₹500"
        )
    )

    val indianStatesWithCities = mapOf(
        "उत्तर प्रदेश (Uttar Pradesh)" to listOf("प्रयागराज (Prayagraj)", "वाराणसी (Varanasi)", "लखनऊ (Lucknow)", "कानपुर (Kanpur)", "गोरखपुर (Gorakhpur)", "आगरा (Agra)", "नोएडा (Noida)"),
        "बिहार (Bihar)" to listOf("पटना (Patna)", "गया (Gaya)", "मुजफ्फरपुर (Muzaffarpur)", "भागलपुर (Bhagalpur)", "दरभंगा (Darbhanga)"),
        "मध्य प्रदेश (Madhya Pradesh)" to listOf("भोपाल (Bhopal)", "इंदौर (Indore)", "जबलपुर (Jabalpur)", "ग्वालियर (Gwalior)", "उज्जैन (Ujjain)"),
        "राजस्थान (Rajasthan)" to listOf("जयपुर (Jaipur)", "जोधपुर (Jodhpur)", "कोटा (Kota)", "उदयपुर (Udaipur)", "बीकानेर (Bikaner)"),
        "दिल्ली NCR (Delhi NCR)" to listOf("नई दिल्ली (New Delhi)", "द्वारका (Dwarka)", "रोहिणी (Rohini)", "गाजियाबाद (Ghaziabad)", "गुरुग्राम (Gurugram)")
    )
}
