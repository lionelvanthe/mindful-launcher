package com.indie.mindfullauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indie.mindfullauncher.ui.AppDrawerScreen
import com.indie.mindfullauncher.ui.GentleFrictionDialog
import com.indie.mindfullauncher.ui.HomeScreen
import com.indie.mindfullauncher.ui.LauncherViewModel
import com.indie.mindfullauncher.ui.SettingsScreen

private enum class LauncherRoute {
    HOME, DRAWER, SETTINGS
}

private val StillnessColorScheme = darkColorScheme(
    primary = Color(0xFFF5F5F5),
    onPrimary = Color(0xFF0A0A0A),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF1A1200),
    background = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF3A3A3C)
)

class MainActivity : ComponentActivity() {

    private var launcherViewModel: LauncherViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: LauncherViewModel = viewModel()
            launcherViewModel = vm
            val state by vm.uiState.collectAsState()
            var route by remember { mutableStateOf(LauncherRoute.HOME) }

            BackHandler(enabled = route != LauncherRoute.HOME) {
                route = LauncherRoute.HOME
            }

            MaterialTheme(colorScheme = StillnessColorScheme) {
                when (route) {
                    LauncherRoute.HOME -> HomeScreen(
                        state = state,
                        onSelectProfile = vm::selectProfile,
                        onCommandQueryChanged = vm::onCommandQueryChanged,
                        onSubmitCommand = vm::submitCommand,
                        onAppClick = vm::onAppClicked,
                        onToggleNote = vm::toggleNoteDone,
                        onDeleteNote = vm::deleteNote,
                        onOpenDrawer = { route = LauncherRoute.DRAWER },
                        onOpenSettings = { route = LauncherRoute.SETTINGS },
                        onOpenDefaultLauncher = vm::openDefaultLauncherSettings,
                        onOpenUsageSettings = vm::openUsageSettings,
                        onDismissSetupBanner = vm::dismissSetupBanner
                    )
                    LauncherRoute.DRAWER -> AppDrawerScreen(
                        state = state,
                        onBack = { route = LauncherRoute.HOME },
                        onAppClick = vm::onAppClicked,
                        onTogglePin = vm::togglePin,
                        onToggleFriction = vm::toggleFrictionApp
                    )
                    LauncherRoute.SETTINGS -> SettingsScreen(
                        state = state,
                        onBack = { route = LauncherRoute.HOME },
                        onToggleAutoContext = vm::toggleAutoContext,
                        onOpenUsageSettings = vm::openUsageSettings,
                        onOpenDefaultLauncher = vm::openDefaultLauncherSettings
                    )
                }

                state.pendingFriction?.let { pending ->
                    GentleFrictionDialog(
                        pending = pending,
                        onDismiss = vm::dismissFriction,
                        onConfirmLaunch = vm::confirmFrictionLaunch
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel?.refreshTimeContextIfAuto()
    }
}
