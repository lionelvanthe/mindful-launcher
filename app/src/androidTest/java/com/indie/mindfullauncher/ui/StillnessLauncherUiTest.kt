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
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
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
}
