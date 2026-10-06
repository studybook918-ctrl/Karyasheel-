package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.model.WorkerEntity
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * Customer Home Screen - PRD Sections 7, 15, 16, 18, 19
 * Supports both category search AND Direct 8-Digit Worker ID Search (PRD Section 7)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val workerIdQuery by viewModel.workerIdSearchQuery.collectAsState()
    val workerIdResult by viewModel.searchedWorkerResult.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedCityFilter by viewModel.selectedCityFilter.collectAsState()
    val availableOnly by viewModel.availableOnlyFilter.collectAsState()
    val workers by viewModel.filteredWorkers.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()

    var showLocationSelectorDialog by remember { mutableStateOf(false) }
    var isWorkerIdSearchMode by remember { mutableStateOf(false) }

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
                        onClick = { viewModel.navigateTo(Screen.RoleSelect) },
                        modifier = Modifier.testTag("customer_home_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "भूमिका बदलें")
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

                // Toggle between Keyword Search and Worker ID Search
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
                        if (selectedCategoryFilter != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setCategoryFilter(null) },
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

            // If Worker ID search mode has a direct match
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
            } else {
                // Category Strip
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "काम चुनें (Categories):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            CategoryPill(
                                icon = "🌟",
                                title = "सभी काम",
                                isSelected = selectedCategoryFilter == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                testTag = "category_pill_all"
                            )
                        }
                        items(categories) { cat ->
                            CategoryPill(
                                icon = cat.iconEmoji,
                                title = cat.nameHindi.split(" ")[0],
                                isSelected = selectedCategoryFilter == cat.categoryId,
                                onClick = { viewModel.setCategoryFilter(cat.categoryId) },
                                testTag = "category_pill_${cat.categoryId}"
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
                                    viewModel.setCategoryFilter(null)
                                }
                            ) {
                                Text("सभी कामगार देखें")
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

    if (showLocationSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSelectorDialog = false },
            title = { Text("शहर / जिला चुनें") },
            text = {
                Column {
                    InitialData.indianStatesWithCities.forEach { (stateName, cityList) ->
                        Text(
                            text = stateName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        cityList.forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setCityFilter(c)
                                        showLocationSelectorDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 $c",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCityFilter == c) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCityFilter == c) SaffronPrimary else TextDark
                                )
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
}

@Composable
fun CustomerWorkerCard(
    worker: WorkerEntity,
    onCardClick: () -> Unit,
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
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (worker.categoryId) {
                            "cat_electrician" -> "⚡"
                            "cat_plumber" -> "🚰"
                            "cat_carpenter" -> "🪚"
                            "cat_painter" -> "🎨"
                            "cat_mason" -> "🧱"
                            "cat_driver" -> "🚗"
                            "cat_ac" -> "❄️"
                            else -> "👷"
                        },
                        fontSize = 28.sp
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
                            fontSize = 16.sp,
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
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛠️ ${worker.experienceYears} वर्ष",
                            fontSize = 12.sp,
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
                    text = if (worker.availability == "available") "🟢 आज उपलब्ध" else "🔴 व्यस्त",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (worker.availability == "available") EmeraldSuccess else RubyAlert
                )

                Text(
                    text = when (worker.verificationStatus) {
                        "verified" -> "🟢 Platform Verified"
                        "trusted" -> "⭐ Platform Trusted"
                        else -> "🟡 Platform Registered"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    Text("प्रोफ़ाइल देखें", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onContactClick,
                    modifier = Modifier.weight(1f).testTag("call_worker_btn_${worker.workerId}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("कॉल करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SaffronContainer else Color.White,
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder(),
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) OnSaffronContainer else TextDark
            )
        }
    }
}

