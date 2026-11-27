package com.indie.mindfullauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.BuiltInContextConfig
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.IconShapePreset
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.MediaPlaybackState
import com.indie.mindfullauncher.model.NotificationPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private sealed interface AlphabetListEntry {
    data class Header(val letter: Char) : AlphabetListEntry
    data class AppItem(val app: AppInfo) : AlphabetListEntry
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: LauncherUiState,
    onCommandQueryChanged: (String) -> Unit,
    onSubmitCommand: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onDialContact: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onRerollUtility: () -> Unit,
    onToggleHidden: (String) -> Unit,
    onToggleSummary: (String) -> Unit,
    onRenameApp: (String, String?) -> Unit,
    onPickPopupWidget: (String) -> Unit,
    onClearPopupWidget: (String) -> Unit,
    onCreateFolder: (String, String?) -> Unit,
    onToggleAppInFolder: (String, String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onRemovePinnedWidget: (Int) -> Unit,
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
    onSetShowContactSearch: (Boolean) -> Unit = {},
    onSetSmartSortEnabled: (Boolean) -> Unit = {},
    onToggleFriction: (String) -> Unit = {},
    onDismissPostSessionNudge: () -> Unit = {},
    onSelectProfile: (ContextProfile) -> Unit = {},
    onSelectCustomContext: (String) -> Unit = {},
    onCreateCustomContext: (String, Boolean) -> Unit = { _, _ -> },
    onSaveCustomContext: (com.indie.mindfullauncher.model.CustomContextProfile) -> Unit = {},
    onSaveBuiltInContextConfig: (BuiltInContextConfig) -> Unit = {},
    onDeleteCustomContext: (String) -> Unit = {},
    onRequirePro: () -> Unit = {},
    onSetAppsForActiveContext: (Set<String>) -> Unit = {},
    onStartFocusTimer: (Int) -> Unit = {},
    onStopFocusTimer: () -> Unit = {},
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
    val calNow = remember(now) { Calendar.getInstance().apply { timeInMillis = now } }
    val hourInt = calNow.get(Calendar.HOUR_OF_DAY)
    val minInt = calNow.get(Calendar.MINUTE)
    val dayOfWeekStr = remember(now) { SimpleDateFormat("EEEE", Locale("vi", "VN")).format(Date(now)) }
    val dateStr = remember(now) { SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(Date(now)) }
    val shortDateStr = remember(now) { SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(Date(now)) }

    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    var isAlphabetMode by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var lastSearchAppPkg by remember { mutableStateOf<String?>(null) }
    var optionsApp by remember { mutableStateOf<AppInfo?>(null) }
    var popupApp by remember { mutableStateOf<AppInfo?>(null) }
    var activeFolder by remember { mutableStateOf<LauncherFolder?>(null) }
    var folderTargetApp by remember { mutableStateOf<AppInfo?>(null) }
    var activeNotificationPair by remember { mutableStateOf<Pair<AppInfo, NotificationPreview>?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showFabCustomizer by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showAgendaDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }
    var showSearchOptionsSheet by remember { mutableStateOf(false) }
    var showContextSwitcherSheet by remember { mutableStateOf(false) }
    var showContextAppPicker by remember { mutableStateOf(false) }
    var editingCustomContext by remember { mutableStateOf<com.indie.mindfullauncher.model.CustomContextProfile?>(null) }
    var editingBuiltInProfile by remember { mutableStateOf<ContextProfile?>(null) }
    var isCreatingNewContext by remember { mutableStateOf(false) }
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

    val activeCustom = state.activeCustomContext
    val activeFavProfile = if (state.activeProfile == ContextProfile.ALL) {
        ContextProfile.WORK
    } else {
        state.activeProfile
    }
    val favoritePackages = remember(state.pinnedByProfile, activeFavProfile, activeCustom) {
        activeCustom?.packages ?: state.pinnedByProfile[activeFavProfile].orEmpty()
    }
    val ambientTargetColor = when {
        state.focusSession.isRunning -> Color(0xFF4DB6AC)
        activeCustom != null -> Color(activeCustom.colorHex)
        else -> Color(
            state.builtInContextConfigs[activeFavProfile]?.colorHex ?: when (activeFavProfile) {
                ContextProfile.EVENING -> 0xFFFFB74DL
                ContextProfile.SLEEP -> 0xFF5C6BC0L
                else -> 0xFF6B8CFFL
            }
        )
    }
    val ambientColor by animateColorAsState(
        targetValue = ambientTargetColor,
        animationSpec = tween(durationMillis = 650),
        label = "ambient_context_color"
    )
    val cycleContext: (Int) -> Unit = { delta ->
        val builtIns = listOf(ContextProfile.WORK, ContextProfile.EVENING, ContextProfile.SLEEP)
        val total = builtIns.size + state.customContexts.size
        val currentIdx = if (activeCustom != null) {
            builtIns.size + state.customContexts.indexOfFirst { it.id == activeCustom.id }.coerceAtLeast(0)
        } else {
            builtIns.indexOf(activeFavProfile).coerceAtLeast(0)
        }
        val nextIdx = ((currentIdx + delta) % total + total) % total
        if (nextIdx < builtIns.size) {
            onSelectProfile(builtIns[nextIdx])
        } else {
            onSelectCustomContext(state.customContexts[nextIdx - builtIns.size].id)
        }
    }
    val favoriteApps = remember(state.installedApps, favoritePackages, state.favoriteOrder, state.hiddenPackages) {
        val visibleFavs = state.installedApps.filter {
            it.packageName in favoritePackages && it.packageName !in state.hiddenPackages
        }
        val orderMap = state.favoriteOrder.withIndex().associate { (i, pkg) -> pkg to i }
        visibleFavs.sortedBy { orderMap[it.packageName] ?: Int.MAX_VALUE }
    }
    val oftenUsedApps = remember(
        state.installedApps,
        favoritePackages,
        state.hiddenPackages,
        state.weeklyUsageMillis,
        state.showMostUsedApps
    ) {
        if (!state.showMostUsedApps || state.focusSession.isRunning || state.weeklyUsageMillis.isEmpty()) {
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

    val isSearchActive = isSearchExpanded || state.commandQuery.isNotEmpty()
    val closeSearch = {
        if (state.commandQuery.isNotEmpty()) onCommandQueryChanged("")
        isSearchExpanded = false
        keyboardController?.hide()
    }

    BackHandler(enabled = isAlphabetMode || isSearchActive) {
        when {
            isSearchActive -> closeSearch()
            isAlphabetMode -> isAlphabetMode = false
        }
    }

    val rootBackground = when {
        isSearchActive && state.showWallpaperBackground -> Color.Black.copy(alpha = 0.64f)
        state.showWallpaperBackground -> Color.Black.copy(alpha = 0.36f)
        else -> colors.background
    }
    val onWallpaperShadow = remember(state.showWallpaperBackground) {
        if (state.showWallpaperBackground) {
            Shadow(color = Color.Black.copy(alpha = 0.65f), offset = Offset(0f, 3f), blurRadius = 6f)
        } else {
            null
        }
    }

    val swipeUpSearchConnection = remember(isAlphabetMode, isSearchExpanded, state.commandQuery) {
        object : NestedScrollConnection {
            private var upwardOverscroll = 0f

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput &&
                    !isSearchExpanded &&
                    !isAlphabetMode &&
                    state.commandQuery.isEmpty() &&
                    available.y < 0f
                ) {
                    upwardOverscroll += available.y
                    if (upwardOverscroll < -65f) {
                        upwardOverscroll = 0f
                        isSearchExpanded = true
                    }
                } else if (available.y >= 0f) {
                    upwardOverscroll = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                upwardOverscroll = 0f
                return Velocity.Zero
            }
        }
    }

    val alphabetPullDownToFavoritesConnection = remember(isAlphabetMode) {
        object : NestedScrollConnection {
            private var downwardOverscroll = 0f

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput &&
                    isAlphabetMode &&
                    available.y > 0f
                ) {
                    downwardOverscroll += available.y
                    if (downwardOverscroll > 80f) {
                        downwardOverscroll = 0f
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isAlphabetMode = false
                    }
                } else if (available.y <= 0f) {
                    downwardOverscroll = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                downwardOverscroll = 0f
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .pointerInput(isSearchActive) {
                detectTapGestures(
                    onTap = {
                        if (isSearchActive) closeSearch()
                    },
                    onLongPress = {
                        if (!isSearchActive) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenSettings()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                var shadeTriggered = false
                detectVerticalDragGestures(
                    onDragStart = {
                        totalDrag = 0f
                        shadeTriggered = false
                    },
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                        if (totalDrag < -90f && !isSearchExpanded) {
                            isSearchExpanded = true
                        } else if (totalDrag > 95f && !shadeTriggered && !isSearchExpanded && !isAlphabetMode) {
                            shadeTriggered = true
                            expandNotificationShade(context)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ambientColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.14f),
                    radius = size.width * 0.85f
                ),
                radius = size.width * 0.85f,
                center = Offset(size.width * 0.22f, size.height * 0.14f)
            )
            if (activeCustom == null && activeFavProfile == ContextProfile.SLEEP) {
                drawRect(color = Color.Black.copy(alpha = 0.24f))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isSearchActive) 24.dp else 28.dp,
                    end = when {
                        isSearchActive -> 24.dp
                        state.showAlphabetWave -> 52.dp
                        else -> 28.dp
                    },
                    top = if (isSearchActive) 16.dp else 24.dp,
                    bottom = 12.dp
                )
        ) {
            // Niagara-style borderless elevated Search Bar (res/NR.xml: 24dp horizontal margin, 6dp elevation, 18sp text, 48dp clear button only when non-empty)
            AnimatedVisibility(visible = isSearchActive) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.surfaceVariant,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .height(54.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    start = 24.dp,
                                    end = if (state.commandQuery.isNotEmpty()) 4.dp else 24.dp
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.commandQuery.isEmpty()) {
                                Text(
                                    text = "Tìm kiếm ứng dụng",
                                    color = colors.onSurfaceVariant,
                                    style = typography.bodyLarge.copy(fontSize = 18.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = state.commandQuery,
                                onValueChange = onCommandQueryChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    color = colors.onSurface
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(colors.secondary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { onSubmitCommand() })
                            )
                        }
                        if (state.commandQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onCommandQueryChanged("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    showSearchOptionsSheet = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Tùy chọn tìm kiếm",
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (state.commandQuery.isEmpty() && !isTwoColumn && !isSearchExpanded && !isAlphabetMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(state.customContexts.size, state.activeProfile, state.activeCustomContextId) {
                            var dragSum = 0f
                            var switched = false
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragSum = 0f
                                    switched = false
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragSum += dragAmount
                                    if (!switched && kotlin.math.abs(dragSum) > 56f) {
                                        switched = true
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        cycleContext(if (dragSum < 0f) 1 else -1)
                                    }
                                }
                            )
                        }
                ) {
                    NiagaraClockHeader(
                        timeStr = timeStr,
                        hourStr = hourStr,
                        minStr = minStr,
                        hourInt = hourInt,
                        minInt = minInt,
                        dayOfWeekStr = dayOfWeekStr.replaceFirstChar { it.uppercase() },
                        dateStr = dateStr.replaceFirstChar { it.uppercase() },
                        shortDateStr = shortDateStr,
                        batteryPct = if (state.showBatteryOnHome && batteryPct >= 0) batteryPct else null,
                        calendarEvent = if (state.showCalendarEvents) state.nextCalendarEvent else null,
                        clockStyle = state.clockStyle,
                        textShadow = onWallpaperShadow,
                        onClickCalendar = {
                            if (state.hasCalendarPermission) showAgendaDialog = true
                        }
                    )

                    val showOnboarding = !state.isSetupBannerDismissed && !state.isDefaultLauncher
                    val pendingNotesCount = state.notes.count { !it.isDone }
                    val activeContextLabel = activeCustom?.name ?: activeFavProfile.displayName

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { showContextSwitcherSheet = true },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (state.focusSession.isRunning) onStopFocusTimer() else onStartFocusTimer(25)
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ambientColor)
                        )
                        Text(
                            text = activeContextLabel,
                            color = colors.onSurfaceVariant,
                            style = typography.labelMedium.copy(shadow = onWallpaperShadow)
                        )
                    }

                    if (state.focusSession.isRunning) {
                        val focus = state.focusSession
                        val mm = focus.remainingSeconds / 60
                        val ss = focus.remainingSeconds % 60
                        val progress = if (focus.totalSeconds > 0) {
                            focus.remainingSeconds.toFloat() / focus.totalSeconds.toFloat()
                        } else {
                            0f
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = ambientColor.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(38.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawArc(
                                            color = ambientColor.copy(alpha = 0.25f),
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = ambientColor,
                                            startAngle = -90f,
                                            sweepAngle = 360f * progress,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = String.format(Locale.US, "%d", (focus.remainingSeconds + 59) / 60),
                                        color = ambientColor,
                                        style = typography.labelSmall
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format(Locale.US, "Chế độ Tập trung · %02d:%02d", mm, ss),
                                        color = colors.onSurface,
                                        style = typography.labelLarge
                                    )
                                    Text(
                                        text = "Chỉ giữ lại ứng dụng Làm việc",
                                        color = colors.onSurfaceVariant,
                                        style = typography.bodySmall
                                    )
                                }
                                TextButton(onClick = onStopFocusTimer) {
                                    Text("Kết thúc", color = ambientColor)
                                }
                            }
                        }
                    }
                    if (showOnboarding || summaryItems.isNotEmpty() || state.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (showOnboarding) {
                                HomeStatusPill(
                                    icon = Icons.Default.Home,
                                    label = "Đặt làm mặc định",
                                    accent = true,
                                    onClick = onOpenDefaultLauncher,
                                    onDismiss = onDismissSetupBanner
                                )
                            }
                            if (summaryItems.isNotEmpty()) {
                                HomeStatusPill(
                                    icon = Icons.Default.Notifications,
                                    label = "Thông báo (${summaryItems.size})",
                                    accent = true,
                                    onClick = { showSummaryDialog = true }
                                )
                            }
                            if (state.notes.isNotEmpty()) {
                                val noteLabel = if (pendingNotesCount > 0) {
                                    "$pendingNotesCount việc cần làm"
                                } else {
                                    "Ghi chú (${state.notes.size})"
                                }
                                HomeStatusPill(
                                    icon = Icons.Default.CheckCircleOutline,
                                    label = noteLabel,
                                    accent = false,
                                    onClick = { showNotesDialog = true }
                                )
                            }
                        }
                    }

                    state.postSessionNudge?.let { nudge ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bạn vừa dùng ${nudge.app.label} ${nudge.sessionMinutes} phút",
                                        color = colors.onSurface,
                                        style = typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (nudge.opensToday > 0) {
                                            "Lần mở thứ ${nudge.opensToday} hôm nay · Nghỉ mắt một chút nhé?"
                                        } else {
                                            "Hít thở một chút trước khi làm việc tiếp nhé."
                                        },
                                        color = colors.onSurfaceVariant,
                                        style = typography.bodySmall
                                    )
                                }
                                TextButton(onClick = onDismissPostSessionNudge) {
                                    Text("Đã hiểu", color = colors.primary)
                                }
                            }
                        }
                    }

                    if (state.showMediaPlayer && state.mediaPlayback != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        NiagaraMediaCard(
                            media = state.mediaPlayback,
                            iconPackPackage = state.selectedIconPack,
                            monochromeIcons = state.effectiveMonochromeIcons,
                            onTogglePlayPause = onToggleMediaPlayPause,
                            onNext = onSkipMediaNext,
                            onPrevious = onSkipMediaPrevious
                        )
                    }

                    if (state.pinnedWidgetIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        NiagaraWidgetStack(widgetIds = state.pinnedWidgetIds)
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
                            hourInt = hourInt,
                            minInt = minInt,
                            dayOfWeekStr = dayOfWeekStr.replaceFirstChar { it.uppercase() },
                            dateStr = dateStr.replaceFirstChar { it.uppercase() },
                            shortDateStr = shortDateStr,
                            batteryPct = if (state.showBatteryOnHome && batteryPct >= 0) batteryPct else null,
                            calendarEvent = if (state.showCalendarEvents) state.nextCalendarEvent else null,
                            clockStyle = state.clockStyle,
                            textShadow = onWallpaperShadow,
                            onClickCalendar = {
                                if (state.hasCalendarPermission) showAgendaDialog = true
                            }
                        )
                        state.postSessionNudge?.let { nudge ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bạn vừa dùng ${nudge.app.label} ${nudge.sessionMinutes} phút",
                                        color = colors.onSurface,
                                        style = typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (nudge.opensToday > 0) {
                                            "Lần mở thứ ${nudge.opensToday} hôm nay · Nghỉ mắt một chút nhé?"
                                        } else {
                                            "Hít thở một chút trước khi làm việc tiếp nhé."
                                        },
                                        color = colors.onSurfaceVariant,
                                        style = typography.bodySmall
                                    )
                                }
                                TextButton(onClick = onDismissPostSessionNudge) {
                                    Text("Đã hiểu", color = colors.primary)
                                }
                            }
                        }
                    }

                    if (state.showMediaPlayer && state.mediaPlayback != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            NiagaraMediaCard(
                                media = state.mediaPlayback,
                                iconPackPackage = state.selectedIconPack,
                                monochromeIcons = state.effectiveMonochromeIcons,
                                onTogglePlayPause = onToggleMediaPlayPause,
                                onNext = onSkipMediaNext,
                                onPrevious = onSkipMediaPrevious
                            )
                        }
                        if (state.pinnedWidgetIds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            NiagaraWidgetStack(widgetIds = state.pinnedWidgetIds)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = if (!isAlphabetMode && !isSearchActive) {
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
                                .align(Alignment.TopStart)
                                .clickable { clipboard.setText(AnnotatedString(res.formattedValue)) },
                            shape = RoundedCornerShape(16.dp),
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
                    is CommandResult.ConversionResult -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .clickable { clipboard.setText(AnnotatedString(res.formattedValue)) },
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "${res.fromExpr} (chạm để sao chép)",
                                    color = colors.onSurfaceVariant,
                                    style = typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${res.formattedValue} ${res.unitLabel}",
                                    color = colors.primary,
                                    style = typography.headlineMedium
                                )
                            }
                        }
                    }
                    is CommandResult.SystemActionResult -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .clickable { onSubmitCommand() },
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = res.title,
                                    color = colors.primary,
                                    style = typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.subtitle,
                                    color = colors.onSurfaceVariant,
                                    style = typography.bodySmall
                                )
                            }
                        }
                    }
                    is CommandResult.UtilityResult -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRerollUtility()
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "${res.title} (${res.hint})",
                                    color = colors.onSurfaceVariant,
                                    style = typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.value,
                                    color = colors.secondary,
                                    style = typography.headlineMedium
                                )
                            }
                        }
                    }
                    is CommandResult.NoteAction -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .clickable { onSubmitCommand() },
                            shape = RoundedCornerShape(16.dp),
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
                                    .align(Alignment.TopStart),
                                shape = RoundedCornerShape(16.dp),
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
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(state.contactMatches, key = { "contact_${it.id}_${it.phoneNumber}" }) { contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 52.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onDialContact(contact.phoneNumber) }
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
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
                                        monochromeIcons = state.effectiveMonochromeIcons,
                                        iconShape = state.iconShapePreset,
                                        textShadow = onWallpaperShadow,
                                        onClick = {
                                            lastSearchAppPkg = app.packageName
                                            closeSearch()
                                            onAppClick(app)
                                        },
                                        onLongClick = { optionsApp = app },
                                        onSwipeRight = { popupApp = app }
                                    )
                                }
                            }
                        }
                    }
                    CommandResult.Empty -> {
                        if (isSearchActive) {
                            val searchSuggestions = remember(
                                lastSearchAppPkg,
                                visibleAllApps,
                                oftenUsedApps
                            ) {
                                buildList<Pair<AppInfo, String>> {
                                    val seen = mutableSetOf<String>()
                                    lastSearchAppPkg?.let { pkg ->
                                        visibleAllApps.firstOrNull { it.packageName == pkg }?.let { app ->
                                            if (seen.add(app.packageName)) add(app to "Kết quả cuối")
                                        }
                                    }
                                    visibleAllApps
                                        .filter { it.installedAtMillis > 0L && it.packageName !in seen }
                                        .sortedByDescending { it.installedAtMillis }
                                        .take(3)
                                        .forEach { app ->
                                            if (seen.add(app.packageName)) add(app to "Được cài đặt gần đây")
                                        }
                                    oftenUsedApps
                                        .filter { it.packageName !in seen }
                                        .take(3)
                                        .forEach { app ->
                                            if (seen.add(app.packageName)) add(app to "Được tìm kiếm thường xuyên")
                                        }
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalFadingEdge(topFadeHeight = 12.dp, bottomFadeHeight = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(searchSuggestions, key = { "suggest_${it.first.packageName}" }) { (app, subtitle) ->
                                    NiagaraAppRow(
                                        app = app,
                                        iconSize = iconSizeDp,
                                        showLabel = true,
                                        iconPackPackage = state.selectedIconPack,
                                        monochromeIcons = state.effectiveMonochromeIcons,
                                        iconShape = state.iconShapePreset,
                                        subtitle = subtitle,
                                        textShadow = onWallpaperShadow,
                                        onClick = {
                                            lastSearchAppPkg = app.packageName
                                            closeSearch()
                                            onAppClick(app)
                                        },
                                        onLongClick = { optionsApp = app },
                                        onSwipeRight = { popupApp = app }
                                    )
                                }
                            }
                        } else AnimatedContent(
                            targetState = isAlphabetMode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "home_list_mode"
                        ) { showAll ->
                            if (showAll) {
                                LazyColumn(
                                    state = alphabetListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(alphabetPullDownToFavoritesConnection)
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
                                                // Niagara home_tile_section_header (res/4c.xml): 20sp
                                                Text(
                                                    text = entry.letter.toString(),
                                                    color = colors.secondary,
                                                    style = typography.titleMedium.copy(
                                                        fontSize = 20.sp,
                                                        shadow = onWallpaperShadow
                                                    ),
                                                    modifier = Modifier.padding(
                                                        start = 12.dp,
                                                        top = 16.dp,
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
                                                    monochromeIcons = state.effectiveMonochromeIcons,
                                                    iconShape = state.iconShapePreset,
                                                    textShadow = onWallpaperShadow,
                                                    onClick = { onAppClick(entry.app) },
                                                    onLongClick = { optionsApp = entry.app },
                                                    onSwipeRight = { popupApp = entry.app }
                                                )
                                            }
                                        }
                                    }
                                    item(key = "az_footer_actions") {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp, bottom = 24.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable(onClick = onOpenSettings)
                                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null,
                                                    tint = colors.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = "Cài đặt Stillness",
                                                    color = colors.onSurfaceVariant,
                                                    style = typography.titleMedium
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { showTipsDialog = true }
                                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                                    contentDescription = null,
                                                    tint = colors.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = "Hướng dẫn thao tác",
                                                    color = colors.onSurfaceVariant,
                                                    style = typography.titleMedium
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { showUsageDialog = true }
                                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BarChart,
                                                    contentDescription = null,
                                                    tint = colors.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = "Thống kê sử dụng (7 ngày)",
                                                    color = colors.onSurfaceVariant,
                                                    style = typography.titleMedium
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
                                        .nestedScroll(swipeUpSearchConnection)
                                        .verticalFadingEdge(topFadeHeight = 16.dp, bottomFadeHeight = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(state.folders, key = { "folder_${it.id}" }) { folder ->
                                        NiagaraFolderRow(
                                            folder = folder,
                                            iconSize = iconSizeDp,
                                            showLabel = !state.hideFavoriteLabels,
                                            iconPackPackage = state.selectedIconPack,
                                            monochromeIcons = state.effectiveMonochromeIcons,
                                            iconShape = state.iconShapePreset,
                                            textShadow = onWallpaperShadow,
                                            onClick = { activeFolder = folder }
                                        )
                                    }
                                    items(favoriteApps, key = { it.packageName }) { app ->
                                        val notifPreview = if (
                                            state.effectiveShowNotifications &&
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
                                            monochromeIcons = state.effectiveMonochromeIcons,
                                            iconShape = state.iconShapePreset,
                                            notificationPreview = notifPreview,
                                            textShadow = onWallpaperShadow,
                                            onNotificationClick = if (notifPreview != null) {
                                                { activeNotificationPair = app to notifPreview }
                                            } else {
                                                null
                                            },
                                            onClick = {
                                            lastSearchAppPkg = app.packageName
                                            closeSearch()
                                            onAppClick(app)
                                        },
                                            onLongClick = { optionsApp = app },
                                            onSwipeRight = { popupApp = app }
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
                                                monochromeIcons = state.effectiveMonochromeIcons,
                                                iconShape = state.iconShapePreset,
                                                textShadow = onWallpaperShadow,
                                                onClick = {
                                            lastSearchAppPkg = app.packageName
                                            closeSearch()
                                            onAppClick(app)
                                        },
                                                onLongClick = { optionsApp = app },
                                                onSwipeRight = { popupApp = app }
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
                                                monochromeIcons = state.effectiveMonochromeIcons,
                                                iconShape = state.iconShapePreset,
                                                textShadow = onWallpaperShadow,
                                                onClick = {
                                            lastSearchAppPkg = app.packageName
                                            closeSearch()
                                            onAppClick(app)
                                        },
                                                onLongClick = { optionsApp = app },
                                                onSwipeRight = { popupApp = app }
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

            if (!isSearchExpanded && state.commandQuery.isEmpty() && !state.showAlphabetWave) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = { isAlphabetMode = !isAlphabetMode }) {
                        Text(if (isAlphabetMode) "Yêu thích" else "Tất cả A–Z")
                    }
                }
            }
        }

        if (state.showAlphabetWave && !isSearchActive && !state.focusSession.isRunning && scrubberLetters.isNotEmpty()) {
            AlphabetScrubber(
                letters = scrubberLetters,
                isFavoritesActive = !isAlphabetMode,
                onLetterSelected = { letter ->
                    if (isSearchExpanded) {
                        isSearchExpanded = false
                        keyboardController?.hide()
                    }
                    if (!isAlphabetMode) isAlphabetMode = true
                    letterIndexMap[letter]?.let { index ->
                        scope.launch { alphabetListState.scrollToItem(index) }
                    }
                },
                onFavoritesSelected = {
                    if (isSearchExpanded) {
                        isSearchExpanded = false
                        keyboardController?.hide()
                    }
                    isAlphabetMode = false
                },
                onSearchSelected = { isSearchExpanded = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 36.dp, bottom = 24.dp)
            )
        }
    }

    popupApp?.let { selected ->
        var shortcuts by remember(selected.packageName) { mutableStateOf<List<AppShortcutItem>>(emptyList()) }
        LaunchedEffect(selected.packageName) {
            shortcuts = withContext(Dispatchers.IO) { onLoadShortcuts(selected.packageName) }
        }
        val popupWidgetId = state.appPopupWidgets[selected.packageName] ?: -1
        val notifPreview = if (state.effectiveShowNotifications) {
            state.notificationPreviews[selected.packageName]
        } else {
            null
        }
        NiagaraAppPopupSheet(
            app = selected,
            shortcuts = shortcuts,
            popupWidgetId = popupWidgetId,
            notificationPreview = notifPreview,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.effectiveMonochromeIcons,
            onDismiss = { popupApp = null },
            onOpenApp = {
                popupApp = null
                onAppClick(selected)
            },
            onOpenOptions = {
                popupApp = null
                optionsApp = selected
            },
            onLaunchShortcut = { sc ->
                onLaunchShortcut(sc.packageName, sc.id)
                popupApp = null
            },
            onPickPopupWidget = {
                popupApp = null
                onPickPopupWidget(selected.packageName)
            },
            onDismissNotification = { key ->
                onDismissNotification(key)
            },
            onSendReply = onSendNotificationReply
        )
    }

    optionsApp?.let { selected ->
        var shortcuts by remember(selected.packageName) { mutableStateOf<List<AppShortcutItem>>(emptyList()) }
        LaunchedEffect(selected.packageName) {
            shortcuts = withContext(Dispatchers.IO) { onLoadShortcuts(selected.packageName) }
        }
        val popupWidgetId = state.appPopupWidgets[selected.packageName] ?: -1
        val favIndex = favoriteApps.indexOfFirst { it.packageName == selected.packageName }
        NiagaraAppOptionsDialog(
            app = selected,
            shortcuts = shortcuts,
            popupWidgetId = popupWidgetId,
            isFavorite = selected.packageName in favoritePackages,
            canMoveUp = favIndex > 0,
            canMoveDown = favIndex in 0 until favoriteApps.lastIndex,
            isHidden = selected.packageName in state.hiddenPackages,
            isSummarized = selected.packageName in state.summaryPackages,
            isFrictionEnabled = selected.packageName in state.frictionPackages,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.effectiveMonochromeIcons,
            onDismiss = { optionsApp = null },
            onLaunchShortcut = { sc ->
                onLaunchShortcut(sc.packageName, sc.id)
                optionsApp = null
            },
            onToggleFavorite = {
                onToggleFavorite(selected.packageName)
                optionsApp = null
            },
            onMoveFavoriteUp = {
                onMoveFavorite(selected.packageName, -1)
                optionsApp = null
            },
            onMoveFavoriteDown = {
                onMoveFavorite(selected.packageName, 1)
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
            onToggleFriction = {
                onToggleFriction(selected.packageName)
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
                monochromeIcons = state.effectiveMonochromeIcons,
                onDismiss = { activeFolder = null },
                onAppClick = { app ->
                    activeFolder = null
                    onAppClick(app)
                },
                onRemoveApp = { pkg -> onToggleAppInFolder(freshFolder.id, pkg) },
                onRenameFolder = { newName -> onRenameFolder(freshFolder.id, newName) },
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
            monochromeIcons = state.effectiveMonochromeIcons,
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
            monochromeIcons = state.effectiveMonochromeIcons,
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

    if (showNotesDialog) {
        QuickNotesDialog(
            notes = state.notes,
            onToggleNote = onToggleNote,
            onDeleteNote = onDeleteNote,
            onDismiss = { showNotesDialog = false }
        )
    }

    if (showAgendaDialog) {
        CalendarAgendaDialog(
            events = state.upcomingCalendarEvents,
            onDismiss = { showAgendaDialog = false }
        )
    }

    if (showUsageDialog) {
        ScreenTimeUsageDialog(
            installedApps = state.installedApps,
            weeklyUsageMillis = state.weeklyUsageMillis,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.effectiveMonochromeIcons,
            onDismiss = { showUsageDialog = false }
        )
    }

    if (showSearchOptionsSheet) {
        NiagaraSearchOptionsSheet(
            showContactSearch = state.showContactSearch,
            smartSortEnabled = state.smartSortEnabled,
            hasLastResult = lastSearchAppPkg != null,
            onDismiss = { showSearchOptionsSheet = false },
            onToggleContactSearch = onSetShowContactSearch,
            onToggleSmartSort = onSetSmartSortEnabled,
            onQuickInsert = { cmd ->
                showSearchOptionsSheet = false
                isSearchExpanded = true
                onCommandQueryChanged(cmd)
            },
            onClearLastResult = { lastSearchAppPkg = null },
            onOpenSettings = {
                closeSearch()
                onOpenSettings()
            }
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

    if (showContextSwitcherSheet) {
        ContextSwitcherSheet(
            activeProfile = state.activeProfile,
            activeCustomContextId = state.activeCustomContextId,
            customContexts = state.customContexts,
            builtInConfigs = state.builtInContextConfigs,
            pinnedByProfile = state.pinnedByProfile,
            isFocusRunning = state.focusSession.isRunning,
            onDismiss = { showContextSwitcherSheet = false },
            onSelectBuiltIn = onSelectProfile,
            onSelectCustom = onSelectCustomContext,
            onPickAppsForBuiltIn = { profile ->
                onSelectProfile(profile)
                showContextAppPicker = true
            },
            onPickAppsForCustom = { ctx ->
                onSelectCustomContext(ctx.id)
                showContextAppPicker = true
            },
            onEditBuiltInContext = { profile ->
                editingBuiltInProfile = profile
            },
            onEditCustomContext = { ctx ->
                editingCustomContext = ctx
                isCreatingNewContext = false
            },
            onCreateCustomContext = {
                if (state.customContexts.isNotEmpty() && !state.isProUnlocked) {
                    onRequirePro()
                } else {
                    editingCustomContext = null
                    isCreatingNewContext = true
                }
            },
            onStartFocus = { onStartFocusTimer(25) },
            onStopFocus = onStopFocusTimer
        )
    }

    if (showContextAppPicker) {
        val contextTitle = activeCustom?.name ?: activeFavProfile.displayName
        ContextAppPickerDialog(
            contextTitle = contextTitle,
            isCustomContext = activeCustom != null,
            installedApps = state.installedApps,
            selectedPackages = favoritePackages,
            iconPackPackage = state.selectedIconPack,
            monochromeIcons = state.effectiveMonochromeIcons,
            onDismiss = { showContextAppPicker = false },
            onSave = onSetAppsForActiveContext,
            onDeleteContext = activeCustom?.let { ctx -> { onDeleteCustomContext(ctx.id) } }
        )
    }

    if (isCreatingNewContext || editingCustomContext != null) {
        ContextModeEditorSheet(
            initialProfile = editingCustomContext,
            isBuiltIn = false,
            isProUnlocked = state.isProUnlocked,
            installedApps = state.installedApps,
            initialPackages = editingCustomContext?.packages ?: emptySet(),
            iconPackPackage = state.selectedIconPack,
            onDismiss = {
                isCreatingNewContext = false
                editingCustomContext = null
            },
            onSave = { saved ->
                onSaveCustomContext(saved)
            },
            onDelete = { id ->
                onDeleteCustomContext(id)
            },
            onRequirePro = onRequirePro
        )
    }

    editingBuiltInProfile?.let { profile ->
        val cfg = state.builtInContextConfigs[profile] ?: BuiltInContextConfig(
            profile = profile,
            colorHex = when (profile) {
                ContextProfile.EVENING -> 0xFFFFB74DL
                ContextProfile.SLEEP -> 0xFF5C6BC0L
                else -> 0xFF6B8CFFL
            }
        )
        val builtInPkgs = state.pinnedByProfile[profile].orEmpty()
        val proxy = com.indie.mindfullauncher.model.CustomContextProfile(
            id = profile.name,
            name = profile.displayName,
            colorHex = cfg.colorHex,
            packages = builtInPkgs,
            restrictOtherApps = cfg.restrictOtherApps,
            hideNotifications = cfg.hideNotifications,
            monochromeIcons = cfg.monochromeIcons,
            oledPureBlack = cfg.oledPureBlack,
            muteMediaOnEnter = cfg.muteMediaOnEnter,
            autoTimeStartHour = cfg.autoTimeStartHour,
            autoTimeEndHour = cfg.autoTimeEndHour,
            autoOnHeadphones = cfg.autoOnHeadphones,
            autoOnCharging = cfg.autoOnCharging
        )
        ContextModeEditorSheet(
            initialProfile = proxy,
            isBuiltIn = true,
            isProUnlocked = state.isProUnlocked,
            installedApps = state.installedApps,
            initialPackages = builtInPkgs,
            iconPackPackage = state.selectedIconPack,
            onDismiss = { editingBuiltInProfile = null },
            onSavePackages = { updatedPkgs ->
                onSelectProfile(profile)
                onSetAppsForActiveContext(updatedPkgs)
            },
            onSave = { edited ->
                onSaveBuiltInContextConfig(
                    BuiltInContextConfig(
                        profile = profile,
                        colorHex = edited.colorHex,
                        restrictOtherApps = edited.restrictOtherApps,
                        hideNotifications = edited.hideNotifications,
                        monochromeIcons = edited.monochromeIcons,
                        oledPureBlack = edited.oledPureBlack,
                        muteMediaOnEnter = edited.muteMediaOnEnter,
                        autoTimeStartHour = edited.autoTimeStartHour,
                        autoTimeEndHour = edited.autoTimeEndHour,
                        autoOnHeadphones = edited.autoOnHeadphones,
                        autoOnCharging = edited.autoOnCharging
                    )
                )
            },
            onRequirePro = onRequirePro
        )
    }
}

@Composable
private fun NiagaraClockHeader(
    timeStr: String,
    hourStr: String,
    minStr: String,
    hourInt: Int,
    minInt: Int,
    dayOfWeekStr: String,
    dateStr: String,
    shortDateStr: String,
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
    val shortSubLine = remember(shortDateStr, timeStr, batteryPct) {
        val base = "$shortDateStr · $timeStr"
        if (batteryPct != null) "$base · $batteryPct%" else base
    }
    val eventTimeStr = remember(calendarEvent) {
        calendarEvent?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.startTimeMillis))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        when (clockStyle) {
            ClockStyle.CLASSIC -> {
                // Niagara BasicClockView: default_clock_text_size = 48dp
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
            ClockStyle.HORIZONTAL -> {
                // Niagara HorizontalClockView: hours = 60sp, minutes = 42sp, gap = 4dp
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        hourStr,
                        color = colors.onBackground,
                        style = typography.displayLarge.copy(
                            fontSize = 60.sp,
                            lineHeight = 62.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            shadow = textShadow
                        )
                    )
                    Text(
                        minStr,
                        color = colors.secondary,
                        style = typography.displayLarge.copy(
                            fontSize = 42.sp,
                            lineHeight = 46.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                            shadow = textShadow
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.VERTICAL -> {
                // Niagara VerticalClockView: stacked 84sp hours & minutes
                Text(
                    hourStr,
                    color = colors.onBackground,
                    style = typography.displayLarge.copy(
                        fontSize = 84.sp,
                        lineHeight = 76.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                        shadow = textShadow
                    )
                )
                Text(
                    minStr,
                    color = colors.secondary,
                    style = typography.displayLarge.copy(
                        fontSize = 84.sp,
                        lineHeight = 76.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                        shadow = textShadow
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.ANALOG -> {
                // Niagara AnalogClockView (res/fk.xml, max 128dp 1:1)
                val dialColor = colors.onBackground
                val accentColor = colors.secondary
                val surfaceBg = colors.surface.copy(alpha = 0.7f)
                Canvas(
                    modifier = Modifier
                        .size(112.dp)
                        .padding(4.dp)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = surfaceBg, radius = radius, center = center)
                    for (i in 0 until 12) {
                        val angleRad = (i * 30.0 - 90.0) * PI / 180.0
                        val tickStart = radius * if (i % 3 == 0) 0.78f else 0.86f
                        val tickEnd = radius * 0.94f
                        drawLine(
                            color = dialColor.copy(alpha = if (i % 3 == 0) 0.75f else 0.35f),
                            start = Offset(
                                center.x + (tickStart * cos(angleRad)).toFloat(),
                                center.y + (tickStart * sin(angleRad)).toFloat()
                            ),
                            end = Offset(
                                center.x + (tickEnd * cos(angleRad)).toFloat(),
                                center.y + (tickEnd * sin(angleRad)).toFloat()
                            ),
                            strokeWidth = if (i % 3 == 0) 3f else 2f,
                            cap = StrokeCap.Round
                        )
                    }
                    val hourAngle = (((hourInt % 12) + minInt / 60.0) * 30.0 - 90.0) * PI / 180.0
                    val minAngle = (minInt * 6.0 - 90.0) * PI / 180.0
                    drawLine(
                        color = dialColor,
                        start = center,
                        end = Offset(
                            center.x + (radius * 0.48f * cos(hourAngle)).toFloat(),
                            center.y + (radius * 0.48f * sin(hourAngle)).toFloat()
                        ),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = accentColor,
                        start = center,
                        end = Offset(
                            center.x + (radius * 0.72f * cos(minAngle)).toFloat(),
                            center.y + (radius * 0.72f * sin(minAngle)).toFloat()
                        ),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = accentColor, radius = 5f, center = center)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.FLIP -> {
                // Niagara FlipClockView (res/ME.xml): retro split-flap tiles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(hourStr to colors.onSurface, minStr to colors.secondary).forEach { (digits, tint) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface.copy(alpha = 0.92f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digits,
                                color = tint,
                                style = typography.displayLarge.copy(
                                    fontSize = 44.sp,
                                    lineHeight = 48.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(1.5.dp)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.OVERLAP -> {
                // Niagara OverlapClockView (res/aL.xml, overlap_clock_default_text_size = 96dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hourStr,
                        color = colors.onBackground.copy(alpha = 0.92f),
                        style = typography.displayLarge.copy(
                            fontSize = 84.sp,
                            lineHeight = 86.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            shadow = textShadow
                        )
                    )
                    Text(
                        text = minStr,
                        color = colors.secondary.copy(alpha = 0.85f),
                        style = typography.displayLarge.copy(
                            fontSize = 84.sp,
                            lineHeight = 86.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                            shadow = textShadow
                        ),
                        modifier = Modifier.offset(x = (-14).dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.DAY_OF_WEEK -> {
                // Niagara DayOfWeekStyle: bold weekday headline + compact date/time
                Text(
                    dayOfWeekStr,
                    color = colors.onBackground,
                    style = typography.headlineMedium.copy(
                        fontSize = 34.sp,
                        lineHeight = 40.sp,
                        shadow = textShadow
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    shortSubLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium.copy(shadow = textShadow)
                )
            }
            ClockStyle.MINIMALIST -> {
                // Niagara MinimalistTextClockView: oversized translucent backdrop time + crisp 14sp date
                Box(contentAlignment = Alignment.CenterStart) {
                    Text(
                        timeStr,
                        color = colors.onBackground.copy(alpha = 0.24f),
                        style = typography.displayLarge.copy(
                            fontSize = 80.sp,
                            lineHeight = 84.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    )
                    Text(
                        subLine,
                        color = colors.onBackground,
                        style = typography.bodyMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            shadow = textShadow
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
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
            ClockStyle.NONE -> Unit
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
                    style = typography.bodyMedium.copy(shadow = textShadow),
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
        color = colors.surface.copy(alpha = 0.92f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    packageName = media.packageName,
                    contentDescription = media.title,
                    size = 44.dp,
                    iconPackPackage = iconPackPackage,
                    monochrome = monochromeIcons
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    color = colors.onSurface,
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (media.artist.isNotEmpty()) {
                    Text(
                        text = media.artist,
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium,
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
private fun HomeStatusPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Boolean,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(50),
        color = colors.surface.copy(alpha = 0.9f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 10.dp,
                end = if (onDismiss != null) 4.dp else 12.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (accent) colors.secondary else colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = colors.onSurface,
                style = typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Ẩn",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun NiagaraAppRow(
    app: AppInfo,
    iconSize: Dp,
    showLabel: Boolean,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    iconShape: IconShapePreset = IconShapePreset.DEFAULT,
    subtitle: String? = null,
    notificationPreview: NotificationPreview? = null,
    textShadow: Shadow? = null,
    onNotificationClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeRight: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 48.dp.toPx() }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val animatedOffsetPx by animateFloatAsState(
        targetValue = dragOffsetPx,
        label = "app_row_swipe"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
            .pressScale(interactionSource)
            .defaultMinSize(minHeight = (iconSize + 10.dp).coerceAtLeast(52.dp))
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (onSwipeRight != null) {
                    Modifier.pointerInput(app.packageName, onSwipeRight) {
                        var triggered = false
                        detectHorizontalDragGestures(
                            onDragStart = {
                                triggered = false
                                dragOffsetPx = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount > 0f || dragOffsetPx > 0f) {
                                    change.consume()
                                    dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, swipeThresholdPx * 1.4f)
                                    if (!triggered && dragOffsetPx >= swipeThresholdPx) {
                                        triggered = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            },
                            onDragEnd = {
                                val shouldOpen = triggered || dragOffsetPx >= swipeThresholdPx
                                dragOffsetPx = 0f
                                if (shouldOpen) onSwipeRight()
                            },
                            onDragCancel = { dragOffsetPx = 0f }
                        )
                    }
                } else {
                    Modifier
                }
            )
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
            monochrome = monochromeIcons,
            iconShape = iconShape
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = colors.onBackground,
                    style = typography.titleLarge.copy(shadow = textShadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrEmpty() && notificationPreview == null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = colors.onBackground.copy(alpha = 0.68f),
                        style = typography.bodySmall.copy(shadow = textShadow),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Niagara inline notification preview (res/Ig.xml): clean frameless 1-2 lines below app label
                if (notificationPreview != null && notificationPreview.summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = if (onNotificationClick != null) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onNotificationClick)
                                .padding(vertical = 1.dp)
                        } else {
                            Modifier
                        }
                    ) {
                        val inlineText = if (notificationPreview.title.isNotEmpty() &&
                            notificationPreview.text.isNotEmpty() &&
                            !notificationPreview.title.equals(app.label, ignoreCase = true)
                        ) {
                            "${notificationPreview.title}: ${notificationPreview.text}"
                        } else {
                            notificationPreview.summary
                        }
                        Text(
                            text = inlineText,
                            color = colors.onBackground.copy(alpha = 0.72f),
                            style = typography.bodySmall.copy(shadow = textShadow),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
    iconPackPackage: String? = null,
    monochromeIcons: Boolean = false,
    iconShape: IconShapePreset = IconShapePreset.DEFAULT,
    textShadow: Shadow? = null,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val previewPkgs = remember(folder.packages) { folder.packages.take(4) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .defaultMinSize(minHeight = (iconSize + 10.dp).coerceAtLeast(52.dp))
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
                .background(colors.surfaceVariant.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            if (previewPkgs.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = folder.name,
                    tint = colors.secondary,
                    modifier = Modifier.size(iconSize * 0.56f)
                )
            } else {
                val miniSize = (iconSize * 0.36f).coerceAtLeast(12.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        previewPkgs.take(2).forEach { pkg ->
                            AppIcon(
                                packageName = pkg,
                                contentDescription = null,
                                size = miniSize,
                                iconPackPackage = iconPackPackage,
                                monochrome = monochromeIcons,
                                iconShape = iconShape
                            )
                        }
                    }
                    if (previewPkgs.size > 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            previewPkgs.drop(2).take(2).forEach { pkg ->
                                AppIcon(
                                    packageName = pkg,
                                    contentDescription = null,
                                    size = miniSize,
                                    iconPackPackage = iconPackPackage,
                                    monochrome = monochromeIcons,
                                    iconShape = iconShape
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    color = colors.onBackground,
                    style = typography.titleLarge.copy(shadow = textShadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.packages.size} ứng dụng",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall.copy(shadow = textShadow)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NiagaraWidgetStack(widgetIds: List<Int>) {
    val colors = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { widgetIds.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp, max = 160.dp)
        ) { page ->
            val widgetId = widgetIds[page]
            AndroidView(
                factory = { ctx ->
                    WidgetHostHelper.createWidgetView(ctx, widgetId)
                        ?: android.widget.FrameLayout(ctx)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp, max = 160.dp)
            )
        }
        if (widgetIds.size > 1) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(widgetIds.size) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(if (active) 16.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) colors.secondary else colors.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@android.annotation.SuppressLint("WrongConstant")
private fun expandNotificationShade(context: Context) {
    runCatching {
        val service = context.getSystemService("statusbar") ?: return
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val expand = statusBarManager.getMethod("expandNotificationsPanel")
        expand.invoke(service)
    }
}
