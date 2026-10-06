package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
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
fun WorkerDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val createdWorker by viewModel.createdWorker.collectAsState()
    val allWorkers by viewModel.repository.workers.collectAsState()

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

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // AVAILABILITY TOGGLE - Master PRD Section 4 & 19
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "मेरी उपलब्धता (Worker Availability)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentWorker.availability == "available") "🟢 आज मैं काम के लिए उपलब्ध हूँ" else "🔴 अभी मैं काम के लिए उपलब्ध नहीं हूँ",
                            fontSize = 13.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "त्वरित विकल्प (Quick Actions):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(10.dp))

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
