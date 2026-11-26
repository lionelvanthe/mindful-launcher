package com.indie.mindfullauncher

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.ui.AppDrawerScreen
import com.indie.mindfullauncher.ui.GentleFrictionDialog
import com.indie.mindfullauncher.ui.HomeScreen
import com.indie.mindfullauncher.ui.LauncherViewModel
import com.indie.mindfullauncher.ui.SettingsScreen
import com.indie.mindfullauncher.ui.StillnessTheme
import com.indie.mindfullauncher.ui.WidgetHostHelper

private enum class LauncherRoute {
    HOME, DRAWER, SETTINGS
}

class MainActivity : ComponentActivity() {

    private var launcherViewModel: LauncherViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val vm: LauncherViewModel = viewModel()
            launcherViewModel = vm
            val state by vm.uiState.collectAsState()
            var route by remember { mutableStateOf(LauncherRoute.HOME) }
            var pendingWidgetId by remember { mutableIntStateOf(-1) }
            var pendingWidgetTargetPkg by remember { mutableStateOf<String?>(null) }

            val widgetPickLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data = result.data
                val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
                if (result.resultCode == Activity.RESULT_OK && id > 0) {
                    val targetPkg = pendingWidgetTargetPkg
                    if (targetPkg != null) {
                        vm.setAppPopupWidget(targetPkg, id)
                    } else {
                        vm.setPinnedWidgetId(id)
                    }
                } else if (id > 0) {
                    WidgetHostHelper.deleteWidgetId(this, id)
                }
                pendingWidgetId = -1
                pendingWidgetTargetPkg = null
            }

            BackHandler(enabled = route != LauncherRoute.HOME) {
                route = LauncherRoute.HOME
            }

            LaunchedEffect(state.showWallpaperBackground) {
                if (state.showWallpaperBackground) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
                }
            }

            StillnessTheme(fontPreset = state.fontPreset) {
                when (route) {
                    LauncherRoute.HOME -> HomeScreen(
                        state = state,
                        onCommandQueryChanged = vm::onCommandQueryChanged,
                        onSubmitCommand = vm::submitCommand,
                        onAppClick = vm::launchAppDirect,
                        onDialContact = vm::dialContact,
                        onToggleFavorite = { pkg -> vm.togglePin(ContextProfile.WORK, pkg) },
                        onToggleHidden = vm::toggleHiddenApp,
                        onToggleSummary = vm::toggleSummaryPackage,
                        onRenameApp = vm::setCustomAppName,
                        onPickPopupWidget = { pkg ->
                            val (id, pickIntent) = WidgetHostHelper.createPickIntent(this@MainActivity)
                            pendingWidgetId = id
                            pendingWidgetTargetPkg = pkg
                            widgetPickLauncher.launch(pickIntent)
                        },
                        onClearPopupWidget = vm::clearAppPopupWidget,
                        onCreateFolder = vm::createFolder,
                        onToggleAppInFolder = vm::toggleAppInFolder,
                        onDeleteFolder = vm::deleteFolder,
                        onLoadShortcuts = vm::loadShortcutsForApp,
                        onLaunchShortcut = vm::launchShortcut,
                        onSetNiagaraButtonPackage = vm::setNiagaraButtonPackage,
                        onToggleMediaPlayPause = vm::toggleMediaPlayPause,
                        onSkipMediaNext = vm::skipMediaNext,
                        onSkipMediaPrevious = vm::skipMediaPrevious,
                        onDismissNotification = vm::dismissNotification,
                        onSendNotificationReply = vm::sendNotificationReply,
                        onOpenAppInfo = vm::openAppInfo,
                        onUninstallApp = vm::uninstallApp,
                        onToggleNote = vm::toggleNoteDone,
                        onDeleteNote = vm::deleteNote,
                        onOpenSettings = { route = LauncherRoute.SETTINGS },
                        onOpenDefaultLauncher = vm::openDefaultLauncherSettings,
                        onDismissSetupBanner = vm::dismissSetupBanner,
                        onDismissTipsBanner = vm::dismissTipsBanner
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
                        onSetClockStyle = vm::setClockStyle,
                        onSetIconSizePreset = vm::setIconSizePreset,
                        onSetFontPreset = vm::setFontPreset,
                        onSetHideFavoriteLabels = vm::setHideFavoriteLabels,
                        onSetShowBatteryOnHome = vm::setShowBatteryOnHome,
                        onSetShowAlphabetWave = vm::setShowAlphabetWave,
                        onSetShowWallpaperBackground = vm::setShowWallpaperBackground,
                        onSetMonochromeIcons = vm::setMonochromeIcons,
                        onSetTwoColumnLargeScreen = vm::setTwoColumnLargeScreen,
                        onSetSelectedIconPack = vm::setSelectedIconPack,
                        onSetShowCalendarEvents = vm::setShowCalendarEvents,
                        onSetShowContactSearch = vm::setShowContactSearch,
                        onSetShowMusicOnHeadphones = vm::setShowMusicOnHeadphones,
                        onSetShowNotificationPreviews = vm::setShowNotificationPreviews,
                        onSetShowMediaPlayer = vm::setShowMediaPlayer,
                        onSetSmartSortEnabled = vm::setSmartSortEnabled,
                        onSetShowMostUsedApps = vm::setShowMostUsedApps,
                        onOpenNotificationSettings = vm::openNotificationListenerSettings,
                        onOpenUsageSettings = vm::openUsageSettings,
                        onPickWidget = {
                            val (id, pickIntent) = WidgetHostHelper.createPickIntent(this@MainActivity)
                            pendingWidgetId = id
                            pendingWidgetTargetPkg = null
                            widgetPickLauncher.launch(pickIntent)
                        },
                        onClearWidget = vm::clearPinnedWidget,
                        onSetNiagaraButtonPackage = vm::setNiagaraButtonPackage,
                        onRefreshPermissions = vm::refreshTimeContextIfAuto,
                        onExportBackup = vm::exportBackupJson,
                        onImportBackup = vm::importBackupJson,
                        onUnhideApp = vm::toggleHiddenApp,
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
