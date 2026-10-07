package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * PRIVATE CHAT SCREEN (PRD Section 20)
 * Only opened after a Work Request is ACCEPTED.
 * Clean, uncluttered UI supporting work details, text, and security notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    connectionId: String,
    otherPartyName: String,
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val filteredMessages = messages.filter { it.connectionId == connectionId }
    var textInput by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "💬 $otherPartyName", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "सुरक्षित प्राइवेट चैट (स्वीकृत कार्य)", fontSize = 11.sp, color = EmeraldSuccess)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.CustomerHome) },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पीछे जाएँ")
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("संदेश लिखें...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendChatMessage(connectionId, textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier.background(SaffronPrimary, shape = RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "भेजें", tint = Color.White)
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
                .padding(12.dp)
        ) {
            // Safety Banner (PRD Section 47)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Text(
                    text = "🛡️ सुरक्षा निर्देश: अग्रिम ऑनलाइन भुगतान या बैंक ओटीपी किसी से साझा न करें। काम पूरा होने पर ही भुगतान करें।",
                    fontSize = 11.sp,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredMessages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "कार्य अनुरोध स्वीकार हो गया है! अब आप सीधे बातचीत कर सकते हैं।",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMessages) { msg ->
                        val isMe = msg.senderUid == currentUser?.uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMe) SaffronContainer else Color.White,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) SaffronDark else NavySecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = msg.text, fontSize = 13.sp, color = TextDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
