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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Worker ID: ${worker.workerId}")
                                putExtra(Intent.EXTRA_TEXT, "कार्यशील प्रोफाइल: ${worker.name} (${worker.professionHindi}) - ID: ${worker.workerId}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "कारीगर की जानकारी शेयर करें"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("शेयर")
                    }

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
                            .weight(0.6f)
                            .height(50.dp)
                            .testTag("direct_call_worker_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("संपर्क करें (Contact)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
            DigitalWorkerIdCard(
                worker = worker,
                onShareClick = {
                    Toast.makeText(context, "ID कार्ड शेयर लिंक तैयार!", Toast.LENGTH_SHORT).show()
                },
                onCopyIdClick = {
                    Toast.makeText(context, "ID ${worker.workerId} कॉपी किया!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("अनुमानित शुल्क", fontSize = 12.sp, color = TextMuted)
                        Text(worker.startingPrice, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    }
                    Divider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("काम का अनुभव", fontSize = 12.sp, color = TextMuted)
                        Text("${worker.experienceYears} वर्ष", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Divider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("सत्यापन", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = if (worker.verificationStatus == "verified") "🟢 Platform Verified" else "🟡 Registered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧰 दी जाने वाली सेवाएँ (Services)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    worker.services.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(text = "✔", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = s, fontSize = 14.sp, color = TextDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 कारीगर के बारे में (About)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = worker.about.ifBlank { "विश्वसनीय और समयबद्ध कार्य के लिए प्रतिबद्ध कारीगर।" },
                        fontSize = 14.sp,
                        color = TextDark,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        // Approximate area only - Exact address never public (PRD Section 18)
                        Text(
                            text = "${worker.area}, ${worker.city}, ${worker.state}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F5F9),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "सुरक्षा सलाह: काम शुरू करने से पहले कामगार का डिजिटल आईडी कार्ड और 8-अंकों का नंबर अवश्य सत्यापित करें। निजी घर का पता गोपनीय रखा जाता है।",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

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
                        Toast.makeText(context, "आपकी शिकायत दर्ज कर ली गई है। एडमिन इसकी समीक्षा करेगा।", Toast.LENGTH_LONG).show()
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
