package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkerEntity
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.adminTab.collectAsState()
    val allWorkers by viewModel.repository.workers.collectAsState()
    val reports by viewModel.repository.reports.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredAdminWorkers = allWorkers.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.workerId.contains(searchQuery) ||
                it.city.contains(searchQuery, ignoreCase = true)
    }

    val totalWorkers = allWorkers.size
    val activeWorkers = allWorkers.count { it.availability == "available" }
    val verifiedWorkers = allWorkers.count { it.verificationStatus == "verified" || it.verificationStatus == "trusted" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "प्रशासन डैशबोर्ड (Admin Portal)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "कार्यशील प्रबंधन व सुरक्षा",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.RoleSelect) },
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "बाहर निकलें")
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
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard("कुल कामगार", "$totalWorkers", SaffronDark, Modifier.weight(1f))
                AdminStatCard("सक्रिय", "$activeWorkers", EmeraldSuccess, Modifier.weight(1f))
                AdminStatCard("सत्यापित", "$verifiedWorkers", NavySecondary, Modifier.weight(1f))
                AdminStatCard("शिकायतें", "${reports.size}", RubyAlert, Modifier.weight(1f))
            }

            // Tabs
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = Color.White,
                contentColor = SaffronPrimary
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { viewModel.setAdminTab(0) },
                    text = { Text("👷 कामगार सूची", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { viewModel.setAdminTab(1) },
                    text = { Text("🚩 शिकायतें (${reports.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = currentTab == 2,
                    onClick = { viewModel.setAdminTab(2) },
                    text = { Text("⚙️ श्रेणियां", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (currentTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ID या नाम से खोजें...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_search_worker"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredAdminWorkers) { worker ->
                                AdminWorkerItem(
                                    worker = worker,
                                    onVerifyClick = {
                                        viewModel.adminVerifyWorker(worker.uid, "verified")
                                        Toast.makeText(context, "${worker.name} Platform Verified किया गया!", Toast.LENGTH_SHORT).show()
                                    },
                                    onMakeTrustedClick = {
                                        viewModel.adminVerifyWorker(worker.uid, "trusted")
                                        Toast.makeText(context, "${worker.name} को Platform Trusted दर्जा दिया गया!", Toast.LENGTH_SHORT).show()
                                    },
                                    onToggleSuspend = {
                                        val newStatus = if (worker.verificationStatus == "suspended") "registered" else "suspended"
                                        viewModel.adminVerifyWorker(worker.uid, newStatus)
                                        Toast.makeText(context, "स्थिति बदली: $newStatus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (reports.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "✅", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("कोई नई शिकायत लंबित नहीं है", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("सभी कामगार सामान्य रूप से कार्यरत हैं।", fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(reports) { r ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "शिकायत ID: ${r.reportId.takeLast(6)}", fontSize = 11.sp, color = TextMuted)
                                            Surface(
                                                color = Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = r.status,
                                                    color = RubyAlert,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "आरोपी: ${r.reportedWorkerName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "कारण: ${r.reason}", color = RubyAlert, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        if (r.description.isNotBlank()) {
                                            Text(text = "विवरण: ${r.description}", fontSize = 12.sp, color = TextDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cat.iconEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = cat.nameHindi, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = cat.nameEnglish, fontSize = 12.sp, color = TextMuted)
                                    }
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "🟢 Active",
                                            fontSize = 11.sp,
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(title, fontSize = 10.sp, color = TextMuted, maxLines = 1)
        }
    }
}

@Composable
fun AdminWorkerItem(
    worker: WorkerEntity,
    onVerifyClick: () -> Unit,
    onMakeTrustedClick: () -> Unit,
    onToggleSuspend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👷", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${worker.professionHindi} • ID: ${worker.workerId}", fontSize = 12.sp, color = TextMuted)
                }
                Surface(
                    color = if (worker.verificationStatus != "suspended") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = worker.verificationStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (worker.verificationStatus != "suspended") EmeraldSuccess else RubyAlert,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onVerifyClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("🟢 Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMakeTrustedClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("⭐ Trusted", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggleSuspend,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (worker.verificationStatus != "suspended") RubyAlert else EmeraldSuccess
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (worker.verificationStatus != "suspended") "Suspend" else "Activate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
