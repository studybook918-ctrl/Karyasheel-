package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.model.MainCategoryType
import com.example.model.ProfessionItem
import com.example.model.WorkerEntity
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * CUSTOMER ENTRY - "मुझे काम करवाना है" (PRD Section 13, 14, 15, 16, 17, 19)
 * 1. Step 1: "आपको किस तरह का काम करवाना है?" (5 Main Category Cards: Skilled, General, Professional, Business, Contractor)
 * 2. Step 2: "आपको कौन सा काम करवाना है?" (Subcategory Icon + Name pills)
 * 3. Step 3: Location search (Searchable State/District/City + ⌖ Current Location + 🇮🇳 All-India)
 * 4. Step 4: Worker Results with Direct Call, 8-Digit Worker ID search, Profile view, and Work Request Dialog
 *
 * NOTE: Preserves existing typography, colors, and compact icon proportions strictly (PRD Rule 10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedMainCategory by viewModel.selectedMainCategory.collectAsState()
    val selectedProfessionFilter by viewModel.selectedProfessionFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val workerIdQuery by viewModel.workerIdSearchQuery.collectAsState()
    val workerIdResult by viewModel.searchedWorkerResult.collectAsState()
    val selectedCityFilter by viewModel.selectedCityFilter.collectAsState()
    val availableOnly by viewModel.availableOnlyFilter.collectAsState()
    val workers by viewModel.filteredWorkers.collectAsState()

    var showLocationSelectorDialog by remember { mutableStateOf(false) }
    var isWorkerIdSearchMode by remember { mutableStateOf(false) }
    var activeWorkRequestWorker by remember { mutableStateOf<WorkerEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🇮🇳", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "कार्यशील (Karyasheel)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showLocationSelectorDialog = true }
                                .padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedCityFilter,
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " ▼ बदलें",
                                fontSize = 10.sp,
                                color = SaffronPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedProfessionFilter != null) {
                                viewModel.setSelectedProfessionFilter(null)
                            } else if (selectedMainCategory != null) {
                                viewModel.setSelectedMainCategory(null)
                            } else {
                                viewModel.navigateTo(Screen.RoleSelect)
                            }
                        },
                        modifier = Modifier.testTag("customer_home_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पीछे जाएँ")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.WorkerDashboard) },
                        modifier = Modifier.testTag("worker_mode_quick_btn")
                    ) {
                        Text(text = "👷", fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceLight)
                .padding(padding)
        ) {
            // Search Bar Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (!isWorkerIdSearchMode) {
                    // Regular Category/Keyword Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("🔍 आपको कौन-सा काम करवाना है?") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "साफ करें")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                } else {
                    // PRD Section 7: Dedicated 8-Digit Worker ID Search
                    OutlinedTextField(
                        value = workerIdQuery,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) viewModel.searchByWorkerId(it) },
                        placeholder = { Text("8-अंकों का Worker ID डालें (उदा. 48271635)") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = SaffronPrimary)
                        },
                        trailingIcon = {
                            if (workerIdQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchByWorkerId("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "साफ करें")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_id_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle between Keyword Search, Worker ID Search & Availability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = availableOnly,
                            onClick = { viewModel.toggleAvailableOnly() },
                            label = { Text("🟢 केवल उपलब्ध", fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_available_chip")
                        )
                        if (selectedMainCategory != null || selectedProfessionFilter != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = true,
                                onClick = {
                                    viewModel.setSelectedMainCategory(null)
                                    viewModel.setSelectedProfessionFilter(null)
                                },
                                label = { Text("फ़िल्टर ✕", fontSize = 11.sp) }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            isWorkerIdSearchMode = !isWorkerIdSearchMode
                            if (!isWorkerIdSearchMode) viewModel.searchByWorkerId("")
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (isWorkerIdSearchMode) "काम खोजें" else "🪪 Worker ID से खोजें",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                    }
                }
            }

            // Direct 8-digit Worker ID Search Result
            if (isWorkerIdSearchMode && workerIdResult != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Worker ID से मिला कारीगर:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CustomerWorkerCard(
                        worker = workerIdResult!!,
                        onCardClick = { viewModel.navigateTo(Screen.WorkerDetail(workerIdResult!!)) },
                        onRequestClick = { activeWorkRequestWorker = workerIdResult },
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${workerIdResult!!.phoneNumber}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "${workerIdResult!!.name}: ${workerIdResult!!.phoneNumber}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            } else if (selectedMainCategory == null && searchQuery.isBlank() && !isWorkerIdSearchMode) {
                // =========================================================================
                // PRD SECTION 13: FIRST SCREEN: "आपको किस तरह का काम करवाना है?"
                // 5 Main Category Cards (Skilled, General, Professional, Business, Contractor)
                // Existing compact icon size/proportion strictly preserved (PRD Rule 10)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "आपको किस तरह का काम करवाना है?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MainCategoryType.values().forEach { catType ->
                        Card(
                            onClick = { viewModel.setSelectedMainCategory(catType) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .testTag("main_cat_${catType.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFEDD5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = catType.iconEmoji, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = catType.titleHindi,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = catType.descriptionHindi,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(text = "›", fontSize = 22.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // All India Search Hint Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🇮🇳", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "पूरे भारत में कामगार खोजें: उत्तर प्रदेश, बिहार, राजस्थान, दिल्ली आदि किसी भी शहर के कारीगर खोज सकते हैं।",
                                fontSize = 11.sp,
                                color = NavySecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                // =========================================================================
                // PRD SECTION 14: SUB-PROFESSION FILTER STRIP + WORKER LIST
                // =========================================================================
                val availableProfessions = if (selectedMainCategory != null) {
                    InitialData.professionItems.filter { it.mainCategoryType == selectedMainCategory }
                } else {
                    InitialData.professionItems
                }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedMainCategory != null) "काम चुनें (${selectedMainCategory?.titleHindi}):" else "काम चुनें (Categories):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        if (selectedMainCategory != null) {
                            Text(
                                text = "श्रेणी बदलें",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark,
                                modifier = Modifier.clickable { viewModel.setSelectedMainCategory(null) }
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CategoryPill(
                                icon = "🌟",
                                title = "सभी काम",
                                isSelected = selectedProfessionFilter == null,
                                onClick = { viewModel.setSelectedProfessionFilter(null) },
                                testTag = "prof_pill_all"
                            )
                        }
                        items(availableProfessions) { prof ->
                            CategoryPill(
                                icon = prof.iconEmoji,
                                title = prof.nameHindi,
                                isSelected = selectedProfessionFilter == prof.nameHindi,
                                onClick = {
                                    if (selectedProfessionFilter == prof.nameHindi) {
                                        viewModel.setSelectedProfessionFilter(null)
                                    } else {
                                        viewModel.setSelectedProfessionFilter(prof.nameHindi)
                                    }
                                },
                                testTag = "prof_pill_${prof.id}"
                            )
                        }
                    }
                }

                // Workers Count & Section Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "उपलब्ध कामगार (${workers.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "स्थान: $selectedCityFilter",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                if (workers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "इस खोज या शहर में कोई कामगार नहीं मिला",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setSelectedProfessionFilter(null)
                                    viewModel.setCityFilter("पूरे भारत में (All India)")
                                }
                            ) {
                                Text("पूरे भारत में सभी कामगार देखें")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(workers) { worker ->
                            CustomerWorkerCard(
                                worker = worker,
                                onCardClick = { viewModel.navigateTo(Screen.WorkerDetail(worker)) },
                                onRequestClick = { activeWorkRequestWorker = worker },
                                onContactClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${worker.phoneNumber}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "${worker.name}: ${worker.phoneNumber}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // PRD SECTION 15 & 16: LOCATION SEARCH DIALOG (All-India + Current Location + States)
    // =========================================================================
    if (showLocationSelectorDialog) {
        var locationQuery by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLocationSelectorDialog = false },
            title = {
                Text(
                    text = "📍 कहाँ का Worker चाहिए?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp)) {
                    // Search bar inside picker
                    OutlinedTextField(
                        value = locationQuery,
                        onValueChange = { locationQuery = it },
                        placeholder = { Text("🔍 राज्य / जिला / शहर खोजें...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⌖ Current Location convenience button (PRD Section 16)
                    OutlinedButton(
                        onClick = {
                            viewModel.setCityFilter("हाटा, कुशीनगर (Current)")
                            showLocationSelectorDialog = false
                            Toast.makeText(context, "करेंट लोकेशन सेट: हाटा, कुशीनगर", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaffronPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⌖ मेरी Current Location", color = SaffronDark, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 🇮🇳 पूरे भारत में खोजें option (PRD Section 15)
                    Button(
                        onClick = {
                            viewModel.setCityFilter("पूरे भारत में (All India)")
                            showLocationSelectorDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🇮🇳 पूरे भारत में खोजें (All India)", fontWeight = FontWeight.Bold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Filterable State/City List
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        InitialData.indianStatesWithCities.forEach { (stateName, cityList) ->
                            val filteredCities = cityList.filter {
                                locationQuery.isBlank() ||
                                        it.contains(locationQuery, ignoreCase = true) ||
                                        stateName.contains(locationQuery, ignoreCase = true)
                            }
                            if (filteredCities.isNotEmpty()) {
                                Text(
                                    text = stateName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDark,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                                )
                                filteredCities.forEach { c ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setCityFilter(c)
                                                showLocationSelectorDialog = false
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📍 $c",
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedCityFilter == c) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCityFilter == c) SaffronPrimary else TextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationSelectorDialog = false }) {
                    Text("बंद करें")
                }
            }
        )
    }

    // =========================================================================
    // PRD SECTION 19: WORK REQUEST DIALOG (Work type, desc, date, budget)
    // =========================================================================
    if (activeWorkRequestWorker != null) {
        val targetWorker = activeWorkRequestWorker!!
        var workTypeInput by remember { mutableStateOf(targetWorker.professionHindi) }
        var workDescInput by remember { mutableStateOf("") }
        var dateInput by remember { mutableStateOf("कल (शीघ्र)") }
        var budgetInput by remember { mutableStateOf("बातचीत के अनुसार") }

        AlertDialog(
            onDismissRequest = { activeWorkRequestWorker = null },
            title = {
                Text(
                    text = "📩 Work Request भेजें",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "कारीगर: ${targetWorker.name} (ID: ${targetWorker.workerId})",
                        fontSize = 12.sp,
                        color = SaffronDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = workTypeInput,
                        onValueChange = { workTypeInput = it },
                        label = { Text("काम का प्रकार (Work Type)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = workDescInput,
                        onValueChange = { workDescInput = it },
                        label = { Text("काम का विवरण (Description)") },
                        placeholder = { Text("उदा. 2 पंखे लगाने हैं व बोर्ड की वायरिंग") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("तारीख व समय (Date / Time)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("अनुमानित बजट (Budget)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendWorkRequest(
                            worker = targetWorker,
                            workType = workTypeInput,
                            workDesc = workDescInput,
                            location = "${targetWorker.area}, ${targetWorker.city}",
                            date = dateInput,
                            budget = budgetInput,
                            onSuccess = {
                                activeWorkRequestWorker = null
                                Toast.makeText(
                                    context,
                                    "Work Request भेज दिया गया! कारीगर के स्वीकार करने पर प्राइवेट चैट शुरू होगी।",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("रिक्वेस्ट भेजें")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeWorkRequestWorker = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

/**
 * Worker Card matching PRD Sections 17 & 18.
 * Preserves compact sizes and clear affordances.
 */
@Composable
fun CustomerWorkerCard(
    worker: WorkerEntity,
    onCardClick: () -> Unit,
    onRequestClick: () -> Unit,
    onContactClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.workerId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            worker.categoryId.contains("electrician") -> "⚡"
                            worker.categoryId.contains("plumber") -> "🚰"
                            worker.categoryId.contains("carpenter") -> "🪚"
                            worker.categoryId.contains("painter") -> "🎨"
                            worker.categoryId.contains("mason") -> "🧱"
                            worker.categoryId.contains("driver") -> "🚗"
                            worker.categoryId.contains("ac") -> "❄️"
                            worker.categoryId.contains("professional") -> "👨‍💼"
                            worker.categoryId.contains("contractor") -> "🏗️"
                            worker.categoryId.contains("general") -> "👷"
                            else -> "👷"
                        },
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = worker.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (worker.ratingAverage > 0) worker.ratingAverage else 4.8}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Text(
                        text = worker.professionHindi,
                        fontSize = 13.sp,
                        color = SaffronDark,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "📍 ${worker.area.ifBlank { worker.city }}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛠️ ${worker.experienceYears} वर्ष",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${worker.workerId}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Text(
                    text = if (worker.availability == "available") "🟢 उपलब्ध" else "🔴 व्यस्त",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (worker.availability == "available") EmeraldSuccess else RubyAlert
                )

                Text(
                    text = when (worker.verificationStatus) {
                        "verified" -> "🟢 Platform Verified"
                        "trusted" -> "⭐ Platform Trusted"
                        else -> "🟡 Registered"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Profile, Work Request (PRD Section 17 & 19), Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCardClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("प्रोफ़ाइल", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onRequestClick,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("📩 Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onContactClick,
                    modifier = Modifier.weight(1f).testTag("call_worker_btn_${worker.workerId}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("कॉल", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    icon: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) SaffronContainer else Color.White,
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder(),
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) OnSaffronContainer else TextDark
            )
        }
    }
}
