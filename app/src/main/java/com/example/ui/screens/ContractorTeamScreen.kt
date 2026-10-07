package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkerEntity
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * CONTRACTOR — "मेरी टीम" (PRD Section 10)
 * Flow:
 * Contractor -> मेरी टीम -> + Worker जोड़ें -> 8-digit Worker ID enter -> Worker basic public profile ->
 * "Team में जोड़ने का Request भेजें" -> Worker Notification / Approval -> Team में जुड़ गया
 *
 * Security:
 * Explicit worker approval required (PRD Rule).
 * Removing worker does not delete worker profile or alter Worker ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorTeamScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teamMembers by viewModel.contractorTeam.collectAsState()
    var workerIdInput by remember { mutableStateOf("") }
    var searchedWorker by remember { mutableStateOf<WorkerEntity?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "मेरी टीम (Contractor Team)", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.WorkerDashboard) },
                        modifier = Modifier.testTag("contractor_team_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पीछे जाएँ")
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
                .padding(16.dp)
        ) {
            // Add Worker via 8-digit ID Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SaffronPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ नया कारीगर टीम में जोड़ें",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Text(
                        text = "कारीगर का 8-अंकों का Karyasheel Digital Work ID दर्ज करें:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = workerIdInput,
                            onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) workerIdInput = it },
                            placeholder = { Text("8-Digit ID (उदा. 48271635)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SaffronPrimary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (workerIdInput.length == 8) {
                                    val match = viewModel.repository.workers.value.firstOrNull { it.workerId == workerIdInput }
                                    searchedWorker = match
                                    if (match == null) {
                                        Toast.makeText(context, "ID $workerIdInput का कारीगर नहीं मिला", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "कृपया पूरे 8 अंक दर्ज करें", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Text("खोजें")
                        }
                    }

                    // Worker Preview & Send Invite Button
                    if (searchedWorker != null) {
                        val w = searchedWorker!!
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "मिले कारीगर का विवरण:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                                Text(text = "${w.name} (${w.professionHindi})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(text = "स्थान: ${w.area}, ${w.city} • अनुभव: ${w.experienceYears} वर्ष", fontSize = 11.sp, color = TextMuted)

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.sendTeamJoinRequest(w) {
                                            searchedWorker = null
                                            workerIdInput = ""
                                            Toast.makeText(context, "Team में जोड़ने का Request भेज दिया गया! कारीगर के स्वीकार करने पर टीम में जुड़ेगा।", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                ) {
                                    Text("Team में जोड़ने का Request भेजें", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team Members List & Statistics
            Text(
                text = "वर्तमान टीम सदस्य (${teamMembers.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (teamMembers.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "👥", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "आपकी टीम में अभी कोई सदस्य नहीं है", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            text = "ऊपर दिए गए बॉक्स में कारीगरों का 8-Digit ID डालकर उन्हें अपनी टीम में आमंत्रित करें।",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(teamMembers) { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = member.workerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(text = "${member.professionHindi} • ID: ${member.workerId}", fontSize = 12.sp, color = SaffronDark, fontFamily = FontFamily.Monospace)
                                    Text(
                                        text = if (member.status == "ACCEPTED") "✅ टीम में सक्रिय" else "⏳ आमंत्रण भेजा गया (Pending)",
                                        fontSize = 11.sp,
                                        color = if (member.status == "ACCEPTED") EmeraldSuccess else SaffronPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (member.status == "ACCEPTED") {
                                    Text(text = "🟢 सक्रिय", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(text = "प्रतीक्षारत", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
