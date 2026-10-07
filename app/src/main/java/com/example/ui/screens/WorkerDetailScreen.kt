package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkerEntity
import com.example.ui.components.DigitalWorkerIdCard
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * WORKER DETAIL SCREEN (PRD Sections 10, 18, 19, 20)
 * Includes:
 * - Digital Worker ID Card & Public QR Deep Link
 * - Work Request button
 * - Private Chat button (gated on accepted connection per PRD Rule 14 & 15)
 * - Contractor "मेरी टीम में जोड़ें" button (gated on worker acceptance per PRD Section 10)
 * - Direct Call option
 * - Report User modal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    worker: WorkerEntity,
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("गलत जानकारी / Fake Profile") }
    var reportNotes by remember { mutableStateOf("") }
    var showWorkRequestDialog by remember { mutableStateOf(false) }

    val workRequests by viewModel.workRequests.collectAsState()
    val existingConnection = workRequests.firstOrNull {
        it.workerId == worker.workerId && it.status == "ACCEPTED"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = worker.name, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.CustomerHome) },
                        modifier = Modifier.testTag("worker_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पीछे जाएँ")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("report_worker_btn")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "शिकायत करें (Report)", tint = RubyAlert)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Work Request Button (PRD Section 19)
                    Button(
                        onClick = { showWorkRequestDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("send_work_request_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("काम की Request", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Direct Call Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${worker.phoneNumber}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "${worker.name}: ${worker.phoneNumber}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("direct_call_worker_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("कॉल करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
            // Digital Worker ID Card Component
            DigitalWorkerIdCard(
                worker = worker,
                onShareClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "कार्यशील डिजिटल आईडी: ${worker.workerId}")
                        putExtra(Intent.EXTRA_TEXT, "कार्यशील प्रोफाइल: ${worker.name} (${worker.professionHindi}) - ID: ${worker.workerId}\nhttps://karyasheel.in/w/${worker.workerId}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "आईडी कार्ड शेयर करें"))
                },
                onCopyIdClick = {
                    Toast.makeText(context, "Worker ID ${worker.workerId} कॉपी किया!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Private Chat (Gated) & Contractor Add to Team (PRD Section 10 & 20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Private Chat Button
                OutlinedButton(
                    onClick = {
                        if (existingConnection != null) {
                            viewModel.navigateTo(
                                Screen.Chat(
                                    connectionId = "conn_${worker.workerId}",
                                    otherPartyName = worker.name,
                                    otherPartyId = worker.uid
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "🔒 चैट केवल तभी खुलेगी जब कारीगर आपकी Work Request स्वीकार कर लेगा (सुरक्षा नियम)।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavySecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (existingConnection != null) "💬 प्राइवेट चैट" else "🔒 चैट (स्वीकृति बाद)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Contractor Team Add Button (PRD Section 10)
                OutlinedButton(
                    onClick = {
                        viewModel.sendTeamJoinRequest(worker) {
                            Toast.makeText(
                                context,
                                "ठेकेदार टीम आमंत्रण भेजा गया! कारीगर के अप्रूवल के बाद टीम में जुड़ेगा।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaffronDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("मेरी टीम में जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fee, Experience & Verification Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("अनुमानित शुल्क", fontSize = 11.sp, color = TextMuted)
                        Text(worker.startingPrice, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("काम का अनुभव", fontSize = 11.sp, color = TextMuted)
                        Text("${worker.experienceYears} वर्ष", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("सत्यापन", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = if (worker.verificationStatus == "verified") "🟢 Verified" else "🟡 Registered",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Services List
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🧰 दी जाने वाली सेवाएँ (Services)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    worker.services.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = "✔", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = s, fontSize = 13.sp, color = TextDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // About Worker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📝 कारीगर के बारे में (About)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = worker.about.ifBlank { "विश्वसनीय और समयबद्ध कार्य के लिए प्रतिबद्ध कारीगर।" },
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${worker.area}, ${worker.city}, ${worker.state}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy & Safety Warning (PRD Section 25 & 47)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "सुरक्षा सलाह: काम शुरू करने से पहले कामगार का डिजिटल आईडी कार्ड और 8-अंकों का नंबर अवश्य सत्यापित करें। निजी घर का पता गोपनीय रखा जाता है।",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Work Request Dialog (PRD Section 19)
    if (showWorkRequestDialog) {
        var workTypeInput by remember { mutableStateOf(worker.professionHindi) }
        var workDescInput by remember { mutableStateOf("") }
        var dateInput by remember { mutableStateOf("कल (शीघ्र)") }
        var budgetInput by remember { mutableStateOf("बातचीत के अनुसार") }

        AlertDialog(
            onDismissRequest = { showWorkRequestDialog = false },
            title = { Text("📩 Work Request भेजें", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("कारीगर: ${worker.name} (ID: ${worker.workerId})", fontSize = 12.sp, color = SaffronDark, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = workTypeInput,
                        onValueChange = { workTypeInput = it },
                        label = { Text("काम का प्रकार") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = workDescInput,
                        onValueChange = { workDescInput = it },
                        label = { Text("काम का विवरण") },
                        placeholder = { Text("उदा. 2 पंखे लगाने हैं व बोर्ड की वायरिंग") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("तारीख व समय") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("अनुमानित बजट") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendWorkRequest(
                            worker = worker,
                            workType = workTypeInput,
                            workDesc = workDescInput,
                            location = "${worker.area}, ${worker.city}",
                            date = dateInput,
                            budget = budgetInput,
                            onSuccess = {
                                showWorkRequestDialog = false
                                Toast.makeText(context, "Work Request भेज दिया गया! स्वीकृति के बाद चैट सक्रिय होगी।", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("रिक्वेस्ट भेजें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkRequestDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("कारीगर की शिकायत दर्ज करें") },
            text = {
                Column {
                    Text("शिकायत का कारण चुनें:", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf(
                        "गलत जानकारी / Fake Profile",
                        "खराब व्यवहार / Unprofessional",
                        "Spam या अधिक पैसे मांगना",
                        "काम पर नहीं पहुँचे"
                    ).forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = reportReason == r,
                                onClick = { reportReason = r }
                            )
                            Text(text = r, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportNotes,
                        onValueChange = { reportNotes = it },
                        placeholder = { Text("अतिरिक्त विवरण (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("report_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitReport(worker, reportReason, reportNotes)
                        showReportDialog = false
                        Toast.makeText(context, "शिकायत दर्ज कर ली गई है। एडमिन समीक्षा करेगा।", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyAlert),
                    modifier = Modifier.testTag("submit_report_btn")
                ) {
                    Text("शिकायत भेजें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}
