package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
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
import com.example.ui.components.DigitalWorkerIdCard
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * WORKER DASHBOARD (PRD Section 4, 10, 19, 20)
 * - 8-Digit Digital Work ID Card
 * - Availability Switch (🟢 Available / 🔴 Busy)
 * - "आए हुए काम के अनुरोध" (Incoming Work Requests) with Accept / Decline
 * - Contractor "मेरी टीम" (My Team) management
 * - Public QR view link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val createdWorker by viewModel.createdWorker.collectAsState()
    val allWorkers by viewModel.repository.workers.collectAsState()
    val workRequests by viewModel.workRequests.collectAsState()
    val contractorTeam by viewModel.contractorTeam.collectAsState()

    val currentWorker = createdWorker ?: allWorkers.firstOrNull { it.uid == "worker_1" } ?: allWorkers.firstOrNull() ?: WorkerEntity()

    var showShareSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "मेरा प्रोफाइल व ID कार्ड",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.RoleSelect) },
                        modifier = Modifier.testTag("dashboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "पीछे जाएँ"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.PublicQrView) },
                        modifier = Modifier.testTag("public_view_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "पब्लिक व्यू देखें",
                            tint = SaffronPrimary
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎉", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "बधाई हो! आपकी डिजिटल पहचान सक्रिय है",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "आपका 8-अंकों का Unique Worker ID: ${currentWorker.workerId}",
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Digital Worker ID Card
            DigitalWorkerIdCard(
                worker = currentWorker,
                onShareClick = {
                    showShareSuccessDialog = true
                },
                onCopyIdClick = {
                    Toast.makeText(context, "Worker ID ${currentWorker.workerId} कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AVAILABILITY TOGGLE - Master PRD Section 4 & 23
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "मेरी उपलब्धता (Worker Availability)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentWorker.availability == "available") "🟢 आज मैं काम के लिए उपलब्ध हूँ" else "🔴 अभी मैं काम के लिए उपलब्ध नहीं हूँ",
                            fontSize = 12.sp,
                            color = if (currentWorker.availability == "available") EmeraldSuccess else RubyAlert,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = currentWorker.availability == "available",
                        onCheckedChange = { viewModel.toggleAvailability() },
                        modifier = Modifier.testTag("worker_availability_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // INCOMING WORK REQUESTS SECTION (PRD Section 19 & 20)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📩 आए हुए काम के अनुरोध (Requests)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "${workRequests.size} अनुरोध",
                            fontSize = 12.sp,
                            color = SaffronDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (workRequests.isEmpty()) {
                        Text(
                            text = "अभी कोई नया कार्य अनुरोध नहीं है। जब कोई ग्राहक आपसे संपर्क करेगा, तो यहाँ दिखेगा।",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        workRequests.forEach { req ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = req.workType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                                        Text(
                                            text = when (req.status) {
                                                "ACCEPTED" -> "✅ स्वीकृत"
                                                "REJECTED" -> "❌ अस्वीकृत"
                                                else -> "🟡 नया अनुरोध"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (req.status == "ACCEPTED") EmeraldSuccess else SaffronPrimary
                                        )
                                    }
                                    if (req.workDescription.isNotBlank()) {
                                        Text(text = req.workDescription, fontSize = 12.sp, color = TextDark, modifier = Modifier.padding(top = 2.dp))
                                    }
                                    Text(text = "📍 ${req.location} • 💰 ${req.budget}", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (req.status == "PENDING") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.rejectWorkRequest(req.requestId) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("अस्वीकार करें", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { viewModel.acceptWorkRequest(req.requestId, "conn_${req.workerId}") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("स्वीकार करें", fontSize = 11.sp)
                                            }
                                        }
                                    } else if (req.status == "ACCEPTED") {
                                        Button(
                                            onClick = {
                                                viewModel.navigateTo(
                                                    Screen.Chat(
                                                        connectionId = "conn_${req.workerId}",
                                                        otherPartyName = req.customerName,
                                                        otherPartyId = req.customerUid
                                                    )
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("💬 ग्राहक से प्राइवेट चैट करें", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CONTRACTOR "मेरी टीम" QUICK SECTION (PRD Section 10)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👷‍♂️ मेरी टीम (Contractor Team)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        TextButton(onClick = { viewModel.navigateTo(Screen.ContractorTeam) }) {
                            Text("पूरी टीम देखें", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                    }

                    Text(
                        text = "8-अंकों के Karyasheel ID से कारीगरों को अपनी टीम में जोड़ें और प्रोजेक्ट प्रबंधित करें।",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.ContractorTeam) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaffronDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ नया कारीगर टीम में जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "त्वरित विकल्प (Quick Actions):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.PublicQrView) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_qr_profile_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ग्राहक को कैसा दिखेगा? (Public QR View)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.CustomerHome) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_to_customer_view_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("दूसरे कारीगरों को खोजें (Customer Mode)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showShareSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showShareSuccessDialog = false },
            title = { Text("ID कार्ड शेयर लिंक तैयार") },
            text = {
                Text(
                    "कार्यशील डिजिटल कामगार आईडी कार्ड\nनाम: ${currentWorker.name}\nकाम: ${currentWorker.professionHindi}\nWorker ID: ${currentWorker.workerId}\nसत्यापन स्थिति: 🟢 Platform Verified\n\nसार्वजनिक लिंक: https://karyasheel.in/w/${currentWorker.workerId}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "शेयर लिंक कॉपी कर लिया गया!", Toast.LENGTH_SHORT).show()
                        showShareSuccessDialog = false
                    }
                ) {
                    Text("WhatsApp / SMS पर भेजें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareSuccessDialog = false }) {
                    Text("बंद करें")
                }
            }
        )
    }
}
