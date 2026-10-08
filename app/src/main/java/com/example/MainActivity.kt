package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.FcmTokenManager
import com.example.notification.NotificationChannels
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Create Android Notification Channels (PRD Section 34)
        NotificationChannels.createChannels(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MarketplaceApp(intent = intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MarketplaceApp(
    viewModel: MarketplaceViewModel = viewModel(),
    intent: Intent? = null
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Android 13+ Runtime Notification Permission Request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "Notification permission granted: $isGranted")
        if (isGranted) {
            FcmTokenManager.syncCurrentToken(context, currentUser?.uid)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Sync token whenever user logs in or profile changes
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            FcmTokenManager.syncCurrentToken(context, uid)
        }
    }

    // Handle Notification Tap navigation
    LaunchedEffect(intent) {
        intent?.let {
            val targetId = it.getStringExtra("target_id")
            val type = it.getStringExtra("notification_type")
            if (!targetId.isNullOrBlank()) {
                viewModel.searchByWorkerId(targetId)
                viewModel.searchedWorkerResult.value?.let { matchedWorker ->
                    viewModel.navigateTo(Screen.WorkerDetail(matchedWorker))
                }
            } else if (type == "PROFILE_VERIFIED" || type == "new_job") {
                viewModel.navigateTo(Screen.WorkerDashboard)
            }
        }
    }

    // Handle Hardware/System Back Key cleanly
    when (val screen = currentScreen) {
        is Screen.RoleSelect -> {
            // Default Root Screen - standard back exits app
        }
        is Screen.WorkerRegistration -> {
            BackHandler {
                val step = viewModel.registrationStep.value
                if (step > 1) {
                    viewModel.setRegistrationStep(step - 1)
                } else {
                    viewModel.navigateTo(Screen.RoleSelect)
                }
            }
        }
        is Screen.WorkerDashboard -> {
            BackHandler {
                viewModel.navigateTo(Screen.RoleSelect)
            }
        }
        is Screen.CustomerHome -> {
            BackHandler {
                if (viewModel.selectedProfessionFilter.value != null) {
                    viewModel.setSelectedProfessionFilter(null)
                } else if (viewModel.selectedMainCategory.value != null) {
                    viewModel.setSelectedMainCategory(null)
                } else {
                    viewModel.navigateTo(Screen.RoleSelect)
                }
            }
        }
        is Screen.WorkerDetail -> {
            BackHandler {
                viewModel.navigateTo(Screen.CustomerHome)
            }
        }
        is Screen.Chat -> {
            BackHandler {
                viewModel.navigateTo(Screen.CustomerHome)
            }
        }
        is Screen.ContractorTeam -> {
            BackHandler {
                viewModel.navigateTo(Screen.WorkerDashboard)
            }
        }
        is Screen.AdminDashboard -> {
            BackHandler {
                viewModel.navigateTo(Screen.RoleSelect)
            }
        }
        is Screen.PublicQrView -> {
            BackHandler {
                viewModel.navigateTo(Screen.WorkerDashboard)
            }
        }
    }

    when (val screen = currentScreen) {
        is Screen.RoleSelect -> RoleSelectionScreen(viewModel = viewModel)
        is Screen.WorkerRegistration -> WorkerRegistrationScreen(viewModel = viewModel)
        is Screen.WorkerDashboard -> WorkerDashboardScreen(viewModel = viewModel)
        is Screen.CustomerHome -> CustomerHomeScreen(viewModel = viewModel)
        is Screen.WorkerDetail -> WorkerDetailScreen(worker = screen.worker, viewModel = viewModel)
        is Screen.Chat -> ChatScreen(
            connectionId = screen.connectionId,
            otherPartyName = screen.otherPartyName,
            viewModel = viewModel
        )
        is Screen.ContractorTeam -> ContractorTeamScreen(viewModel = viewModel)
        is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
        is Screen.PublicQrView -> PublicQrProfileScreen(viewModel = viewModel)
    }
}
