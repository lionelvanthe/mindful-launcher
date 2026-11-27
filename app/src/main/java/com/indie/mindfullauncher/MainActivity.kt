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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.indie.mindfullauncher.ui.ProUpgradeSheet
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
                        vm.addPinnedWidgetId(id)
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

            LaunchedEffect(state.effectiveShowWallpaper) {
                if (state.effectiveShowWallpaper) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
                }
            }

            LaunchedEffect(state.hideStatusBar) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (state.hideStatusBar) {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                }
            }

            StillnessTheme(fontPreset = state.fontPreset, accentPreset = state.accentPreset) {
                when (route) {
                    LauncherRoute.HOME -> HomeScreen(
                        state = state,
                        onCommandQueryChanged = vm::onCommandQueryChanged,
                        onSubmitCommand = vm::submitCommand,
                        onAppClick = vm::onAppClicked,
                        onDialContact = vm::dialContact,
                        onToggleFavorite = { pkg ->
                            val target = if (vm.uiState.value.activeProfile == ContextProfile.ALL) {
                                ContextProfile.WORK
                            } else {
                                vm.uiState.value.activeProfile
                            }
                            vm.togglePin(target, pkg)
                        },
                        onMoveFavorite = vm::moveFavorite,
                        onRerollUtility = vm::rerollCommandUtility,
                        onToggleHidden = vm::toggleHiddenApp,
                        onToggleSummary = vm::toggleSummaryPackage,
                        onToggleFriction = vm::toggleFrictionApp,
                        onDismissPostSessionNudge = vm::dismissPostSessionNudge,
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
                        onRenameFolder = vm::renameFolder,
                        onDeleteFolder = vm::deleteFolder,
                        onRemovePinnedWidget = vm::removePinnedWidgetId,
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
                        onSetShowContactSearch = vm::setShowContactSearch,
                        onSetSmartSortEnabled = vm::setSmartSortEnabled,
                        onSelectProfile = vm::selectProfile,
                        onSelectCustomContext = vm::selectCustomContext,
                        onCreateCustomContext = vm::createCustomContext,
                        onSaveCustomContext = vm::saveCustomContext,
                        onSaveBuiltInContextConfig = vm::saveBuiltInContextConfig,
                        onDeleteCustomContext = vm::deleteCustomContext,
                        onRequirePro = vm::openProUpgradeSheet,
                        onSetAppsForActiveContext = vm::setAppsForActiveContext,
                        onStartFocusTimer = vm::startFocusTimer,
                        onStopFocusTimer = vm::stopFocusTimer,
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
                        onSetAccentPreset = vm::setAccentPreset,
                        onSetIconSizePreset = vm::setIconSizePreset,
                        onSetIconShapePreset = vm::setIconShapePreset,
                        onSetHideStatusBar = vm::setHideStatusBar,
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
                        onToggleAutoContext = vm::toggleAutoContext,
                        onSetAutoSleepOnCharge = vm::setAutoSleepOnCharge,
                        onSetMonochromeInSleep = vm::setMonochromeInSleep,
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
                        onOpenDefaultLauncher = vm::openDefaultLauncherSettings,
                        onOpenProUpgrade = vm::openProUpgradeSheet
                    )
                }

                if (state.showProUpgradeSheet) {
                    ProUpgradeSheet(
                        isProUnlocked = state.isProUnlocked,
                        formattedPrice = state.proPrice,
                        statusMessage = state.proStatusMessage,
                        onDismiss = vm::dismissProUpgradeSheet,
                        onPurchaseClick = { vm.launchProPurchase(this@MainActivity) },
                        onRestoreClick = vm::restoreProPurchases
                    )
                }

                state.pendingFriction?.let { pending ->
                    GentleFrictionDialog(
                        pending = pending,
                        onDismiss = vm::dismissFriction,
                        onConfirmLaunch = vm::confirmFrictionLaunch,
                        onSelectIntention = vm::selectFrictionIntention
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel?.onLauncherResumed()
    }
}
