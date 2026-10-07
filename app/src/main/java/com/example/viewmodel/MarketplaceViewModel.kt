package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InitialData
import com.example.model.*
import com.example.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object RoleSelect : Screen()
    object WorkerRegistration : Screen()
    object WorkerDashboard : Screen()
    object CustomerHome : Screen()
    data class WorkerDetail(val worker: WorkerEntity) : Screen()
    data class Chat(val connectionId: String, val otherPartyName: String, val otherPartyId: String) : Screen()
    object ContractorTeam : Screen()
    object AdminDashboard : Screen()
    object PublicQrView : Screen()
}

data class AuthUser(
    val uid: String,
    val name: String,
    val email: String,
    val role: String? = null
)

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MarketplaceRepository(application)

    // Current Authenticated User (Firebase UID as primary internal identity)
    private val _currentUser = MutableStateFlow<AuthUser?>(
        AuthUser("usr_default_raj", "राज कुमार", "raj.kumar@karyasheel.in", null)
    )
    val currentUser = _currentUser.asStateFlow()

    // Navigation Stack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.RoleSelect)
    val currentScreen = _currentScreen.asStateFlow()

    // Worker Registration Form State (Step by Step - Master PRD Section 4 & 10)
    private val _registrationStep = MutableStateFlow(1)
    val registrationStep = _registrationStep.asStateFlow()

    // Selected user type during registration (Skilled, General, Professional, Business, Contractor)
    private val _regUserType = MutableStateFlow(MainCategoryType.SKILLED_WORKER)
    val regUserType = _regUserType.asStateFlow()

    private val _regName = MutableStateFlow("")
    val regName = _regName.asStateFlow()

    private val _regProfessionItem = MutableStateFlow<ProfessionItem?>(null)
    val regProfessionItem = _regProfessionItem.asStateFlow()

    private val _regState = MutableStateFlow("उत्तर प्रदेश (Uttar Pradesh)")
    val regState = _regState.asStateFlow()

    private val _regCity = MutableStateFlow("प्रयागराज (Prayagraj)")
    val regCity = _regCity.asStateFlow()

    private val _regArea = MutableStateFlow("")
    val regArea = _regArea.asStateFlow()

    private val _regExperience = MutableStateFlow(3)
    val regExperience = _regExperience.asStateFlow()

    private val _regPhone = MutableStateFlow("")
    val regPhone = _regPhone.asStateFlow()

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering = _isRegistering.asStateFlow()

    // Worker Profile Entity
    val createdWorker: StateFlow<WorkerEntity?> = repository.currentWorkerEntity

    // Customer Search & Filter States
    // 1. Selected Main Category in Customer Flow (Skilled, General, Professional, Business, Contractor)
    private val _selectedMainCategory = MutableStateFlow<MainCategoryType?>(null)
    val selectedMainCategory = _selectedMainCategory.asStateFlow()

    // 2. Selected Sub-profession filter
    private val _selectedProfessionFilter = MutableStateFlow<String?>(null)
    val selectedProfessionFilter = _selectedProfessionFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _workerIdSearchQuery = MutableStateFlow("")
    val workerIdSearchQuery = _workerIdSearchQuery.asStateFlow()

    private val _searchedWorkerResult = MutableStateFlow<WorkerEntity?>(null)
    val searchedWorkerResult = _searchedWorkerResult.asStateFlow()

    private val _selectedCityFilter = MutableStateFlow("प्रयागराज (Prayagraj)")
    val selectedCityFilter = _selectedCityFilter.asStateFlow()

    private val _availableOnlyFilter = MutableStateFlow(false)
    val availableOnlyFilter = _availableOnlyFilter.asStateFlow()

    // Admin Tabs & Filters
    private val _adminTab = MutableStateFlow(0) // 0: Workers, 1: Reports, 2: Categories
    val adminTab = _adminTab.asStateFlow()

    // Filtered Workers for Customers (All-India capable, matching main category, subcategory, city, query)
    val filteredWorkers: StateFlow<List<WorkerEntity>> = combine(
        repository.workers,
        _searchQuery,
        _selectedMainCategory,
        combine(
            _selectedProfessionFilter,
            _selectedCityFilter,
            _availableOnlyFilter
        ) { prof, city, avail -> Triple(prof, city, avail) }
    ) { workers, query, mainCat, (profFilter, cityFilter, availOnly) ->
        workers.filter { worker ->
            val matchesMainCat = mainCat == null || when (mainCat) {
                MainCategoryType.SKILLED_WORKER -> worker.categoryId.contains("skilled") || worker.categoryId.contains("cat_")
                MainCategoryType.GENERAL_WORKER -> worker.categoryId.contains("general")
                MainCategoryType.PROFESSIONAL -> worker.categoryId.contains("professional")
                MainCategoryType.BUSINESS -> worker.categoryId.contains("business")
                MainCategoryType.CONTRACTOR -> worker.categoryId.contains("contractor")
            }

            val matchesProf = profFilter == null ||
                    worker.professionHindi.contains(profFilter, ignoreCase = true) ||
                    worker.professionEnglish.contains(profFilter, ignoreCase = true) ||
                    worker.skills.any { it.contains(profFilter, ignoreCase = true) }

            // All-India filter: if city is blank or "पूरे भारत में खोजें", matches all
            val matchesCity = cityFilter.isBlank() ||
                    cityFilter == "पूरे भारत में (All India)" ||
                    worker.city.contains(cityFilter, ignoreCase = true) ||
                    worker.state.contains(cityFilter, ignoreCase = true)

            val matchesAvail = !availOnly || worker.availability == "available"

            val matchesQuery = query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.professionHindi.contains(query, ignoreCase = true) ||
                    worker.professionEnglish.contains(query, ignoreCase = true) ||
                    worker.workerId.contains(query) ||
                    worker.services.any { it.contains(query, ignoreCase = true) } ||
                    worker.area.contains(query, ignoreCase = true) ||
                    worker.city.contains(query, ignoreCase = true)

            matchesMainCat && matchesProf && matchesCity && matchesAvail && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectRole(role: String) {
        val user = _currentUser.value?.copy(role = role) ?: AuthUser("usr_${System.currentTimeMillis()}", "नया उपयोगकर्ता", "user@karyasheel.in", role)
        _currentUser.value = user

        viewModelScope.launch {
            val userEntity = UserEntity(
                uid = user.uid,
                name = user.name,
                email = user.email,
                role = role,
                accountStatus = "active"
            )
            repository.saveUser(userEntity)
        }

        when (role) {
            "worker" -> {
                if (repository.currentWorkerEntity.value != null) {
                    _currentScreen.value = Screen.WorkerDashboard
                } else {
                    _registrationStep.value = 1
                    _currentScreen.value = Screen.WorkerRegistration
                }
            }
            "customer" -> {
                _currentScreen.value = Screen.CustomerHome
            }
            "admin" -> {
                _currentScreen.value = Screen.AdminDashboard
            }
        }
    }

    fun setRegistrationStep(step: Int) { _registrationStep.value = step }
    fun setRegUserType(type: MainCategoryType) {
        _regUserType.value = type
        _regProfessionItem.value = null
    }
    fun updateRegName(name: String) { _regName.value = name }
    fun updateRegProfessionItem(item: ProfessionItem) { _regProfessionItem.value = item }
    fun updateRegState(state: String) { _regState.value = state }
    fun updateRegCity(city: String) { _regCity.value = city }
    fun updateRegArea(area: String) { _regArea.value = area }
    fun updateRegExperience(exp: Int) { _regExperience.value = exp }
    fun updateRegPhone(phone: String) { _regPhone.value = phone }

    // Complete Worker Registration with Atomic Transaction-safe 8-digit Worker ID
    fun completeWorkerRegistration() {
        viewModelScope.launch {
            _isRegistering.value = true
            val generated8DigitId = repository.generateAndReserveUniqueWorkerId()
            val profItem = _regProfessionItem.value
            val profNameHindi = profItem?.nameHindi ?: "कुशल कारीगर"
            val profNameEng = profItem?.nameEnglish ?: "Skilled Worker"
            val catType = _regUserType.value
            val uid = _currentUser.value?.uid ?: "worker_${System.currentTimeMillis()}"

            val worker = WorkerEntity(
                uid = uid,
                workerId = generated8DigitId,
                name = _regName.value.ifBlank { "कार्यशील कामगार" },
                categoryId = catType.id,
                categoryNameHindi = catType.titleHindi,
                categoryNameEnglish = catType.titleEnglish,
                professionHindi = profNameHindi,
                professionEnglish = profNameEng,
                experienceYears = _regExperience.value,
                state = _regState.value,
                district = _regCity.value,
                city = _regCity.value,
                area = _regArea.value.ifBlank { "स्थानीय क्षेत्र" },
                services = listOf("बुनियादी सेवा", "आपातकालीन कार्य", "नियमित मरम्मत"),
                skills = listOf(profNameHindi, "समयबद्ध कार्य"),
                about = "${_regExperience.value} वर्षों के अनुभव के साथ $profNameHindi में समर्पित सेवा।",
                availability = "available",
                verificationStatus = "registered",
                phoneNumber = _regPhone.value.ifBlank { "+91 98000 12345" },
                startingPrice = "₹250",
                ratingAverage = 0.0,
                ratingCount = 0,
                completedJobsCount = 0
            )

            repository.saveWorker(worker)
            _isRegistering.value = false
            _currentScreen.value = Screen.WorkerDashboard
        }
    }

    fun toggleAvailability() {
        val currentUid = repository.currentWorkerEntity.value?.uid ?: "worker_1"
        viewModelScope.launch {
            repository.toggleAvailability(currentUid)
        }
    }

    // Customer filters
    fun setSelectedMainCategory(mainCategoryType: MainCategoryType?) {
        _selectedMainCategory.value = mainCategoryType
        _selectedProfessionFilter.value = null
    }

    fun setSelectedProfessionFilter(prof: String?) {
        _selectedProfessionFilter.value = prof
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCityFilter(city: String) { _selectedCityFilter.value = city }
    fun toggleAvailableOnly() { _availableOnlyFilter.value = !_availableOnlyFilter.value }

    // Search by 8-Digit Worker ID (PRD Section 7 & 11)
    fun searchByWorkerId(id: String) {
        _workerIdSearchQuery.value = id
        if (id.length == 8) {
            viewModelScope.launch {
                val match = repository.searchWorkerByWorkerId(id)
                _searchedWorkerResult.value = match
            }
        } else {
            _searchedWorkerResult.value = null
        }
    }

    // Work Requests (PRD Section 19 & 20)
    val workRequests: StateFlow<List<WorkRequestEntity>> = repository.workRequests

    fun sendWorkRequest(
        worker: WorkerEntity,
        workType: String,
        workDesc: String,
        location: String,
        date: String,
        budget: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val request = WorkRequestEntity(
                requestId = "req_${System.currentTimeMillis()}",
                customerUid = user?.uid ?: "cust_${System.currentTimeMillis()}",
                customerName = user?.name ?: "ग्राहक",
                customerPhone = "9876543210",
                workerUid = worker.uid,
                workerId = worker.workerId,
                workerName = worker.name,
                workType = workType.ifBlank { worker.professionHindi },
                workDescription = workDesc,
                location = location.ifBlank { "${worker.area}, ${worker.city}" },
                date = date.ifBlank { "कल (शीघ्र)" },
                budget = budget.ifBlank { "बातचीत के अनुसार" },
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            repository.createWorkRequest(request)

            // Send notification to worker
            val notif = NotificationEntity(
                notificationId = "notif_${System.currentTimeMillis()}",
                userId = worker.uid,
                type = "new_job",
                title = "🔔 नया काम उपलब्ध है",
                body = "${request.workType} की जरूरत है - 📍 ${request.location}"
            )
            repository.sendNotification(notif)
            onSuccess()
        }
    }

    fun acceptWorkRequest(requestId: String, connectionId: String) {
        viewModelScope.launch {
            repository.updateWorkRequestStatus(requestId, "ACCEPTED")
            // Automatically navigate to private chat
            val req = workRequests.value.firstOrNull { it.requestId == requestId }
            _currentScreen.value = Screen.Chat(
                connectionId = connectionId,
                otherPartyName = req?.customerName ?: "ग्राहक",
                otherPartyId = req?.customerUid ?: ""
            )
        }
    }

    fun rejectWorkRequest(requestId: String) {
        viewModelScope.launch {
            repository.updateWorkRequestStatus(requestId, "REJECTED")
        }
    }

    // Chat System (PRD Section 20)
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages

    fun sendChatMessage(connectionId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value
        val msg = ChatMessageEntity(
            messageId = "msg_${System.currentTimeMillis()}",
            connectionId = connectionId,
            senderUid = user?.uid ?: "user_1",
            senderName = user?.name ?: "उपयोगकर्ता",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.sendChatMessage(msg)
        }
    }

    // Contractor Team (PRD Section 10: मेरी टीम)
    val contractorTeam: StateFlow<List<ContractorTeamMember>> = repository.contractorTeam

    fun sendTeamJoinRequest(worker: WorkerEntity, onSuccess: () -> Unit) {
        val contractorUid = _currentUser.value?.uid ?: "contractor_1"
        val member = ContractorTeamMember(
            memberId = "team_${System.currentTimeMillis()}",
            contractorUid = contractorUid,
            workerUid = worker.uid,
            workerId = worker.workerId,
            workerName = worker.name,
            professionHindi = worker.professionHindi,
            city = worker.city,
            status = "PENDING"
        )
        viewModelScope.launch {
            repository.addWorkerToTeam(member)
            onSuccess()
        }
    }

    fun setAdminTab(tab: Int) { _adminTab.value = tab }

    fun adminVerifyWorker(uid: String, status: String) {
        viewModelScope.launch {
            repository.adminUpdateWorkerStatus(uid, status, null)
        }
    }

    fun adminSetAccountStatus(uid: String, status: String) {
        viewModelScope.launch {
            repository.adminUpdateWorkerStatus(uid, null, status)
        }
    }

    fun submitReport(worker: WorkerEntity, reason: String, desc: String) {
        val report = ReportEntity(
            reportId = "rep_${System.currentTimeMillis()}",
            reporterId = _currentUser.value?.uid ?: "anon",
            reportedUserId = worker.uid,
            reportedWorkerName = worker.name,
            reason = reason,
            description = desc
        )
        viewModelScope.launch {
            repository.submitReport(report)
        }
    }

    fun simulateGoogleLogin() {
        val uid = "goog_${System.currentTimeMillis() % 10000}"
        _currentUser.value = AuthUser(
            uid = uid,
            name = "अमित वर्मा (Google User)",
            email = "amit.verma@gmail.com",
            role = null
        )
        _currentScreen.value = Screen.RoleSelect
    }

    fun simulateEmailLogin(email: String, name: String) {
        val uid = "mail_${System.currentTimeMillis() % 10000}"
        _currentUser.value = AuthUser(
            uid = uid,
            name = name.ifBlank { "नया यूजर" },
            email = email.ifBlank { "user@example.com" },
            role = null
        )
        _currentScreen.value = Screen.RoleSelect
    }
}
