package com.indie.mindfullauncher.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.FontPreset
import com.indie.mindfullauncher.model.IconSizePreset
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onTogglePin: (ContextProfile, String) -> Unit,
    onToggleFriction: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, state.installedApps) {
        if (search.isBlank()) state.installedApps
        else state.installedApps.filter { it.label.contains(search.trim(), ignoreCase = true) }
    }
    val editableProfile = if (state.activeProfile == ContextProfile.ALL) ContextProfile.WORK else state.activeProfile
    val pinnedSet = state.pinnedByProfile[editableProfile].orEmpty()

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val letterIndexMap = remember(filtered) {
        val map = linkedMapOf<Char, Int>()
        filtered.forEachIndexed { index, app ->
            val first = app.label.trim().firstOrNull()?.uppercaseChar()
            val key = if (first != null && first in 'A'..'Z') first else '#'
            map.putIfAbsent(key, index)
        }
        map
    }
    val scrubberLetters = remember(letterIndexMap) { letterIndexMap.keys.toList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = colors.onBackground)
            }
            Text(
                text = "Tất cả ứng dụng (${filtered.size})",
                color = colors.onBackground,
                style = typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
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
                    "Lọc nhanh tên ứng dụng...",
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium
                )
            },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm", tint = colors.onSurfaceVariant)
                    }
                }
            },
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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ghim vào ngữ cảnh '${editableProfile.displayName}' hoặc bật khoảng dừng 5s",
            color = colors.onSurfaceVariant,
            style = typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = if (scrubberLetters.isNotEmpty()) 32.dp else 0.dp)
                    .verticalFadingEdge(topFadeHeight = 16.dp, bottomFadeHeight = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val isPinned = app.packageName in pinnedSet
                    val isFriction = app.packageName in state.frictionPackages
                    val rowInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(rowInteraction)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = rowInteraction,
                                indication = LocalIndication.current,
                                onClick = { onAppClick(app) }
                            )
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            color = colors.onBackground,
                            style = typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onTogglePin(editableProfile, app.packageName) }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Ghim",
                                tint = if (isPinned) colors.primary else colors.outline
                            )
                        }
                        IconButton(onClick = { onToggleFriction(app.packageName) }) {
                            Icon(
                                Icons.Default.HourglassBottom,
                                contentDescription = "Khoảng dừng 5s",
                                tint = if (isFriction) colors.secondary else colors.outline
                            )
                        }
                    }
                }
            }

            AlphabetScrubber(
                letters = scrubberLetters,
                onLetterSelected = { letter ->
                    letterIndexMap[letter]?.let { targetIndex ->
                        scope.launch { listState.scrollToItem(targetIndex) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetIconSizePreset: (IconSizePreset) -> Unit,
    onSetFontPreset: (FontPreset) -> Unit,
    onSetHideFavoriteLabels: (Boolean) -> Unit,
    onSetShowBatteryOnHome: (Boolean) -> Unit,
    onSetShowAlphabetWave: (Boolean) -> Unit,
    onSetShowWallpaperBackground: (Boolean) -> Unit,
    onSetMonochromeIcons: (Boolean) -> Unit,
    onSetTwoColumnLargeScreen: (Boolean) -> Unit,
    onSetSelectedIconPack: (String?) -> Unit,
    onSetShowCalendarEvents: (Boolean) -> Unit,
    onSetShowContactSearch: (Boolean) -> Unit,
    onSetShowMusicOnHeadphones: (Boolean) -> Unit,
    onSetShowNotificationPreviews: (Boolean) -> Unit,
    onSetShowMediaPlayer: (Boolean) -> Unit,
    onSetSmartSortEnabled: (Boolean) -> Unit,
    onSetShowMostUsedApps: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onPickWidget: () -> Unit,
    onClearWidget: () -> Unit,
    onSetNiagaraButtonPackage: (String?) -> Unit,
    onRefreshPermissions: () -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Boolean,
    onUnhideApp: (String) -> Unit,
    onOpenDefaultLauncher: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var showFabDialog by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    val hiddenApps = remember(state.installedApps, state.hiddenPackages) {
        state.installedApps.filter { it.packageName in state.hiddenPackages }
    }

    val calendarPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRefreshPermissions()
        if (!granted) {
            Toast.makeText(context, "Cần cấp quyền Lịch để hiện sự kiện", Toast.LENGTH_SHORT).show()
        }
    }

    val contactsPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRefreshPermissions()
        if (!granted) {
            Toast.makeText(context, "Cần cấp quyền Danh bạ để tìm liên hệ", Toast.LENGTH_SHORT).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(onExportBackup().toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                Toast.makeText(context, "Đã sao lưu cấu hình", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Sao lưu thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().readText()
                }?.let { onImportBackup(it) } ?: false
            }.getOrDefault(false)
            Toast.makeText(
                context,
                if (ok) "Đã khôi phục cấu hình" else "File sao lưu không hợp lệ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = colors.onBackground)
            }
            Text("Cài đặt Launcher", color = colors.onBackground, style = typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("GIAO DIỆN NIAGARA", color = colors.secondary, style = typography.labelMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Text("Kiểu đồng hồ", color = colors.onBackground, style = typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockStyle.entries.forEach { style ->
                FilterChip(
                    selected = state.clockStyle == style,
                    onClick = { onSetClockStyle(style) },
                    shape = RoundedCornerShape(50),
                    label = { Text(style.displayName, style = typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.onSurfaceVariant,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Kích thước icon", color = colors.onBackground, style = typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconSizePreset.entries.forEach { preset ->
                FilterChip(
                    selected = state.iconSizePreset == preset,
                    onClick = { onSetIconSizePreset(preset) },
                    shape = RoundedCornerShape(50),
                    label = { Text("${preset.displayName} (${preset.dpValue}dp)", style = typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.onSurfaceVariant,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Font chữ", color = colors.onBackground, style = typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FontPreset.entries.forEach { preset ->
                FilterChip(
                    selected = state.fontPreset == preset,
                    onClick = { onSetFontPreset(preset) },
                    shape = RoundedCornerShape(50),
                    label = { Text(preset.displayName, style = typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.onSurfaceVariant,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SettingsSwitchRow(
            title = "Bố cục 2 cột trên màn hình lớn",
            subtitle = "Chia đôi màn hình trên Tablet hoặc màn hình gập (>= 600dp)",
            checked = state.twoColumnLargeScreen,
            onCheckedChange = onSetTwoColumnLargeScreen
        )
        SettingsSwitchRow(
            title = "Ẩn tên app ở Yêu thích",
            subtitle = "Chỉ hiển thị icon gọn gàng trên màn hình chính",
            checked = state.hideFavoriteLabels,
            onCheckedChange = onSetHideFavoriteLabels
        )
        SettingsSwitchRow(
            title = "Hiển thị % Pin",
            subtitle = "Hiện phần trăm pin cạnh ngày tháng",
            checked = state.showBatteryOnHome,
            onCheckedChange = onSetShowBatteryOnHome
        )
        SettingsSwitchRow(
            title = "Hiện thanh cuộn A–Z",
            subtitle = "Thanh chữ cái nhanh ở cạnh phải màn hình",
            checked = state.showAlphabetWave,
            onCheckedChange = onSetShowAlphabetWave
        )
        SettingsSwitchRow(
            title = "Nền hình nền trong suốt",
            subtitle = "Hiển thị hình nền hệ thống phía sau màn hình chính",
            checked = state.showWallpaperBackground,
            onCheckedChange = onSetShowWallpaperBackground
        )
        SettingsSwitchRow(
            title = "Icon đơn sắc tối giản",
            subtitle = "Chuyển toàn bộ icon ứng dụng sang tông xám dịu mắt",
            checked = state.monochromeIcons,
            onCheckedChange = onSetMonochromeIcons
        )

        if (state.availableIconPacks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Icon Pack đã cài (${state.availableIconPacks.size})", color = colors.onBackground, style = typography.bodyLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedIconPack == null,
                    onClick = { onSetSelectedIconPack(null) },
                    shape = RoundedCornerShape(50),
                    label = { Text("Mặc định", style = typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.onSurfaceVariant,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary
                    ),
                    border = null
                )
                state.availableIconPacks.forEach { pack ->
                    FilterChip(
                        selected = state.selectedIconPack == pack.packageName,
                        onClick = { onSetSelectedIconPack(pack.packageName) },
                        shape = RoundedCornerShape(50),
                        label = { Text(pack.label, style = typography.labelLarge) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            labelColor = colors.onSurfaceVariant,
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.onPrimary
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("SMART SORT & WIDGET", color = colors.secondary, style = typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SettingsSwitchRow(
            title = "Sắp xếp thông minh theo tần suất",
            subtitle = if (state.hasUsagePermission) "Ưu tiên app dùng nhiều khi duyệt & tìm kiếm" else "Cần quyền Usage Access",
            checked = state.smartSortEnabled,
            onCheckedChange = onSetSmartSortEnabled
        )
        SettingsSwitchRow(
            title = "Gợi ý app hay dùng trên Home",
            subtitle = "Hiện tối đa 3 app mở nhiều nhất gần đây",
            checked = state.showMostUsedApps,
            onCheckedChange = onSetShowMostUsedApps
        )
        if (!state.hasUsagePermission) {
            Button(
                onClick = onOpenUsageSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Cấp quyền Usage Access")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onPickWidget,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text(if (state.pinnedWidgetId > 0) "Đổi Widget" else "Gắn Widget lên Home")
            }
            if (state.pinnedWidgetId > 0) {
                Button(
                    onClick = onClearWidget,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onBackground
                    )
                ) {
                    Text("Gỡ Widget")
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { showFabDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Tùy chỉnh nút Niagara")
            }
            Button(
                onClick = { showTipsDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Hướng dẫn & Mẹo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("AT-A-GLANCE, NHẠC & THÔNG BÁO", color = colors.secondary, style = typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SettingsSwitchRow(
            title = "Trình phát nhạc At-a-Glance",
            subtitle = "Điều khiển nhạc đang phát ngay trên màn hình chính",
            checked = state.showMediaPlayer,
            onCheckedChange = onSetShowMediaPlayer
        )
        SettingsSwitchRow(
            title = "Gợi ý app nhạc khi kết nối tai nghe",
            subtitle = "Tự động hiện app nghe nhạc khi cắm tai nghe / Bluetooth",
            checked = state.showMusicOnHeadphones,
            onCheckedChange = onSetShowMusicOnHeadphones
        )
        SettingsSwitchRow(
            title = "Tìm kiếm Danh bạ",
            subtitle = if (state.hasContactsPermission) "Đã cấp quyền Danh bạ" else "Chưa cấp quyền đọc Danh bạ",
            checked = state.showContactSearch,
            onCheckedChange = { enabled ->
                onSetShowContactSearch(enabled)
                if (enabled && !state.hasContactsPermission) {
                    contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        )
        if (!state.hasContactsPermission) {
            Button(
                onClick = { contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Cấp quyền đọc Danh bạ")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        SettingsSwitchRow(
            title = "Hiển thị sự kiện Lịch sắp tới",
            subtitle = if (state.hasCalendarPermission) "Đã cấp quyền Lịch" else "Chưa cấp quyền đọc Lịch",
            checked = state.showCalendarEvents,
            onCheckedChange = { enabled ->
                onSetShowCalendarEvents(enabled)
                if (enabled && !state.hasCalendarPermission) {
                    calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR)
                }
            }
        )
        if (!state.hasCalendarPermission) {
            Button(
                onClick = { calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Cấp quyền đọc Lịch")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SettingsSwitchRow(
            title = "Xem trước thông báo trên Yêu thích",
            subtitle = if (state.hasNotificationPermission) "Đã bật quyền Thông báo" else "Chưa bật quyền đọc Thông báo",
            checked = state.showNotificationPreviews,
            onCheckedChange = onSetShowNotificationPreviews
        )
        if (!state.hasNotificationPermission) {
            Button(
                onClick = onOpenNotificationSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Bật quyền đọc Thông báo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("SAO LƯU & KHÔI PHỤC", color = colors.secondary, style = typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { exportLauncher.launch("stillness_niagara_backup.json") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Sao lưu JSON")
            }
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Khôi phục")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("ỨNG DỤNG ĐÃ ẨN (${hiddenApps.size})", color = colors.secondary, style = typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        if (hiddenApps.isEmpty()) {
            Text(
                text = "Chưa có ứng dụng nào bị ẩn. Nhấn giữ ứng dụng bất kỳ để ẩn khỏi danh sách A–Z.",
                color = colors.onSurfaceVariant,
                style = typography.bodySmall
            )
        } else {
            hiddenApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(app.label, color = colors.onBackground, style = typography.bodyLarge)
                    Button(
                        onClick = { onUnhideApp(app.packageName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surfaceVariant,
                            contentColor = colors.onBackground
                        )
                    ) {
                        Text("Hiện lại")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Zero-Telemetry", color = Color(0xFF81C784), style = typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Ứng dụng không xin quyền Internet. Mọi ghi chú và cấu hình chỉ nằm trên điện thoại của bạn.",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Đặt làm màn hình chính mặc định", color = colors.onBackground, style = typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onOpenDefaultLauncher,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surfaceVariant,
                contentColor = colors.onBackground
            )
        ) {
            Text("Mở cài đặt Launcher mặc định")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showFabDialog) {
        NiagaraFabActionDialog(
            installedApps = state.installedApps,
            selectedPackage = state.niagaraButtonPackage,
            onDismiss = { showFabDialog = false },
            onSelectPackage = { pkg ->
                onSetNiagaraButtonPackage(pkg)
                showFabDialog = false
            }
        )
    }

    if (showTipsDialog) {
        QuickTipsDialog(onDismiss = { showTipsDialog = false })
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.onBackground, style = typography.bodyLarge)
            Text(subtitle, color = colors.onSurfaceVariant, style = typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
