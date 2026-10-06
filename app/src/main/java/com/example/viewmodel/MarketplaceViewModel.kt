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

    // Worker Registration Form State (Step by Step - Master PRD Section 10)
    private val _registrationStep = MutableStateFlow(1)
    val registrationStep = _registrationStep.asStateFlow()

    private val _regName = MutableStateFlow("")
    val regName = _regName.asStateFlow()

    private val _regCategory = MutableStateFlow<CategoryEntity?>(null)
    val regCategory = _regCategory.asStateFlow()

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

    // Customer Search & Filter States (Worker ID Search + Category + City)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _workerIdSearchQuery = MutableStateFlow("")
    val workerIdSearchQuery = _workerIdSearchQuery.asStateFlow()

    private val _searchedWorkerResult = MutableStateFlow<WorkerEntity?>(null)
    val searchedWorkerResult = _searchedWorkerResult.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedCityFilter = MutableStateFlow("प्रयागराज (Prayagraj)")
    val selectedCityFilter = _selectedCityFilter.asStateFlow()

    private val _availableOnlyFilter = MutableStateFlow(false)
    val availableOnlyFilter = _availableOnlyFilter.asStateFlow()

    // Admin Tabs & Filters
    private val _adminTab = MutableStateFlow(0) // 0: Workers, 1: Reports, 2: Categories
    val adminTab = _adminTab.asStateFlow()

    // Filtered Workers for Customers (Efficient, only active workers displayed)
    val filteredWorkers: StateFlow<List<WorkerEntity>> = combine(
        repository.workers,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedCityFilter,
        _availableOnlyFilter
    ) { workers, query, catFilter, cityFilter, availOnly ->
        workers.filter { worker ->
            val matchesCategory = catFilter == null || worker.categoryId == catFilter
            val matchesCity = cityFilter.isBlank() || worker.city.contains(cityFilter, ignoreCase = true)
            val matchesAvail = !availOnly || worker.availability == "available"
            val matchesQuery = query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.professionHindi.contains(query, ignoreCase = true) ||
                    worker.professionEnglish.contains(query, ignoreCase = true) ||
                    worker.workerId.contains(query) ||
                    worker.services.any { it.contains(query, ignoreCase = true) }

            matchesCategory && matchesCity && matchesAvail && matchesQuery
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
    fun updateRegName(name: String) { _regName.value = name }
    fun updateRegCategory(cat: CategoryEntity) { _regCategory.value = cat }
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
            val cat = _regCategory.value ?: repository.categories.value.firstOrNull() ?: CategoryEntity("cat_electrician", "बिजली का काम", "Electrician", "⚡")
            val uid = _currentUser.value?.uid ?: "worker_${System.currentTimeMillis()}"

            val worker = WorkerEntity(
                uid = uid,
                workerId = generated8DigitId,
                name = _regName.value.ifBlank { "कुशल कारीगर" },
                categoryId = cat.categoryId,
                categoryNameHindi = cat.nameHindi,
                categoryNameEnglish = cat.nameEnglish,
                professionHindi = cat.nameHindi,
                professionEnglish = cat.nameEnglish,
                experienceYears = _regExperience.value,
                state = _regState.value,
                district = _regCity.value,
                city = _regCity.value,
                area = _regArea.value.ifBlank { "स्थानीय क्षेत्र" },
                services = listOf("बुनियादी सेवा", "आपातकालीन कार्य", "नियमित मरम्मत"),
                about = "${_regExperience.value} वर्षों के अनुभव के साथ ${cat.nameHindi} में समर्पित सेवा।",
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

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(categoryId: String?) { _selectedCategoryFilter.value = categoryId }
    fun setCityFilter(city: String) { _selectedCityFilter.value = city }
    fun toggleAvailableOnly() { _availableOnlyFilter.value = !_availableOnlyFilter.value }

    // Search by 8-Digit Worker ID (PRD Section 7)
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
