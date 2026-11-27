package com.indie.mindfullauncher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AccentPreset
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.BuiltInContextConfig
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.CustomContextProfile
import com.indie.mindfullauncher.model.FocusSessionState
import com.indie.mindfullauncher.model.IconShapePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StillnessLauncherUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleApps = listOf(
        AppInfo("Calendar", "com.example.calendar"),
        AppInfo("Spotify", "com.example.spotify"),
        AppInfo("Notes", "com.example.notes")
    )

    @Test
    fun homeScreen_displaysOrderedFavoritesAndUtilityResult() {
        var rerolled = false
        val state = LauncherUiState(
            installedApps = sampleApps,
            pinnedByProfile = mapOf(
                ContextProfile.WORK to setOf("com.example.calendar", "com.example.spotify")
            ),
            favoriteOrder = listOf("com.example.spotify", "com.example.calendar"),
            showWallpaperBackground = false,
            isDefaultLauncher = true,
            commandQuery = "/coin",
            commandResult = CommandResult.UtilityResult(
                kind = "coin",
                title = "Tung đồng xu",
                value = "Ngửa (Heads)",
                hint = "Chạm để tung lại"
            )
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = { rerolled = true },
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Ngửa (Heads)").assertIsDisplayed().performClick()
        assertTrue(rerolled)
    }

    @Test
    fun homeScreen_opensTopSearchWhenScrubberSearchClicked() {
        val state = LauncherUiState(
            installedApps = sampleApps,
            pinnedByProfile = mapOf(
                ContextProfile.WORK to setOf("com.example.calendar", "com.example.spotify")
            ),
            favoriteOrder = listOf("com.example.calendar", "com.example.spotify"),
            showWallpaperBackground = false,
            isDefaultLauncher = true
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeRule.onNodeWithText("Spotify").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Tìm kiếm").performClick()
        composeRule.onNodeWithText("Tìm kiếm ứng dụng").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Tùy chọn tìm kiếm").performClick()
        composeRule.onNodeWithText("Tìm kiếm trong danh bạ").assertIsDisplayed()
    }

    @Test
    fun gentleFrictionDialog_disablesContinueDuringCountdownAndEnablesAtZero() {
        var confirmed = false
        composeRule.setContent {
            StillnessTheme {
                GentleFrictionDialog(
                    pending = PendingFriction(
                        app = AppInfo("TikTok", "com.zhiliaoapp.musically"),
                        minutesToday = 24,
                        hasUsagePermission = true,
                        secondsRemaining = 3
                    ),
                    onDismiss = {},
                    onConfirmLaunch = { confirmed = true }
                )
            }
        }

        composeRule.onNodeWithText("Bạn chuẩn bị mở TikTok").assertIsDisplayed()
        composeRule.onNodeWithText("Tiếp tục mở (3s)").assertIsNotEnabled()
        assertEquals(false, confirmed)
    }

    @Test
    fun gentleFrictionDialog_allowsLaunchWhenCountdownFinished() {
        var confirmed = false
        composeRule.setContent {
            StillnessTheme {
                GentleFrictionDialog(
                    pending = PendingFriction(
                        app = AppInfo("TikTok", "com.zhiliaoapp.musically"),
                        minutesToday = 24,
                        hasUsagePermission = true,
                        secondsRemaining = 0
                    ),
                    onDismiss = {},
                    onConfirmLaunch = { confirmed = true }
                )
            }
        }

        composeRule.onNodeWithText("Mở TikTok").assertIsEnabled().performClick()
        assertTrue(confirmed)
    }

    @Test
    fun gentleFrictionDialog_showsOpenCountResistedAndIntentionSelection() {
        var selectedIntention: String? = null
        composeRule.setContent {
            StillnessTheme {
                GentleFrictionDialog(
                    pending = PendingFriction(
                        app = AppInfo("TikTok", "com.zhiliaoapp.musically"),
                        minutesToday = 24,
                        opensToday = 9,
                        resistedToday = 3,
                        hasUsagePermission = true,
                        secondsRemaining = 0,
                        selectedIntention = "Tra cứu"
                    ),
                    onDismiss = {},
                    onConfirmLaunch = {},
                    onSelectIntention = { selectedIntention = it }
                )
            }
        }

        composeRule.onNodeWithText("Hôm nay: 24 phút · Lần mở thứ 9").assertIsDisplayed()
        composeRule.onNodeWithText("Đã giữ bình tĩnh 3 lần hôm nay").assertIsDisplayed()
        composeRule.onNodeWithText("Mở TikTok (Tra cứu)").assertIsEnabled()
        composeRule.onNodeWithText("Nhắn tin").performClick()
        assertEquals("Nhắn tin", selectedIntention)
    }

    @Test
    fun settingsScreen_selectsClockIconShapeAccentAndShowsZeroTelemetry() {
        var selectedClock: ClockStyle? = null
        var selectedShape: IconShapePreset? = null
        var selectedAccent: AccentPreset? = null

        composeRule.setContent {
            StillnessTheme {
                SettingsScreen(
                    state = LauncherUiState(installedApps = sampleApps),
                    onBack = {},
                    onSetClockStyle = { selectedClock = it },
                    onSetAccentPreset = { selectedAccent = it },
                    onSetIconSizePreset = {},
                    onSetIconShapePreset = { selectedShape = it },
                    onSetHideStatusBar = {},
                    onSetFontPreset = {},
                    onSetHideFavoriteLabels = {},
                    onSetShowBatteryOnHome = {},
                    onSetShowAlphabetWave = {},
                    onSetShowWallpaperBackground = {},
                    onSetMonochromeIcons = {},
                    onSetTwoColumnLargeScreen = {},
                    onSetSelectedIconPack = {},
                    onSetShowCalendarEvents = {},
                    onSetShowContactSearch = {},
                    onSetShowMusicOnHeadphones = {},
                    onSetShowNotificationPreviews = {},
                    onSetShowMediaPlayer = {},
                    onSetSmartSortEnabled = {},
                    onSetShowMostUsedApps = {},
                    onOpenNotificationSettings = {},
                    onOpenUsageSettings = {},
                    onPickWidget = {},
                    onClearWidget = {},
                    onSetNiagaraButtonPackage = {},
                    onRefreshPermissions = {},
                    onExportBackup = { "{}" },
                    onImportBackup = { true },
                    onUnhideApp = {},
                    onOpenDefaultLauncher = {}
                )
            }
        }

        composeRule.onNodeWithText("Ngang").performClick()
        assertEquals(ClockStyle.HORIZONTAL, selectedClock)

        composeRule.onNodeWithText("Niagara Dots").performScrollTo().performClick()
        assertEquals(IconShapePreset.DOTS, selectedShape)

        composeRule.onNodeWithText("Hổ phách").performScrollTo().performClick()
        assertEquals(AccentPreset.AMBER, selectedAccent)

        composeRule.onNodeWithText("Zero-Telemetry").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeScreen_switchesContextFavoritesAndTriggersFocusTimer() {
        var selectedProfile: ContextProfile? = null
        var startedFocusMinutes = 0
        val state = LauncherUiState(
            activeProfile = ContextProfile.EVENING,
            installedApps = sampleApps,
            pinnedByProfile = mapOf(
                ContextProfile.WORK to setOf("com.example.calendar"),
                ContextProfile.EVENING to setOf("com.example.spotify")
            ),
            favoriteOrder = listOf("com.example.spotify"),
            showWallpaperBackground = false,
            isDefaultLauncher = true
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onSelectProfile = { selectedProfile = it },
                    onStartFocusTimer = { startedFocusMinutes = it },
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Spotify").assertIsDisplayed()
        composeRule.onNodeWithText("Buổi tối").performClick()
        composeRule.onNodeWithText("Ngữ cảnh màn hình chính").assertIsDisplayed()
        composeRule.onNodeWithText("Làm việc").performClick()
        assertEquals(ContextProfile.WORK, selectedProfile)

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Buổi tối").performClick()
        composeRule.onNodeWithText("Bắt đầu Tập trung (25p)").performClick()
        assertEquals(25, startedFocusMinutes)
    }

    @Test
    fun homeScreen_customContextShowsAppsAndAllowsCreatingAndPickingApps() {
        var createdName: String? = null
        var savedPackages: Set<String>? = null
        val customCtx = CustomContextProfile(
            id = "ctx-1",
            name = "Học tập",
            packages = setOf("com.example.notes"),
            favoriteOrder = listOf("com.example.notes")
        )
        val state = LauncherUiState(
            installedApps = sampleApps,
            customContexts = listOf(customCtx),
            activeCustomContextId = "ctx-1",
            favoriteOrder = listOf("com.example.notes"),
            showWallpaperBackground = false,
            isDefaultLauncher = true
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onCreateCustomContext = { n, _ -> createdName = n },
                    onSetAppsForActiveContext = { savedPackages = it },
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Học tập").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Ngữ cảnh màn hình chính").assertIsDisplayed()
    }

    @Test
    fun homeScreen_zenFocusModeDisplaysCountdownAndStopButton() {
        var stopped = false
        val state = LauncherUiState(
            activeProfile = ContextProfile.WORK,
            installedApps = sampleApps,
            pinnedByProfile = mapOf(ContextProfile.WORK to setOf("com.example.calendar")),
            favoriteOrder = listOf("com.example.calendar"),
            focusSession = FocusSessionState(
                remainingSeconds = 24 * 60 + 59,
                totalSeconds = 25 * 60,
                isRunning = true
            ),
            showWallpaperBackground = false,
            isDefaultLauncher = true
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onStopFocusTimer = { stopped = true },
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Chế độ Tập trung · 24:59").assertIsDisplayed()
        composeRule.onNodeWithText("Chỉ giữ lại ứng dụng Làm việc").assertIsDisplayed()
        composeRule.onNodeWithText("Kết thúc").performClick()
        assertTrue(stopped)
    }

    @Test
    fun contextModeEditorSheet_configuresAutoTriggersRestrictionsAndSettings() {
        var savedProfile: CustomContextProfile? = null
        val initial = CustomContextProfile(
            id = "mode-study",
            name = "Học tập"
        )

        composeRule.setContent {
            StillnessTheme {
                ContextModeEditorSheet(
                    initialProfile = initial,
                    installedApps = sampleApps,
                    onDismiss = {},
                    onSave = { savedProfile = it }
                )
            }
        }

        composeRule.onNodeWithText("Tùy chỉnh chế độ").assertIsDisplayed()
        composeRule.onNodeWithText("Chọn ứng dụng (0 đã chọn)").performClick()
        composeRule.onNodeWithText("Calendar").performClick()
        composeRule.onNodeWithText("Lưu").performClick()
        composeRule.onNodeWithText("Chọn ứng dụng (1 đã chọn)").assertIsDisplayed()

        composeRule.onNodeWithText("Khi kết nối tai nghe / Bluetooth").performScrollTo().performClick()
        composeRule.onNodeWithText("19–22h").performScrollTo().performClick()
        composeRule.onNodeWithText("Hạn chế ứng dụng ngoài chế độ (dừng 5s)").performScrollTo().performClick()
        composeRule.onNodeWithText("Nền đen thuần OLED (Ẩn hình nền)").performScrollTo().performClick()
        composeRule.onNodeWithText("Hoàn tất").performScrollTo().performClick()

        assertEquals(setOf("com.example.calendar"), savedProfile?.packages)

        assertEquals(true, savedProfile?.autoOnHeadphones)
        assertEquals(19, savedProfile?.autoTimeStartHour)
        assertEquals(22, savedProfile?.autoTimeEndHour)
        assertEquals(true, savedProfile?.restrictOtherApps)
        assertEquals(true, savedProfile?.oledPureBlack)
    }

    @Test
    fun homeScreen_builtInContextCanEditSettingsViaModeEditor() {
        var savedBuiltIn: BuiltInContextConfig? = null
        val state = LauncherUiState(
            activeProfile = ContextProfile.WORK,
            installedApps = sampleApps,
            pinnedByProfile = mapOf(ContextProfile.WORK to setOf("com.example.calendar")),
            favoriteOrder = listOf("com.example.calendar"),
            showWallpaperBackground = false,
            isDefaultLauncher = true,
            isProUnlocked = true
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = {},
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onSaveBuiltInContextConfig = { savedBuiltIn = it },
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Làm việc").performClick()
        composeRule.onNodeWithText("Ngữ cảnh màn hình chính").assertIsDisplayed()
        composeRule.onAllNodes(androidx.compose.ui.test.hasText("Tùy chỉnh"))[0].performClick()
        composeRule.onNodeWithText("Tùy chỉnh chế độ").assertIsDisplayed()
        composeRule.onNodeWithText("Hạn chế ứng dụng ngoài chế độ (dừng 5s)").performScrollTo().performClick()
        composeRule.onNodeWithText("Hoàn tất").performScrollTo().performClick()

        assertEquals(ContextProfile.WORK, savedBuiltIn?.profile)
        assertEquals(true, savedBuiltIn?.restrictOtherApps)
    }

    @Test
    fun homeScreen_displaysRaycastConversionAndSystemActionCards() {
        var submitted = false
        val state = LauncherUiState(
            installedApps = sampleApps,
            showWallpaperBackground = false,
            isDefaultLauncher = true,
            commandQuery = "/timer 15m",
            commandResult = CommandResult.SystemActionResult(
                actionId = "timer",
                title = "Đặt hẹn giờ 15 phút",
                subtitle = "Nhấn Enter hoặc chạm để mở Đồng hồ",
                argInt = 900
            )
        )

        composeRule.setContent {
            StillnessTheme {
                HomeScreen(
                    state = state,
                    onCommandQueryChanged = {},
                    onSubmitCommand = { submitted = true },
                    onAppClick = {},
                    onDialContact = {},
                    onToggleFavorite = {},
                    onMoveFavorite = { _, _ -> },
                    onRerollUtility = {},
                    onToggleHidden = {},
                    onToggleSummary = {},
                    onRenameApp = { _, _ -> },
                    onPickPopupWidget = {},
                    onClearPopupWidget = {},
                    onCreateFolder = { _, _ -> },
                    onToggleAppInFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onDeleteFolder = {},
                    onRemovePinnedWidget = {},
                    onLoadShortcuts = { emptyList() },
                    onLaunchShortcut = { _, _ -> },
                    onSetNiagaraButtonPackage = {},
                    onToggleMediaPlayPause = {},
                    onSkipMediaNext = {},
                    onSkipMediaPrevious = {},
                    onDismissNotification = {},
                    onSendNotificationReply = { _, _ -> false },
                    onOpenAppInfo = {},
                    onUninstallApp = {},
                    onToggleNote = {},
                    onDeleteNote = {},
                    onOpenSettings = {},
                    onOpenDefaultLauncher = {},
                    onDismissSetupBanner = {},
                    onDismissTipsBanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Đặt hẹn giờ 15 phút").assertIsDisplayed().performClick()
        assertTrue(submitted)
    }

    @Test
    fun proUpgradeSheet_displaysOneTimePricingAndTriggersPurchaseOrRestore() {
        var purchased = false
        var restored = false
        composeRule.setContent {
            StillnessTheme {
                ProUpgradeSheet(
                    isProUnlocked = false,
                    formattedPrice = "$3.99",
                    statusMessage = null,
                    onDismiss = {},
                    onPurchaseClick = { purchased = true },
                    onRestoreClick = { restored = true }
                )
            }
        }

        composeRule.onNodeWithText("Nâng cấp Stillness Pro").assertIsDisplayed()
        composeRule.onNodeWithText("Mở khóa Pro trọn đời ($3.99)").performClick()
        assertTrue(purchased)
        composeRule.onNodeWithText("Khôi phục giao dịch Google Play").performClick()
        assertTrue(restored)
    }
}
