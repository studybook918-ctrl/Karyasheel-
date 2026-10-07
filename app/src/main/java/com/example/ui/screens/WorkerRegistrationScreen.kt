package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.model.MainCategoryType
import com.example.model.ProfessionItem
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * WORKER ONBOARDING (PRD Section 4, 5, 6, 7, 8, 9, 10, 11)
 * Step 1: उपयोगकर्ता का प्रकार चुनें (5 Main Categories: Skilled, General, Professional, Business, Contractor)
 * Step 2: आपका शुभ नाम क्या है?
 * Step 3: आप क्या काम करते हैं? (Select Sub-profession from Category with compact visual icons)
 * Step 4: आप कहाँ काम करते हैं? (Searchable Indian Location)
 * Step 5: अनुभव व संपर्क (Experience Counter + Phone)
 * Step 6: 🎉 आपका प्रोफाइल व 8-Digit ID कार्ड तैयार है!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.registrationStep.collectAsState()
    val regUserType by viewModel.regUserType.collectAsState()
    val name by viewModel.regName.collectAsState()
    val selectedProfItem by viewModel.regProfessionItem.collectAsState()
    val state by viewModel.regState.collectAsState()
    val city by viewModel.regCity.collectAsState()
    val area by viewModel.regArea.collectAsState()
    val experience by viewModel.regExperience.collectAsState()
    val phone by viewModel.regPhone.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "कामगार रजिस्ट्रेशन (Worker Signup)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "कदम $currentStep / 5",
                            fontSize = 12.sp,
                            color = SaffronDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                viewModel.setRegistrationStep(currentStep - 1)
                            } else {
                                viewModel.navigateTo(Screen.RoleSelect)
                            }
                        },
                        modifier = Modifier.testTag("reg_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "पीछे जाएँ"
                        )
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.setRegistrationStep(currentStep - 1) },
                            modifier = Modifier
                                .weight(0.4f)
                                .testTag("step_prev_btn"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isRegistering
                        ) {
                            Text("पीछे")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep == 1 && name.isBlank()) {
                                Toast.makeText(context, "कृपया अपना नाम दर्ज करें", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (currentStep == 3 && selectedProfItem == null) {
                                Toast.makeText(context, "कृपया अपना कार्य/पेशा चुनें", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (currentStep < 5) {
                                viewModel.setRegistrationStep(currentStep + 1)
                            } else {
                                viewModel.completeWorkerRegistration()
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentStep > 1) 0.6f else 1f)
                            .height(52.dp)
                            .testTag("step_next_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        enabled = !isRegistering
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Worker ID सुरक्षित कर रहे हैं...")
                        } else {
                            Text(
                                text = if (currentStep == 5) "🎉 प्रोफाइल व 8-Digit ID कार्ड बनाएँ" else "आगे बढ़ें (Next)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentStep < 5) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
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
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { currentStep / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = SaffronPrimary,
                trackColor = Color(0xFFFFEDD5)
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (currentStep) {
                // Step 1: Name
                1 -> Step1Name(name = name, onNameChange = { viewModel.updateRegName(it) })

                // Step 2: 5 Main Categories
                2 -> Step2MainCategory(
                    selectedType = regUserType,
                    onSelect = { viewModel.setRegUserType(it) }
                )

                // Step 3: Sub-profession
                3 -> Step3SubProfession(
                    mainCategoryType = regUserType,
                    selectedItem = selectedProfItem,
                    onSelect = { viewModel.updateRegProfessionItem(it) }
                )

                // Step 4: Location
                4 -> Step4Location(
                    selectedState = state,
                    selectedCity = city,
                    area = area,
                    onStateChange = { viewModel.updateRegState(it) },
                    onCityChange = { viewModel.updateRegCity(it) },
                    onAreaChange = { viewModel.updateRegArea(it) }
                )

                // Step 5: Experience & Phone
                5 -> Step5ExperienceAndFinish(
                    experience = experience,
                    phone = phone,
                    name = name,
                    profItem = selectedProfItem,
                    onExpChange = { viewModel.updateRegExperience(it) },
                    onPhoneChange = { viewModel.updateRegPhone(it) }
                )
            }
        }
    }
}

@Composable
fun Step1Name(name: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEDD5)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✍️", fontSize = 38.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "आपका शुभ नाम क्या है?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "यही नाम आपके डिजिटल पहचान पत्र (Worker ID Card) पर दिखेगा",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("उदा. राज कुमार / Raj Kumar") },
            label = { Text("पूरा नाम (Full Name)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reg_name_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "टिप: अपना वही नाम लिखें जिससे आपके इलाके के ग्राहक या ठेकेदार आपको पहचानते हैं।",
                    fontSize = 12.sp,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
fun Step2MainCategory(
    selectedType: MainCategoryType,
    onSelect: (MainCategoryType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "आपकी कार्य श्रेणी क्या है?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "अपनी उपयुक्त श्रेणी चुनें:",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        MainCategoryType.values().forEach { catType ->
            val isSelected = selectedType == catType
            Card(
                onClick = { onSelect(catType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFFEDD5) else Color.White
                ),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = catType.iconEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = catType.titleHindi,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SaffronDark else TextDark
                        )
                        Text(
                            text = catType.descriptionHindi,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    RadioButton(selected = isSelected, onClick = { onSelect(catType) })
                }
            }
        }
    }
}

@Composable
fun Step3SubProfession(
    mainCategoryType: MainCategoryType,
    selectedItem: ProfessionItem?,
    onSelect: (ProfessionItem) -> Unit
) {
    val items = InitialData.professionItems.filter { it.mainCategoryType == mainCategoryType }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "आप क्या काम करते हैं?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "${mainCategoryType.titleHindi} के अंतर्गत अपना मुख्य काम चुनें:",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { prof ->
                val isSelected = selectedItem?.id == prof.id
                Card(
                    onClick = { onSelect(prof) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFEDD5) else Color.White
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = prof.iconEmoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prof.nameHindi,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) SaffronDark else TextDark,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prof.nameEnglish,
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Step4Location(
    selectedState: String,
    selectedCity: String,
    area: String,
    onStateChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onAreaChange: (String) -> Unit
) {
    var stateMenuExpanded by remember { mutableStateOf(false) }
    var cityMenuExpanded by remember { mutableStateOf(false) }

    val availableCities = InitialData.indianStatesWithCities[selectedState] ?: listOf("प्रयागराज (Prayagraj)")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "आप कहाँ काम करते हैं?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "ग्राहक आपको इसी स्थान के आधार पर खोजेंगे:",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // State Dropdown
        Text("राज्य (State):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { stateMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = selectedState, color = TextDark)
                    Text("▼", color = SaffronPrimary)
                }
            }
            DropdownMenu(expanded = stateMenuExpanded, onDismissRequest = { stateMenuExpanded = false }) {
                InitialData.indianStatesWithCities.keys.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s) },
                        onClick = {
                            onStateChange(s)
                            val firstCity = InitialData.indianStatesWithCities[s]?.firstOrNull() ?: ""
                            onCityChange(firstCity)
                            stateMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // City Dropdown
        Text("जिला / शहर (City / District):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { cityMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = selectedCity, color = TextDark)
                    Text("▼", color = SaffronPrimary)
                }
            }
            DropdownMenu(expanded = cityMenuExpanded, onDismissRequest = { cityMenuExpanded = false }) {
                availableCities.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            onCityChange(c)
                            cityMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Area / Mohalla
        Text("इलाका / मोहल्ला (Area / Colony):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = area,
            onValueChange = onAreaChange,
            placeholder = { Text("उदा. सिविल लाइंस / कटरा / हाटा बाजार") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun Step5ExperienceAndFinish(
    experience: Int,
    phone: String,
    name: String,
    profItem: ProfessionItem?,
    onExpChange: (Int) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⭐", fontSize = 34.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "काम का अनुभव व फोन नंबर",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "आपको यह काम कितने साल से आता है?", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (experience > 0) onExpChange(experience - 1) },
                        modifier = Modifier.background(Color(0xFFFFEDD5), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "कम करें")
                    }

                    Text(
                        text = "$experience वर्ष",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    IconButton(
                        onClick = { onExpChange(experience + 1) },
                        modifier = Modifier.background(Color(0xFFFFEDD5), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "बढ़ाएं")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("मोबाइल नंबर (फोन कॉल व WhatsApp)") },
            placeholder = { Text("उदा. 9876543210") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFEFF6FF),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "✅ 8-अंकों का Digital Work ID मिलेगा", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                Text(text = "नाम: $name", fontSize = 12.sp, color = TextDark)
                Text(text = "काम: ${profItem?.nameHindi ?: "कारीगर"}", fontSize = 12.sp, color = TextDark)
                Text(text = "सत्यापन: 🟡 Platform Registered (मुफ्त)", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}
