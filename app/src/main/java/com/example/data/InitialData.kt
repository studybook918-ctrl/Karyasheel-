package com.example.data

import com.example.model.*
import kotlin.random.Random

object InitialData {

    // PRD Section 5, 6, 7, 8, 9: Sub-professions mapped to 5 Main Categories
    val professionItems = listOf(
        // 1. 🔧 SKILLED WORKER
        ProfessionItem("prof_elec", MainCategoryType.SKILLED_WORKER, "इलेक्ट्रीशियन", "Electrician", "⚡", "घरेलू वायरिंग, पंखे, स्विच बोर्ड, इन्वर्टर"),
        ProfessionItem("prof_plumb", MainCategoryType.SKILLED_WORKER, "प्लंबर", "Plumber", "🚰", "नल फिटिंग, पाइप लीकेज, मोटर, सिंक रिपेयर"),
        ProfessionItem("prof_carp", MainCategoryType.SKILLED_WORKER, "बढ़ई (कारपेंटर)", "Carpenter", "🪚", "दरवाजे, अलमारी, खिड़कियां, फर्नीचर"),
        ProfessionItem("prof_paint", MainCategoryType.SKILLED_WORKER, "पेंटर (रंग-रोगन)", "Painter", "🎨", "दीवार पेंटिंग, पुट्टी, डिस्टेंपर, वॉटरप्रूफिंग"),
        ProfessionItem("prof_mason", MainCategoryType.SKILLED_WORKER, "राजमिस्त्री", "Mason", "🧱", "ईंट चिनाई, प्लास्टर, टाइल्स, फर्श, छत"),
        ProfessionItem("prof_ac", MainCategoryType.SKILLED_WORKER, "AC तकनीशियन", "AC Technician", "❄️", "AC सर्विसिंग, गैस रिफिल, कूलर रिपेयर"),
        ProfessionItem("prof_mech", MainCategoryType.SKILLED_WORKER, "मैकेनिक", "Mechanic", "🔧", "बाइक/कार सर्विस, इंजन, पंक्चर"),
        ProfessionItem("prof_weld", MainCategoryType.SKILLED_WORKER, "वेल्डर", "Welder", "🔩", "गेट, ग्रिल, रेलिंग, लोहे की वेल्डिंग"),
        ProfessionItem("prof_mob", MainCategoryType.SKILLED_WORKER, "मोबाइल रिपेयर", "Mobile Repair", "📱", "स्क्रीन, बैटरी, चार्जिंग पोर्ट, सॉफ्टवेयर"),
        ProfessionItem("prof_driver", MainCategoryType.SKILLED_WORKER, "ड्राइवर", "Driver", "🚗", "कार ड्राइविंग, लोकल व लंबी दूरी की यात्राएं"),
        ProfessionItem("prof_tailor", MainCategoryType.SKILLED_WORKER, "दर्जी (सिलाई)", "Tailor", "🧵", "सूट, शर्ट-पैंट, ब्लाउज, अल्टरेशन"),
        ProfessionItem("prof_beauty", MainCategoryType.SKILLED_WORKER, "ब्यूटीशियन / नाई", "Beautician", "💇", "हेयरकट, ग्रूमिंग, फेशियल सर्विस"),

        // 2. 👷 GENERAL WORKER / MULTI-WORK
        ProfessionItem("prof_gen_help", MainCategoryType.GENERAL_WORKER, "हेल्पर (Helper)", "Helper", "👷", "घरेलू या दुकान का सहायक कार्य"),
        ProfessionItem("prof_gen_const", MainCategoryType.GENERAL_WORKER, "कंस्ट्रक्शन हेल्पर", "Construction Helper", "🏗️", "मकान निर्माण में सामग्री ढुलाई, मसाला तैयार करना"),
        ProfessionItem("prof_gen_load", MainCategoryType.GENERAL_WORKER, "लोडिंग / अनलोडिंग", "Loading / Unloading", "🚚", "सामान उतारना, चढ़ाना, ट्रांसपोर्ट लेबर"),
        ProfessionItem("prof_gen_pack", MainCategoryType.GENERAL_WORKER, "पैकिंग वर्कर", "Packing Worker", "📦", "सामान पैक करना, वेयरहाउस शिफ्टिंग"),
        ProfessionItem("prof_gen_clean", MainCategoryType.GENERAL_WORKER, "सफाई कर्मी (क्लीनर)", "Cleaning Worker", "🧹", "घर, दुकान व ऑफिस की संपूर्ण सफाई"),
        ProfessionItem("prof_gen_shift", MainCategoryType.GENERAL_WORKER, "शिफ्टिंग हेल्पर", "Shifting Helper", "🪑", "घर या ऑफिस का सामान एक जगह से दूसरी जगह ले जाना"),
        ProfessionItem("prof_gen_any", MainCategoryType.GENERAL_WORKER, "मुझे कोई भी सामान्य काम चाहिए", "General Labour", "🤝", "आज काम के लिए तैयार, दैनिक मजदूरी"),

        // 3. 👨‍💼 PROFESSIONAL (Teachers, Tutors, Lawyers, Tech, Finance)
        ProfessionItem("prof_teach", MainCategoryType.PROFESSIONAL, "शिक्षक (Teacher)", "Teacher", "👨‍🏫", "स्कूल, कॉलेज, कोचिंग शिक्षक"),
        ProfessionItem("prof_tutor", MainCategoryType.PROFESSIONAL, "होम ट्यूटर (Tutor)", "Tutor", "📚", "कक्षा 1 से 12 तक व्यक्तिगत ट्यूशन"),
        ProfessionItem("prof_lawyer", MainCategoryType.PROFESSIONAL, "वकील (Lawyer)", "Lawyer", "⚖️", "कानूनी सलाह, दस्तावेजीकरण, एफिडेविट"),
        ProfessionItem("prof_dev", MainCategoryType.PROFESSIONAL, "सॉफ्टवेयर डेवलपर", "Software Developer", "💻", "वेबसाइट, मोबाइल ऐप, कोडिंग"),
        ProfessionItem("prof_acc", MainCategoryType.PROFESSIONAL, "अकाउंटेंट (Accountant)", "Accountant", "🧮", "जीएसटी, आईटीआर, बहीखाता, टैली"),
        ProfessionItem("prof_eng", MainCategoryType.PROFESSIONAL, "इंजीनियर (Engineer)", "Engineer", "🏗️", "सिविल, मैकेनिकल, इलेक्ट्रिकल योजनाएं"),
        ProfessionItem("prof_des", MainCategoryType.PROFESSIONAL, "डिजाइनर (Designer)", "Designer", "🎨", "ग्राफिक डिजाइन, पोस्टर, लोगो, इंटीरियर"),
        ProfessionItem("prof_digi", MainCategoryType.PROFESSIONAL, "डिजिटल मार्केटर", "Digital Marketer", "📢", "सोशल मीडिया, ऑनलाइन विज्ञापन, प्रचार"),

        // 4. 🏢 BUSINESS / COMPANY / SELF-EMPLOYED
        ProfessionItem("prof_biz_shop", MainCategoryType.BUSINESS, "दुकानदार (Shop Owner)", "Shop Owner", "🏪", "किराना, हार्डवेयर, इलेक्ट्रॉनिक्स, रिटेल दुकान"),
        ProfessionItem("prof_biz_svc", MainCategoryType.BUSINESS, "सर्विस बिजनेस", "Service Business", "🛠️", "रिपेयरिंग सेंटर, सर्विस सेंटर, एजेंसी"),
        ProfessionItem("prof_biz_agency", MainCategoryType.BUSINESS, "एजेंसी / फर्म", "Agency / Firm", "🏢", "सिक्योरिटी गार्ड, मैनपावर, इवेंट एजेंसी"),
        ProfessionItem("prof_biz_free", MainCategoryType.BUSINESS, "स्व-रोजगार / फ्रीलांसर", "Self-Employed", "💼", "स्वतंत्र व्यावसायिक सेवाएं"),

        // 5. 👷‍♂️ CONTRACTOR (Construction, Electrical, Plumbing, Painting, Civil, Labour)
        ProfessionItem("prof_con_const", MainCategoryType.CONTRACTOR, "कंस्ट्रक्शन ठेकेदार", "Construction Contractor", "🏗️", "मकान, बिल्डिंग, नींव, संपूर्ण निर्माण कार्य"),
        ProfessionItem("prof_con_elec", MainCategoryType.CONTRACTOR, "इलेक्ट्रिकल ठेकेदार", "Electrical Contractor", "⚡", "बिल्डिंग व फैक्ट्री संपूर्ण वायरिंग ठेका"),
        ProfessionItem("prof_con_plumb", MainCategoryType.CONTRACTOR, "प्लंबिंग ठेकेदार", "Plumbing Contractor", "🚰", "पाइपलाइन, सीवरेज, वाटर सप्लाई ठेका"),
        ProfessionItem("prof_con_paint", MainCategoryType.CONTRACTOR, "पेंटिंग ठेकेदार", "Painting Contractor", "🎨", "मल्टी-स्टोरी व बड़े प्रोजेक्ट पेंटिंग"),
        ProfessionItem("prof_con_civil", MainCategoryType.CONTRACTOR, "सिविल ठेकेदार", "Civil Contractor", "🏛️", "सड़क, नाली, बाउंड्री वॉल, ढांचा निर्माण"),
        ProfessionItem("prof_con_labour", MainCategoryType.CONTRACTOR, "लेबर ठेकेदार", "Labour Contractor", "👥", "दैनिक व मासिक मजदूर टीम उपलब्ध कराना")
    )

