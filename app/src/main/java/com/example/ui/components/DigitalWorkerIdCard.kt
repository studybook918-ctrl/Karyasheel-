package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkerEntity
import com.example.ui.theme.*

/**
 * QR Code Canvas Generator - Safe public deep-link reference only (app://worker/{workerId})
 * No private or sensitive data encoded (PRD Section 8)
 */
@Composable
fun SimulatedQrCode(
    workerId: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 15
            val cellSize = size.width / gridSize
            val seed = (workerId.hashCode().toLong() and 0xFFFFFFFFL).toInt()

            fun drawMarker(startX: Int, startY: Int) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(startX * cellSize, startY * cellSize),
                    size = Size(3 * cellSize, 3 * cellSize)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset((startX + 0.5f) * cellSize, (startY + 0.5f) * cellSize),
                    size = Size(2 * cellSize, 2 * cellSize)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset((startX + 1f) * cellSize, (startY + 1f) * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }

            drawMarker(0, 0)
            drawMarker(gridSize - 3, 0)
            drawMarker(0, gridSize - 3)

            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val inMarker1 = r < 3 && c < 3
                    val inMarker2 = r < 3 && c >= gridSize - 3
                    val inMarker3 = r >= gridSize - 3 && c < 3
                    if (!inMarker1 && !inMarker2 && !inMarker3) {
                        val bit = ((seed xor (r * 31 + c * 17)) and (1 shl ((r + c) % 16))) != 0
                        if (bit) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize * 0.9f, cellSize * 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Digital Worker ID Card - Master PRD Sections 4, 8, 11, 12
 * Wording: “Platform Verified” (Not government verified)
 */
@Composable
fun DigitalWorkerIdCard(
    worker: WorkerEntity,
    onShareClick: () -> Unit = {},
    onCopyIdClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_worker_id_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE65100), Color(0xFFEA580C), Color(0xFFC2410C))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🇮🇳", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text(
                                text = "कार्यशील डिजिटल कामगार पहचान-पत्र",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Karyasheel Digital Worker ID Card",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (worker.availability == "available") EmeraldSuccess else RubyAlert,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (worker.availability == "available") "सक्रिय (Active)" else "व्यस्त",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFEDD5))
                            .border(2.dp, SaffronPrimary, RoundedCornerShape(16.dp)),
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
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Text(
                            text = worker.professionHindi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaffronDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "📍 ${worker.city}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🛠️ ${worker.experienceYears} वर्ष अनुभव",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlighted 8-Digit Worker ID & QR Section
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WORKER ID (कामगार क्रमांक)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = worker.workerId.chunked(4).joinToString(" "),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF0F172A)
                                )
                                IconButton(
                                    onClick = onCopyIdClick,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(start = 4.dp)
                                        .testTag("copy_worker_id_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "कॉपी करें",
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // PRD Section 4: Platform Verified (Not Govt Verified)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                val (statusColor, statusText) = when (worker.verificationStatus) {
                                    "verified" -> EmeraldSuccess to "🟢 Platform Verified"
                                    "trusted" -> GoldStar to "⭐ Platform Trusted"
                                    else -> AmberPending to "🟡 Platform Registered"
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }

                        // QR Code
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SimulatedQrCode(
                                workerId = worker.workerId,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Scan QR",
                                fontSize = 9.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Card Footer Actions
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_worker_card_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "शेयर करें",
                            modifier = Modifier.size(16.dp),
                            tint = SaffronPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "कार्ड शेयर करें",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }

                    FilledTonalButton(
                        onClick = onCopyIdClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_worker_card_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SaffronContainer,
                            contentColor = OnSaffronContainer
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "ID सेव करें",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ID सुरक्षित करें",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
