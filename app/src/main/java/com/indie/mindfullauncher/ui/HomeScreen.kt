package com.indie.mindfullauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.MediaPlaybackState
import com.indie.mindfullauncher.model.NotificationPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed interface AlphabetListEntry {
    data class Header(val letter: Char) : AlphabetListEntry
    data class AppItem(val app: AppInfo) : AlphabetListEntry
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: LauncherUiState,
    onCommandQueryChanged: (String) -> Unit,
    onSubmitCommand: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onDialContact: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleHidden: (String) -> Unit,
    onToggleSummary: (String) -> Unit,
    onRenameApp: (String, String?) -> Unit,
    onPickPopupWidget: (String) -> Unit,
    onClearPopupWidget: (String) -> Unit,
    onCreateFolder: (String, String?) -> Unit,
    onToggleAppInFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onLoadShortcuts: (String) -> List<AppShortcutItem>,
    onLaunchShortcut: (String, String) -> Unit,
    onSetNiagaraButtonPackage: (String?) -> Unit,
    onToggleMediaPlayPause: () -> Unit,
    onSkipMediaNext: () -> Unit,
    onSkipMediaPrevious: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onSendNotificationReply: (NotificationPreview, String) -> Boolean,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onToggleNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDefaultLauncher: () -> Unit,
    onDismissSetupBanner: () -> Unit,
    onDismissTipsBanner: () -> Unit
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var batteryPct by remember { mutableIntStateOf(-1) }

    DisposableEffect(context) {
        val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                now = System.currentTimeMillis()
            }
        }
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) batteryPct = (level * 100) / scale
            }
        }
        context.registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        val sticky = context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (sticky != null) {
            val level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) batteryPct = (level * 100) / scale
        }
        onDispose {
            context.unregisterReceiver(timeReceiver)
            context.unregisterReceiver(batteryReceiver)
        }
    }

    val timeStr = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)) }
    val hourStr = remember(now) { SimpleDateFormat("HH", Locale.getDefault()).format(Date(now)) }
    val minStr = remember(now) { SimpleDateFormat("mm", Locale.getDefault()).format(Date(now)) }
    val dateStr = remember(now) { SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(Date(now)) }

    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    var isAlphabetMode by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var optionsApp by remember { mutableStateOf<AppInfo?>(null) }
    var activeFolder by remember { mutableStateOf<LauncherFolder?>(null) }
    var folderTargetApp by remember { mutableStateOf<AppInfo?>(null) }
    var activeNotificationPair by remember { mutableStateOf<Pair<AppInfo, NotificationPreview>?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showFabCustomizer by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showAgendaDialog by remember { mutableStateOf(false) }
    var renameTargetApp by remember { mutableStateOf<AppInfo?>(null) }

    val isTwoColumn = LocalConfiguration.current.screenWidthDp >= 600 && state.twoColumnLargeScreen
    val musicSuggestions = remember(state.installedApps, state.showMusicOnHeadphones, state.isHeadphonesConnected) {
        if (!state.showMusicOnHeadphones || !state.isHeadphonesConnected) {
            emptyList()
        } else {
            val musicKeys = listOf("spotify", "music", "zing", "nhaccuatui", "soundcloud", "apple.android.music", "podcast")
            state.installedApps.filter { app ->
                val target = "${app.packageName} ${app.label}".lowercase()
                musicKeys.any { target.contains(it) }
            }.take(3)
        }
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val favoritePackages = remember(state.pinnedByProfile) {
        state.pinnedByProfile[ContextProfile.WORK].orEmpty()
    }
    val favoriteApps = remember(state.installedApps, favoritePackages, state.hiddenPackages) {
        state.installedApps.filter {
            it.packageName in favoritePackages && it.packageName !in state.hiddenPackages
        }
    }
    val oftenUsedApps = remember(
        state.installedApps,
        favoritePackages,
        state.hiddenPackages,
        state.weeklyUsageMillis,
        state.showMostUsedApps
    ) {
        if (!state.showMostUsedApps || state.weeklyUsageMillis.isEmpty()) {
            emptyList()
        } else {
            state.installedApps
                .filter {
                    it.packageName !in favoritePackages &&
                        it.packageName !in state.hiddenPackages &&
                        (state.weeklyUsageMillis[it.packageName] ?: 0L) > 0L
                }
                .sortedByDescending { state.weeklyUsageMillis[it.packageName] ?: 0L }
                .take(3)
        }
    }

    val visibleAllApps = remember(state.installedApps, state.hiddenPackages) {
        state.installedApps.filter { it.packageName !in state.hiddenPackages }
    }
    val alphabetEntries = remember(visibleAllApps, state.smartSortEnabled, state.weeklyUsageMillis) {
        buildList {
            val grouped = visibleAllApps.groupBy { app ->
                val first = app.label.trim().firstOrNull()?.uppercaseChar()
                if (first != null && first in 'A'..'Z') first else '#'
            }
            grouped.forEach { (letter, groupApps) ->
                add(AlphabetListEntry.Header(letter))
                val ordered = if (state.smartSortEnabled && state.weeklyUsageMillis.isNotEmpty()) {
                    groupApps.sortedByDescending { state.weeklyUsageMillis[it.packageName] ?: 0L }
                } else {
                    groupApps
                }
                ordered.forEach { add(AlphabetListEntry.AppItem(it)) }
            }
        }
    }
    val letterIndexMap = remember(alphabetEntries) {
        buildMap {
            alphabetEntries.forEachIndexed { index, entry ->
                if (entry is AlphabetListEntry.Header) put(entry.letter, index)
            }
        }
    }
    val scrubberLetters = remember(letterIndexMap) { letterIndexMap.keys.toList() }
    val alphabetListState = rememberLazyListState()
    val iconSizeDp = state.iconSizePreset.dpValue.dp

    val summaryItems = remember(state.notificationPreviews, state.summaryPackages, state.installedApps) {
        val appMap = state.installedApps.associateBy { it.packageName }
        state.summaryPackages.mapNotNull { pkg ->
            val preview = state.notificationPreviews[pkg] ?: return@mapNotNull null
            val app = appMap[pkg] ?: return@mapNotNull null
            app to preview
        }
    }

    BackHandler(enabled = isAlphabetMode || isSearchExpanded || state.commandQuery.isNotEmpty()) {
        when {
            state.commandQuery.isNotEmpty() -> onCommandQueryChanged("")
            isSearchExpanded -> {
                isSearchExpanded = false
                keyboardController?.hide()
            }
            isAlphabetMode -> isAlphabetMode = false
        }
    }

    val rootBackground = if (state.showWallpaperBackground) {
        Color.Black.copy(alpha = 0.36f)
    } else {
        colors.background
    }
    val onWallpaperShadow = remember(state.showWallpaperBackground) {
        if (state.showWallpaperBackground) {
            Shadow(color = Color.Black.copy(alpha = 0.65f), offset = Offset(0f, 3f), blurRadius = 6f)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                        if (totalDrag < -90f && !isSearchExpanded) {
                            isSearchExpanded = true
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 28.dp,
                    end = if (state.showAlphabetWave) 52.dp else 28.dp,
                    top = 24.dp,
                    bottom = 12.dp
                )
        ) {
            if (!isAlphabetMode && state.commandQuery.isEmpty() && !isTwoColumn) {
                NiagaraClockHeader(
                    timeStr = timeStr,
                    hourStr = hourStr,
                    minStr = minStr,
                    dateStr = dateStr.replaceFirstChar { it.uppercase() },
                    batteryPct = if (state.showBatteryOnHome && batteryPct >= 0) batteryPct else null,
                    calendarEvent = if (state.showCalendarEvents) state.nextCalendarEvent else null,
                    clockStyle = state.clockStyle,
                    textShadow = onWallpaperShadow,
                    onClickCalendar = {
                        if (state.hasCalendarPermission) showAgendaDialog = true
                    }
                )

                if (state.showMediaPlayer && state.mediaPlayback != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    NiagaraMediaCard(
                        media = state.mediaPlayback,
                        iconPackPackage = state.selectedIconPack,
                        monochromeIcons = state.monochromeIcons,
                        onTogglePlayPause = onToggleMediaPlayPause,
                        onNext = onSkipMediaNext,
                        onPrevious = onSkipMediaPrevious
                    )
                }

                if (state.pinnedWidgetId > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AndroidView(
                        factory = { ctx ->
                            WidgetHostHelper.createWidgetView(ctx, state.pinnedWidgetId)
                                ?: android.widget.FrameLayout(ctx)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp, max = 160.dp)
                    )
                }

                if (summaryItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSummaryDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tóm tắt thông báo (${summaryItems.size})",
                                color = colors.onSurface,
                                style = typography.labelLarge
                            )
                        }
                    }
                }

                val showOnboarding = !state.isSetupBannerDismissed && !state.isDefaultLauncher
                if (showOnboarding) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FirstRunSetupCard(
                        onOpenDefaultLauncher = onOpenDefaultLauncher,
                        onDismiss = onDismissSetupBanner
                    )
                }

                if (!state.isTipsBannerDismissed) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTipsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mẹo: Nhấn giữ app để tạo thư mục, ẩn app…",
                                color = colors.onSurfaceVariant,
                                style = typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissTipsBanner,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Ẩn mẹo",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (state.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "VIỆC CẦN LÀM",
                        color = colors.onSurfaceVariant,
                        style = typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    state.notes.take(3).forEach { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = note.isDone,
                                onCheckedChange = { onToggleNote(note.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.onSurfaceVariant,
                                    uncheckedColor = colors.onSurfaceVariant,
                                    checkmarkColor = colors.background
                                )
                            )
                            Text(
                                text = note.text,
                                color = if (note.isDone) colors.onSurfaceVariant else colors.onBackground,
                                style = typography.bodyLarge,
                                textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteNote(note.id) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa ghi chú",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else if (isAlphabetMode && state.commandQuery.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { isAlphabetMode = false }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại Yêu thích",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Yêu thích",
                            color = colors.onSurfaceVariant,
                            style = typography.labelLarge
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isTwoColumn && !isAlphabetMode && state.commandQuery.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxSize()
                    ) {
                        NiagaraClockHeader(
                            timeStr = timeStr,
                            hourStr = hourStr,
                            minStr = minStr,
                            dateStr = dateStr.replaceFirstChar { it.uppercase() },
                            batteryPct = if (state.showBatteryOnHome && batteryPct >= 0) batteryPct else null,
                            calendarEvent = if (state.showCalendarEvents) state.nextCalendarEvent else null,
                            clockStyle = state.clockStyle,
                            onClickCalendar = {
                                if (state.hasCalendarPermission) showAgendaDialog = true
                            }
                        )
                        if (state.showMediaPlayer && state.mediaPlayback != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            NiagaraMediaCard(
                                media = state.mediaPlayback,
                                iconPackPackage = state.selectedIconPack,
                                monochromeIcons = state.monochromeIcons,
                                onTogglePlayPause = onToggleMediaPlayPause,
                                onNext = onSkipMediaNext,
                                onPrevious = onSkipMediaPrevious
                            )
                        }
                        if (state.pinnedWidgetId > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AndroidView(
                                factory = { ctx ->
                                    WidgetHostHelper.createWidgetView(ctx, state.pinnedWidgetId)
                                        ?: android.widget.FrameLayout(ctx)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp, max = 160.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = if (!isAlphabetMode && state.commandQuery.isEmpty()) {
                        Alignment.BottomStart
                    } else {
                        Alignment.TopStart
                    }
                ) {
                when (val res = state.commandResult) {
                    is CommandResult.MathResult -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .clickable { clipboard.setText(AnnotatedString(res.formattedValue)) },
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Kết quả (chạm để sao chép)",
                                    color = colors.onSurfaceVariant,
                                    style = typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "= ${res.formattedValue}",
                                    color = colors.onSurface,
                                    style = typography.headlineMedium
                                )
                            }
                        }
                    }
                    is CommandResult.NoteAction -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .clickable { onSubmitCommand() },
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Nhấn Enter hoặc chạm để lưu ghi chú",
                                    color = colors.onSurfaceVariant,
                                    style = typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.noteText.ifEmpty { "Nhập nội dung sau dấu !" },
                                    color = colors.onSurface,
                                    style = typography.titleMedium
                                )
                            }
                        }
                    }
                    is CommandResult.AppMatches -> {
                        if (res.apps.isEmpty() && state.contactMatches.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart),
                                shape = RoundedCornerShape(18.dp),
                                color = colors.surface
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Không tìm thấy ứng dụng phù hợp",
                                        color = colors.onSurface,
                                        style = typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Thử từ khóa khác hoặc kéo thanh chữ cái bên phải.",
                                        color = colors.onSurfaceVariant,
                                        style = typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(onClick = { onCommandQueryChanged("") }) {
                                        Text("Xóa tìm kiếm")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalFadingEdge(topFadeHeight = 16.dp, bottomFadeHeight = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                reverseLayout = true
                            ) {
                                items(state.contactMatches, key = { "contact_${it.id}_${it.phoneNumber}" }) { contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onDialContact(contact.phoneNumber) }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = colors.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(contact.name, color = colors.onBackground, style = typography.titleMedium)
                                            Text(contact.phoneNumber, color = colors.onSurfaceVariant, style = typography.bodySmall)
                                        }
                                    }
                                }
                                items(res.apps, key = { it.packageName }) { app ->
                                    NiagaraAppRow(
                                        app = app,
                                        iconSize = iconSizeDp,
                                        showLabel = true,
                                        iconPackPackage = state.selectedIconPack,
                                        monochromeIcons = state.monochromeIcons,
                                        onClick = { onAppClick(app) },
                                        onLongClick = { optionsApp = app }
                                    )
                                }
                            }
                        }
                    }
                    CommandResult.Empty -> {
                        AnimatedContent(
                            targetState = isAlphabetMode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "home_list_mode"
                        ) { showAll ->
                            if (showAll) {
                                LazyColumn(
                                    state = alphabetListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalFadingEdge(topFadeHeight = 16.dp, bottomFadeHeight = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(
                                        items = alphabetEntries,
                                        key = { entry ->
                                            when (entry) {
                                                is AlphabetListEntry.Header -> "hdr_${entry.letter}"
                                                is AlphabetListEntry.AppItem -> entry.app.packageName
                                            }
                                        }
                                    ) { entry ->
                                        when (entry) {
                                            is AlphabetListEntry.Header -> {
                                                Text(
                                                    text = entry.letter.toString(),
                                                    color = colors.secondary,
                                                    style = typography.labelLarge,
                                                    modifier = Modifier.padding(
                                                        start = 12.dp,
                                                        top = 12.dp,
                                                        bottom = 4.dp
                                                    )
                                                )
                                            }
                                            is AlphabetListEntry.AppItem -> {
                                                NiagaraAppRow(
                                                    app = entry.app,
                                                    iconSize = iconSizeDp,
                                                    showLabel = true,
                                                    iconPackPackage = state.selectedIconPack,
                                                    monochromeIcons = state.monochromeIcons,
                                                    onClick = { onAppClick(entry.app) },
                                                    onLongClick = { optionsApp = entry.app }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (favoriteApps.isEmpty() && state.folders.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Chưa có ứng dụng yêu thích",
                                        color = colors.onSurface,
                                        style = typography.titleMedium
                                    )
                                    Text(
                                        text = "Kéo thanh A–Z bên phải hoặc vuốt lên để tìm kiếm, nhấn giữ ứng dụng để ghim.",
                                        color = colors.onSurfaceVariant,
                                        style = typography.bodyMedium
                                    )
                                    TextButton(onClick = { isAlphabetMode = true }) {
                                        Text("Mở danh sách A–Z")
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalFadingEdge(topFadeHeight = 16.dp, bottomFadeHeight = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(state.folders, key = { "folder_${it.id}" }) { folder ->
                                        NiagaraFolderRow(
                                            folder = folder,
                                            iconSize = iconSizeDp,
                                            showLabel = !state.hideFavoriteLabels,
                                            onClick = { activeFolder = folder }
                                        )
                                    }
                                    items(favoriteApps, key = { it.packageName }) { app ->
                                        val notifPreview = if (
                                            state.showNotificationPreviews &&
                                            app.packageName !in state.summaryPackages
                                        ) {
                                            state.notificationPreviews[app.packageName]
                                        } else {
                                            null
                                        }
                                        NiagaraAppRow(
                                            app = app,
                                            iconSize = iconSizeDp,
                                            showLabel = !state.hideFavoriteLabels,
                                            iconPackPackage = state.selectedIconPack,
                                            monochromeIcons = state.monochromeIcons,
                                            notificationPreview = notifPreview?.summary,
                                            onNotificationClick = if (notifPreview != null) {
                                                { activeNotificationPair = app to notifPreview }
                                            } else {
                                                null
                                            },
                                            onClick = { onAppClick(app) },
                                            onLongClick = { optionsApp = app }
                                        )
                                    }
                                    if (musicSuggestions.isNotEmpty()) {
                                        item(key = "hdr_headphones_music") {
                                            Row(
                                                modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Headphones,
                                                    contentDescription = null,
                                                    tint = colors.secondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "TAI NGHE ĐÃ KẾT NỐI",
                                                    color = colors.secondary,
                                                    style = typography.labelSmall
                                                )
                                            }
                                        }
                                        items(musicSuggestions, key = { "music_${it.packageName}" }) { app ->
                                            NiagaraAppRow(
                                                app = app,
                                                iconSize = iconSizeDp,
                                                showLabel = !state.hideFavoriteLabels,
                                                iconPackPackage = state.selectedIconPack,
                                                monochromeIcons = state.monochromeIcons,
                                                onClick = { onAppClick(app) },
                                                onLongClick = { optionsApp = app }
                                            )
                                        }
                                    }
                                    if (oftenUsedApps.isNotEmpty()) {
                                        item(key = "hdr_often_used") {
                                            Text(
                                                text = "HAY DÙNG",
                                                color = colors.onSurfaceVariant,
                                                style = typography.labelSmall,
                                                modifier = Modifier.padding(start = 10.dp, top = 12.dp, bottom = 2.dp)
                                            )
                                        }
                                        items(oftenUsedApps, key = { "often_${it.packageName}" }) { app ->
                                            NiagaraAppRow(
                                                app = app,
                                                iconSize = iconSizeDp,
                                                showLabel = !state.hideFavoriteLabels,
                                                iconPackPackage = state.selectedIconPack,
                                                monochromeIcons = state.monochromeIcons,
                                                onClick = { onAppClick(app) },
                                                onLongClick = { optionsApp = app }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(visible = isSearchExpanded || state.commandQuery.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.commandQuery,
                        onValueChange = onCommandQueryChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Tìm app, tính toán, ! ghi chú...",
                                color = colors.onSurfaceVariant,
                                style = typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (state.commandQuery.isNotEmpty()) {
                                        onCommandQueryChanged("")
                                    } else {
                                        isSearchExpanded = false
                                        keyboardController?.hide()
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Đóng tìm kiếm",
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSubmitCommand() }),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            focusedBorderColor = colors.outline,
                            unfocusedBorderColor = colors.surfaceVariant,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        )
                    )
                }
            }

            if (!isSearchExpanded && state.commandQuery.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Cài đặt",
                                tint = colors.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showTipsDialog = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "Hướng dẫn thao tác",
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                    if (!state.showAlphabetWave) {
                        TextButton(onClick = { isAlphabetMode = !isAlphabetMode }) {
                            Text(if (isAlphabetMode) "Yêu thích" else "Tất cả A–Z")
                        }
                    }
                }
            }
        }

        // Niagara Button (Customizable FAB: tap to Search or launch assigned app, long-press to customize)
        AnimatedVisibility(
            visible = !isSearchExpanded && state.commandQuery.isEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 18.dp)
        ) {
            val fabApp = remember(state.niagaraButtonPackage, state.installedApps) {
                state.niagaraButtonPackage?.let { pkg -> state.installedApps.firstOrNull { it.packageName == pkg } }
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .combinedClickable(
                        onClick = {
                            if (fabApp != null) onAppClick(fabApp) else isSearchExpanded = true
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFabCustomizer = true
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (fabApp != null) {
                    AppIcon(
                        packageName = fabApp.packageName,
                        contentDescription = fabApp.label,
                        size = 30.dp,
                        iconPackPackage = state.selectedIconPack,
                        monochrome = state.monochromeIcons
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm nhanh",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (state.showAlphabetWave && state.commandQuery.isEmpty() && scrubberLetters.isNotEmpty()) {
            AlphabetScrubber(
                letters = scrubberLetters,
                onLetterSelected = { letter ->
                    if (!isAlphabetMode) isAlphabetMode = true
                    letterIndexMap[letter]?.let { index ->
                        scope.launch { alphabetListState.scrollToItem(index) }
                    }
                },
                onFavoritesSelected = { isAlphabetMode = false },
                onSearchSelected = { isSearchExpanded = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 56.dp, bottom = 88.dp)
            )
        }
    }

    optionsApp?.let { selected ->
        var shortcuts by remember(selected.packageName) { mutableStateOf<List<AppShortcutItem>>(emptyList()) }
        LaunchedEffect(selected.packageName) {
            shortcuts = withContext(Dispatchers.IO) { onLoadShortcuts(selected.packageName) }
        }
        val popupWidgetId = state.appPopupWidgets[selected.packageName] ?: -1
        NiagaraAppOptionsDialog(
            app = selected,
            shortcuts = shortcuts,
            popupWidgetId = popupWidgetId,
            isFavorite = selected.packageName in favoritePackages,
            isHidden = selected.packageName in state.hiddenPackages,
            isSummarized = selected.packageName in state.summaryPackages,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.monochromeIcons,
            onDismiss = { optionsApp = null },
            onLaunchShortcut = { sc ->
                onLaunchShortcut(sc.packageName, sc.id)
                optionsApp = null
            },
            onToggleFavorite = {
                onToggleFavorite(selected.packageName)
                optionsApp = null
            },
            onAddToFolder = {
                folderTargetApp = selected
                optionsApp = null
            },
            onRenameApp = {
                renameTargetApp = selected
                optionsApp = null
            },
            onPickPopupWidget = {
                onPickPopupWidget(selected.packageName)
                optionsApp = null
            },
            onClearPopupWidget = {
                onClearPopupWidget(selected.packageName)
                optionsApp = null
            },
            onToggleSummary = {
                onToggleSummary(selected.packageName)
                optionsApp = null
            },
            onToggleHidden = {
                onToggleHidden(selected.packageName)
                optionsApp = null
            },
            onOpenAppInfo = {
                onOpenAppInfo(selected.packageName)
                optionsApp = null
            },
            onUninstall = {
                onUninstallApp(selected.packageName)
                optionsApp = null
            }
        )
    }

    activeFolder?.let { folder ->
        val freshFolder = state.folders.firstOrNull { it.id == folder.id }
        if (freshFolder == null) {
            activeFolder = null
        } else {
            val folderApps = remember(freshFolder.packages, state.installedApps) {
                val map = state.installedApps.associateBy { it.packageName }
                freshFolder.packages.mapNotNull { map[it] }
            }
            NiagaraFolderPopupDialog(
                folder = freshFolder,
                apps = folderApps,
                iconPackPackage = state.selectedIconPack,
                monochromeIcons = state.monochromeIcons,
                onDismiss = { activeFolder = null },
                onAppClick = { app ->
                    activeFolder = null
                    onAppClick(app)
                },
                onRemoveApp = { pkg -> onToggleAppInFolder(freshFolder.id, pkg) },
                onDeleteFolder = {
                    onDeleteFolder(freshFolder.id)
                    activeFolder = null
                }
            )
        }
    }

    folderTargetApp?.let { app ->
        AddToFolderDialog(
            app = app,
            folders = state.folders,
            onDismiss = { folderTargetApp = null },
            onSelectFolder = { folderId ->
                onToggleAppInFolder(folderId, app.packageName)
                folderTargetApp = null
            },
            onCreateFolder = { name ->
                onCreateFolder(name, app.packageName)
                folderTargetApp = null
            }
        )
    }

    activeNotificationPair?.let { (app, preview) ->
        NotificationPopupDialog(
            app = app,
            preview = preview,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.monochromeIcons,
            onDismiss = { activeNotificationPair = null },
            onOpenApp = {
                activeNotificationPair = null
                onAppClick(app)
            },
            onDismissNotification = {
                onDismissNotification(preview.key)
                activeNotificationPair = null
            },
            onSendReply = { text -> onSendNotificationReply(preview, text) }
        )
    }

    if (showSummaryDialog) {
        NotificationSummaryDialog(
            items = summaryItems,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.monochromeIcons,
            onDismiss = { showSummaryDialog = false },
            onOpenApp = { app ->
                showSummaryDialog = false
                onAppClick(app)
            },
            onDismissAll = {
                summaryItems.forEach { onDismissNotification(it.second.key) }
                showSummaryDialog = false
            }
        )
    }

    if (showFabCustomizer) {
        NiagaraFabActionDialog(
            installedApps = state.installedApps,
            selectedPackage = state.niagaraButtonPackage,
            onDismiss = { showFabCustomizer = false },
            onSelectPackage = { pkg ->
                onSetNiagaraButtonPackage(pkg)
                showFabCustomizer = false
            }
        )
    }

    if (showTipsDialog) {
        QuickTipsDialog(onDismiss = { showTipsDialog = false })
    }

    if (showAgendaDialog) {
        CalendarAgendaDialog(
            events = state.upcomingCalendarEvents,
            onDismiss = { showAgendaDialog = false }
        )
    }

    renameTargetApp?.let { app ->
        RenameAppDialog(
            app = app,
            onDismiss = { renameTargetApp = null },
            onSave = { newName ->
                onRenameApp(app.packageName, newName)
                renameTargetApp = null
            }
        )
    }
}

@Composable
private fun NiagaraClockHeader(
    timeStr: String,
    hourStr: String,
    minStr: String,
    dateStr: String,
    batteryPct: Int?,
    calendarEvent: CalendarEventPreview?,
    clockStyle: ClockStyle,
    textShadow: Shadow? = null,
    onClickCalendar: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val subLine = remember(dateStr, batteryPct) {
        if (batteryPct != null) "$dateStr · $batteryPct%" else dateStr
    }
    val eventTimeStr = remember(calendarEvent) {
        calendarEvent?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.startTimeMillis))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        when (clockStyle) {
            ClockStyle.MINIMALIST -> {
                Text(
                    timeStr,
                    color = colors.onBackground,
                    style = typography.displayLarge.copy(shadow = textShadow)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.COMPACT -> {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        timeStr,
                        color = colors.onBackground,
                        style = typography.headlineMedium.copy(shadow = textShadow)
                    )
                    Text(
                        subLine,
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium.copy(shadow = textShadow),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            ClockStyle.VERTICAL -> {
                Text(
                    hourStr,
                    color = colors.onBackground,
                    style = typography.displayLarge.copy(shadow = textShadow)
                )
                Text(
                    minStr,
                    color = colors.secondary,
                    style = typography.displayLarge.copy(shadow = textShadow)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
        }
        if (calendarEvent != null && eventTimeStr != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClickCalendar)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$eventTimeStr · ${calendarEvent.title}",
                    color = colors.onSurface,
                    style = typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NiagaraMediaCard(
    media: MediaPlaybackState,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                packageName = media.packageName,
                contentDescription = media.title,
                size = 34.dp,
                iconPackPackage = iconPackPackage,
                monochrome = monochromeIcons
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    color = colors.onSurface,
                    style = typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (media.artist.isNotEmpty()) {
                    Text(
                        text = media.artist,
                        color = colors.onSurfaceVariant,
                        style = typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onPrevious, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Trước", tint = colors.onSurfaceVariant)
            }
            IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (media.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (media.isPlaying) "Tạm dừng" else "Phát",
                    tint = colors.primary
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Tiếp", tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FirstRunSetupCard(
    onOpenDefaultLauncher: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onOpenDefaultLauncher) {
                Text("Đặt làm Launcher mặc định", style = typography.labelMedium)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Ẩn hướng dẫn",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NiagaraAppRow(
    app: AppInfo,
    iconSize: Dp,
    showLabel: Boolean,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    notificationPreview: String? = null,
    onNotificationClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .defaultMinSize(minHeight = (iconSize + 12.dp).coerceAtLeast(48.dp))
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(vertical = 7.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            packageName = app.packageName,
            contentDescription = app.label,
            size = iconSize,
            iconPackPackage = iconPackPackage,
            monochrome = monochromeIcons
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = colors.onBackground,
                    style = typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!notificationPreview.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surface,
                        modifier = if (onNotificationClick != null) {
                            Modifier.clickable(onClick = onNotificationClick)
                        } else {
                            Modifier
                        }
                    ) {
                        Text(
                            text = notificationPreview,
                            color = colors.onSurfaceVariant,
                            style = typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NiagaraFolderRow(
    folder: LauncherFolder,
    iconSize: Dp,
    showLabel: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .defaultMinSize(minHeight = (iconSize + 12.dp).coerceAtLeast(48.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 7.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = folder.name,
                tint = colors.secondary,
                modifier = Modifier.size(iconSize * 0.56f)
            )
        }
        if (showLabel) {
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    color = colors.onBackground,
                    style = typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.packages.size} ứng dụng",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall
                )
            }
        }
    }
}