    // Legacy CategoryEntity list kept for backwards-compatibility
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
            categoryId = "cat_skilled",
            categoryNameHindi = "कुशल कामगार",
            categoryNameEnglish = "Skilled Worker",
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
            categoryId = "cat_skilled",
            categoryNameHindi = "कुशल कामगार",
            categoryNameEnglish = "Skilled Worker",
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
            categoryId = "cat_skilled",
            categoryNameHindi = "कुशल कामगार",
            categoryNameEnglish = "Skilled Worker",
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
            categoryId = "cat_skilled",
            categoryNameHindi = "कुशल कामगार",
            categoryNameEnglish = "Skilled Worker",
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
            categoryId = "cat_skilled",
            categoryNameHindi = "कुशल कामगार",
            categoryNameEnglish = "Skilled Worker",
            professionHindi = "AC / कूलर मैकेनिक",
            professionEnglish = "AC Technician",
            experienceYears = 4,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "कुशीनगर (Kushinagar)",
            city = "हाटा (Hata)",
            area = "हाटा बाजार (Hata Bazar)",
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
        // GENERAL WORKER
        WorkerEntity(
            uid = "worker_7",
            workerId = "55443322",
            name = "सोहन लाल (Sohan Lal)",
            categoryId = "cat_general",
            categoryNameHindi = "सामान्य कामगार",
            categoryNameEnglish = "General Worker",
            professionHindi = "लोडिंग व शिफ्टिंग हेल्पर",
            professionEnglish = "Loading & Shifting Helper",
            experienceYears = 3,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "गोरखपुर (Gorakhpur)",
            city = "गोरखपुर (Gorakhpur)",
            area = "गोलघर (Golghar)",
            services = listOf("सामान अनलोडिंग", "घर शिफ्टिंग में मदद", "दुकान सफाई", "भारी वजन उठाना"),
            skills = listOf("लोडिंग", "शिफ्टिंग"),
            about = "मेहनती और समय के पाबंद। आज काम के लिए पूरी तरह उपलब्ध।",
            availability = "available",
            verificationStatus = "registered",
            ratingAverage = 4.7,
            ratingCount = 15,
            completedJobsCount = 14,
            phoneNumber = "+91 98765 44556",
            startingPrice = "₹350/दिन"
        ),
        // PROFESSIONAL (Teacher/Tutor/Lawyer)
        WorkerEntity(
            uid = "worker_8",
            workerId = "77889900",
            name = "अमित वर्मा (Amit Verma)",
            categoryId = "cat_professional",
            categoryNameHindi = "प्रोफेशनल्स",
            categoryNameEnglish = "Professional",
            professionHindi = "गणित व विज्ञान शिक्षक (Tutor)",
            professionEnglish = "Maths & Science Tutor",
            experienceYears = 8,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "लखनऊ (Lucknow)",
            city = "लखनऊ (Lucknow)",
            area = "हजरतगंज (Hazratganj)",
            services = listOf("कक्षा 9-10 बोर्ड तैयारी", "व्यक्तिगत होम ट्यूशन", "ऑनलाइन व ऑफलाइन क्लासेस"),
            skills = listOf("गणित", "भौतिक विज्ञान", "बोर्ड परीक्षा तैयारी"),
            about = "एम.एससी. गोल्ड मेडलिस्ट। सरल उदाहरणों से विषय समझाने में 8 वर्षों का अनुभव।",
            availability = "available",
            verificationStatus = "verified",
            ratingAverage = 5.0,
            ratingCount = 40,
            completedJobsCount = 35,
            phoneNumber = "+91 98765 66779",
            startingPrice = "₹500/घंटा"
        ),
        // CONTRACTOR (Construction)
        WorkerEntity(
            uid = "worker_9",
            workerId = "11223344",
            name = "संजय ठेकेदार (Sanjay Choudhary)",
            categoryId = "cat_contractor",
            categoryNameHindi = "कांट्रैक्टर / ठेकेदार",
            categoryNameEnglish = "Contractor",
            professionHindi = "कंस्ट्रक्शन ठेकेदार (Civil Contractor)",
            professionEnglish = "Civil Contractor",
            experienceYears = 12,
            state = "उत्तर प्रदेश (Uttar Pradesh)",
            district = "कुशीनगर (Kushinagar)",
            city = "कुशीनगर (Kushinagar)",
            area = "कसया (Kasia)",
            services = listOf("मकान निर्माण ठेका", "मजदूर टीम उपलब्ध कराना", "ईंट चिनाई व प्लास्टर", "छत ढलाई"),
            skills = listOf("कंस्ट्रक्शन", "टीम मैनेजमेंट", "सिविल वर्क"),
            about = "15+ कुशल राजमिस्त्री व हेल्परों की समर्पित टीम। समय पर काम पूरा करने का 12 वर्षों का अनुभव।",
            availability = "available",
            verificationStatus = "trusted",
            ratingAverage = 4.9,
            ratingCount = 65,
            completedJobsCount = 50,
            phoneNumber = "+91 98765 77881",
            startingPrice = "अनुबंध अनुसार"
        )
    )

    // Comprehensive Master Location Database: States, Districts & Cities
    val indianStatesWithCities = mapOf(
        "उत्तर प्रदेश (Uttar Pradesh)" to listOf(
            "प्रयागराज (Prayagraj)",
            "कुशीनगर (Kushinagar)",
            "हाटा (Hata)",
            "कसया (Kasia)",
            "गोरखपुर (Gorakhpur)",
            "लखनऊ (Lucknow)",
            "वाराणसी (Varanasi)",
            "कानपुर (Kanpur)",
            "आगरा (Agra)",
            "नोएडा (Noida)",
            "गाजियाबाद (Ghaziabad)",
            "अयोध्या (Ayodhya)",
            "झांसी (Jhansi)",
            "मेरठ (Meerut)",
            "बरेली (Bareilly)"
        ),
        "बिहार (Bihar)" to listOf(
            "पटना (Patna)",
            "गया (Gaya)",
            "मुजफ्फरपुर (Muzaffarpur)",
            "भागलपुर (Bhagalpur)",
            "दरभंगा (Darbhanga)",
            "गोपालगंज (Gopalganj)",
            "सीवान (Siwan)",
            "पूर्णिया (Purnia)"
        ),
        "मध्य प्रदेश (Madhya Pradesh)" to listOf(
            "भोपाल (Bhopal)",
            "इंदौर (Indore)",
            "जबलपुर (Jabalpur)",
            "ग्वालियर (Gwalior)",
            "उज्जैन (Ujjain)",
            "रीवा (Rewa)",
            "सतना (Satna)"
        ),
        "राजस्थान (Rajasthan)" to listOf(
            "जयपुर (Jaipur)",
            "जोधपुर (Jodhpur)",
            "कोटा (Kota)",
            "उदयपुर (Udaipur)",
            "बीकानेर (Bikaner)",
            "अजमेर (Ajmer)"
        ),
        "दिल्ली NCR (Delhi NCR)" to listOf(
            "नई दिल्ली (New Delhi)",
            "द्वारका (Dwarka)",
            "रोहिणी (Rohini)",
            "चांदनी चौक (Chandni Chowk)",
            "कनॉट प्लेस (Connaught Place)",
            "गुरुग्राम (Gurugram)",
            "फरीदाबाद (Faridabad)"
        ),
        "महाराष्ट्र (Maharashtra)" to listOf(
            "मुंबई (Mumbai)",
            "पुणे (Pune)",
            "नागपुर (Nagpur)",
            "नासिक (Nashik)",
            "ठाणे (Thane)",
            "औरंगाबाद (Chhatrapati Sambhajinagar)"
        ),
        "गुजरात (Gujarat)" to listOf(
            "अहमदाबाद (Ahmedabad)",
            "सूरत (Surat)",
            "वडोदरा (Vadodara)",
            "राजकोट (Rajkot)",
            "गांधीनगर (Gandhinagar)"
        ),
        "पश्चिम बंगाल (West Bengal)" to listOf(
            "कोलकाता (Kolkata)",
            "हावड़ा (Howrah)",
            "सिलीगुड़ी (Siliguri)",
            "दुर्गापुर (Durgapur)"
        )
    )
}
