package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.model.CategoryEntity
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

/**
 * Worker Registration - Master PRD Section 9, 10 & Firebase Production Architecture Section 6
 * Step 1: आपका नाम क्या है?
 * Step 2: आप क्या काम करते हैं? (CategoryEntity)
 * Step 3: आप कहाँ काम करते हैं? (State, City, Area)
 * Step 4: आपको यह काम कितने साल से आता है? (Experience Counter)
 * Step 5: अपना फोटो लगाएँ (50-80 KB WebP target pipeline ready)
 * Step 6: 🎉 आपका प्रोफाइल तैयार है!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.registrationStep.collectAsState()
    val name by viewModel.regName.collectAsState()
    val selectedCategory by viewModel.regCategory.collectAsState()
    val state by viewModel.regState.collectAsState()
    val city by viewModel.regCity.collectAsState()
    val area by viewModel.regArea.collectAsState()
    val experience by viewModel.regExperience.collectAsState()
    val phone by viewModel.regPhone.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()

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

            Spacer(modifier = Modifier.height(20.dp))

            when (currentStep) {
                1 -> Step1Name(name = name, onNameChange = { viewModel.updateRegName(it) })
                2 -> Step2Category(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelect = { viewModel.updateRegCategory(it) }
                )
                3 -> Step3Location(
                    selectedState = state,
                    selectedCity = city,
                    area = area,
                    onStateChange = { viewModel.updateRegState(it) },
                    onCityChange = { viewModel.updateRegCity(it) },
                    onAreaChange = { viewModel.updateRegArea(it) }
                )
                4 -> Step4Experience(
                    experience = experience,
                    phone = phone,
                    onExpChange = { viewModel.updateRegExperience(it) },
                    onPhoneChange = { viewModel.updateRegPhone(it) }
                )
                5 -> Step5Photo(name = name, category = selectedCategory)
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
                    text = "टिप: अपना वही नाम लिखें जिससे आपके इलाके के ग्राहक आपको पहचानते हैं।",
                    fontSize = 12.sp,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
fun Step2Category(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onSelect: (CategoryEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "आप क्या काम करते हैं?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "नीचे दिए गए विकल्पों में से अपना मुख्य काम चुनें (कम पढ़ना, ज्यादा देखना):",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory?.categoryId == cat.categoryId
                Card(
                    onClick = { onSelect(cat) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_card_${cat.categoryId}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFEDD5) else Color.White
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = cat.iconEmoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.nameHindi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) SaffronDark else TextDark,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.nameEnglish,
                            fontSize = 11.sp,
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
fun Step3Location(
    selectedState: String,
    selectedCity: String,
    area: String,
    onStateChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onAreaChange: (String) -> Unit
) {
    var stateMenuExpanded by remember { mutableStateOf(false) }
    var cityMenuExpanded by remember { mutableStateOf(false) }

    val states = InitialData.indianStatesWithCities.keys.toList()
    val cities = InitialData.indianStatesWithCities[selectedState] ?: listOf("प्रयागराज (Prayagraj)")

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
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📍", fontSize = 38.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "आप कहाँ काम करते हैं?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "ग्राहक आपको आपके शहर और इलाके के अनुसार खोजेंगे (सटीक घर का पता गोपनीय रहेगा)",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // State Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedState,
                onValueChange = {},
                readOnly = true,
                label = { Text("राज्य (State)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { stateMenuExpanded = true }
                    .testTag("reg_state_field"),
                shape = RoundedCornerShape(14.dp)
            )
            DropdownMenu(
                expanded = stateMenuExpanded,
                onDismissRequest = { stateMenuExpanded = false }
            ) {
                states.forEach { s ->
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
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("शहर / जिला (City / District)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { cityMenuExpanded = true }
                    .testTag("reg_city_field"),
                shape = RoundedCornerShape(14.dp)
            )
            DropdownMenu(
                expanded = cityMenuExpanded,
                onDismissRequest = { cityMenuExpanded = false }
            ) {
                cities.forEach { c ->
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
        OutlinedTextField(
            value = area,
            onValueChange = onAreaChange,
            placeholder = { Text("उदा. सिविल लाइंस, कटरा, चौक आदि") },
            label = { Text("इलाका / मोहल्ला (Area)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaffronPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reg_area_input"),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun Step4Experience(
    experience: Int,
    phone: String,
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
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🛠️", fontSize = 38.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "आपको यह काम कितने साल से आता है?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "अनुभव से ग्राहकों का भरोसा बढ़ता है",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { if (experience > 0) onExpChange(experience - 1) },
                    modifier = Modifier.size(54.dp).testTag("exp_minus_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SaffronContainer)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "घटाएँ", tint = OnSaffronContainer)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$experience",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = SaffronDark
                    )
                    Text(
                        text = "साल का अनुभव (Years)",
                        fontSize = 14.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                FilledIconButton(
                    onClick = { onExpChange(experience + 1) },
                    modifier = Modifier.size(54.dp).testTag("exp_plus_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "बढ़ाएँ", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = { Text("उदा. +91 98765 43210") },
            label = { Text("संपर्क फोन नंबर (Mobile Number)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reg_phone_input"),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun Step5Photo(
    name: String,
    category: CategoryEntity?
) {
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
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📸", fontSize = 38.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "अपना फोटो चुनें",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "साफ फोटो से ग्राहक तुरंत पहचान पाते हैं (ऑटोमैटिक 50-80 KB ऑप्टिमाइज़्ड)",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEDD5))
                .border(3.dp, SaffronPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category?.iconEmoji ?: "👷", fontSize = 64.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("upload_photo_btn")
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SaffronPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("फोटो चुनें (ऑटो कम्प्रेशन)", color = SaffronPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎉 सब तैयार है!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "‘आगे बढ़ें’ दबाते ही आपका 8-अंकों का यूनिक Worker ID रिज़र्व होगा और डिजिटल ID कार्ड तैयार हो जाएगा जिसे आप सुरक्षित रूप से शेयर कर सकेंगे।",
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
