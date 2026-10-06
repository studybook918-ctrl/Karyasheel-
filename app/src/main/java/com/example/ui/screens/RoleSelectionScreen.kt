package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    var showEmailLoginDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Header
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(text = "🇮🇳", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "कार्यशील (Karyasheel)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = SaffronDark
            )
        }

        Text(
            text = "“काम करने वाले को पहचान, काम करवाने वाले को समाधान”",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Big Greeting & Purpose Selection
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "नमस्ते! 🙏",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "आप इस ऐप का उपयोग किस लिए करना चाहते हैं?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CARD 1: WORKER (काम करने वाला)
        Card(
            onClick = { viewModel.selectRole("worker") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("role_worker_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFF7ED), Color(0xFFFFFFFF))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEDD5))
                            .border(2.dp, SaffronPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👷", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "मैं काम करता हूँ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "काम पाएँ • डिजिटल 8-Digit ID कार्ड पाएँ • पहचान बनाएँ",
                            fontSize = 13.sp,
                            color = TextDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD 2: CUSTOMER (काम करवाने वाला)
        Card(
            onClick = { viewModel.selectRole("customer") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("role_customer_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFEFF6FF), Color(0xFFFFFFFF))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(NavyContainer)
                            .border(2.dp, NavySecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔎", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "मुझे काम करवाना है",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "इलेक्ट्रीशियन, प्लंबर, बढ़ई आदि अपने आसपास खोजें व संपर्क करें",
                            fontSize = 13.sp,
                            color = TextDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Authentication Quick Switchers (Google / Email)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "खाता व लॉगिन (Login Options):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.simulateGoogleLogin() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_login_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "🔵 Google से", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showEmailLoginDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("email_login_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "✉️ Email से", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (user != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "लॉगिन हुआ: ${user?.name} (${user?.email})",
                        fontSize = 11.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Admin Access Quick Link (PRD section 23/53)
        TextButton(
            onClick = { viewModel.selectRole("admin") },
            modifier = Modifier.testTag("admin_portal_btn")
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "प्रशासन / एडमिन पोर्टल (Admin Panel)",
                fontSize = 13.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEmailLoginDialog) {
        AlertDialog(
            onDismissRequest = { showEmailLoginDialog = false },
            title = { Text("Email से लॉगिन करें") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("आपका नाम") },
                        modifier = Modifier.fillMaxWidth().testTag("email_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("ईमेल पता") },
                        modifier = Modifier.fillMaxWidth().testTag("email_address_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.simulateEmailLogin(emailInput, nameInput)
                        showEmailLoginDialog = false
                    },
                    modifier = Modifier.testTag("confirm_email_login_btn")
                ) {
                    Text("लॉगिन करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailLoginDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}
