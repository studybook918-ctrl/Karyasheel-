package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.PublicQrProfileScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.WorkerDashboardScreen
import com.example.ui.screens.WorkerDetailScreen
import com.example.ui.screens.WorkerRegistrationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MarketplaceApp()
                }
            }
        }
    }
}

@Composable
fun MarketplaceApp(
    viewModel: MarketplaceViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle Hardware/System Back Key cleanly
    when (val screen = currentScreen) {
        is Screen.RoleSelect -> {
            // Default Root Screen - standard back behavior exits app
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
                viewModel.navigateTo(Screen.RoleSelect)
            }
        }
        is Screen.WorkerDetail -> {
            BackHandler {
                viewModel.navigateTo(Screen.CustomerHome)
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
        is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
        is Screen.PublicQrView -> PublicQrProfileScreen(viewModel = viewModel)
    }
}
