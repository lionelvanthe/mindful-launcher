package com.indie.mindfullauncher.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.indie.mindfullauncher.model.AccentPreset
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.FontPreset
import com.indie.mindfullauncher.model.IconShapePreset
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = colors.onBackground)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tất cả ứng dụng (${filtered.size})",
                    color = colors.onBackground,
                    style = typography.titleMedium
                )
                Text(
                    text = "Ghim vào '${editableProfile.displayName}' hoặc bật khoảng dừng 5s",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val isPinned = app.packageName in pinnedSet
                    val isFriction = app.packageName in state.frictionPackages
                    val rowInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .pressScale(rowInteraction)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = rowInteraction,
                                indication = LocalIndication.current,
                                onClick = { onAppClick(app) }
                            )
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            packageName = app.packageName,
                            contentDescription = app.label,
                            size = 38.dp,
                            iconPackPackage = state.selectedIconPack,
                            monochrome = state.effectiveMonochromeIcons,
                            iconShape = state.iconShapePreset
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = app.label,
                            color = colors.onBackground,
                            style = typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onTogglePin(editableProfile, app.packageName) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Ghim",
                                tint = if (isPinned) colors.secondary else colors.outline.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onToggleFriction(app.packageName) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.HourglassBottom,
                                contentDescription = "Khoảng dừng 5s",
                                tint = if (isFriction) colors.secondary else colors.outline.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
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
    onSetAccentPreset: (AccentPreset) -> Unit,
    onSetIconSizePreset: (IconSizePreset) -> Unit,
    onSetIconShapePreset: (IconShapePreset) -> Unit,
    onSetHideStatusBar: (Boolean) -> Unit,
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
    onToggleAutoContext: (Boolean) -> Unit = {},
    onSetAutoSleepOnCharge: (Boolean) -> Unit = {},
    onSetMonochromeInSleep: (Boolean) -> Unit = {},
    onOpenNotificationSettings: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onPickWidget: () -> Unit,
    onClearWidget: () -> Unit,
    onSetNiagaraButtonPackage: (String?) -> Unit,
    onRefreshPermissions: () -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Boolean,
    onUnhideApp: (String) -> Unit,
    onOpenDefaultLauncher: () -> Unit,
    onOpenProUpgrade: () -> Unit = {}
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
    ) {
        // Sticky Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = colors.onBackground)
            }
            Text(
                text = "Cài đặt Launcher",
                color = colors.onBackground,
                style = typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        HorizontalDivider(color = colors.surfaceVariant.copy(alpha = 0.6f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pro Upgrade Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenProUpgrade() },
                shape = RoundedCornerShape(20.dp),
                color = if (state.isProUnlocked) colors.secondary.copy(alpha = 0.14f) else colors.surface,
                border = BorderStroke(
                    1.dp,
                    if (state.isProUnlocked) colors.secondary.copy(alpha = 0.45f) else colors.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.secondary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isProUnlocked) "Stillness Pro đã mở khóa" else "Nâng cấp Stillness Pro (${state.proPrice})",
                            color = colors.onSurface,
                            style = typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (state.isProUnlocked) {
                                "Đã mở khóa toàn bộ ngữ cảnh, Auto-Trigger, Icon Pack & Niagara Dots."
                            } else {
                                "Mua đứt 1 lần qua Google Play · Không giới hạn ngữ cảnh, Auto-Trigger, Icon Pack & Dots."
                            },
                            color = colors.onSurfaceVariant,
                            style = typography.bodySmall
                        )
                    }
                }
            }

            // Section 1: Giao diện & Đồng hồ
            SettingsSectionCard(title = "GIAO DIỆN NIAGARA") {
                SettingsChipGroupLabel("Kiểu đồng hồ")
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
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = colors.onSurfaceVariant,
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SettingsChipGroupLabel("Màu nhấn")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccentPreset.entries.forEach { accent ->
                        val swatchColor = Color(accent.colorHex)
                        FilterChip(
                            selected = state.accentPreset == accent,
                            onClick = { onSetAccentPreset(accent) },
                            shape = RoundedCornerShape(50),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.accentPreset == accent) colors.onSecondary else swatchColor
                                        )
                                )
                            },
                            label = { Text(accent.displayName, style = typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = colors.onSurfaceVariant,
                                selectedContainerColor = swatchColor,
                                selectedLabelColor = colors.onSecondary
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SettingsChipGroupLabel("Kích thước icon")
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
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = colors.onSurfaceVariant,
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SettingsChipGroupLabel("Kiểu icon (Hình dạng & Niagara Dots)")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconShapePreset.entries.forEach { shapePreset ->
                        FilterChip(
                            selected = state.iconShapePreset == shapePreset,
                            onClick = { onSetIconShapePreset(shapePreset) },
                            shape = RoundedCornerShape(50),
                            label = { Text(shapePreset.displayName, style = typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = colors.onSurfaceVariant,
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SettingsChipGroupLabel("Font chữ")
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
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = colors.onSurfaceVariant,
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                if (state.availableIconPacks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsChipGroupLabel("Icon Pack đã cài (${state.availableIconPacks.size})")
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
                                containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
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
                                    containerColor = colors.surfaceVariant.copy(alpha = 0.6f),
                                    labelColor = colors.onSurfaceVariant,
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = colors.onPrimary
                                ),
                                border = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.surfaceVariant.copy(alpha = 0.6f))
                SettingsSwitchRow(
                    title = "Bố cục 2 cột trên màn hình lớn",
                    subtitle = "Chia đôi màn hình trên Tablet hoặc màn hình gập (>= 600dp)",
                    checked = state.twoColumnLargeScreen,
                    onCheckedChange = onSetTwoColumnLargeScreen
                )
                SettingsSwitchRow(
                    title = "Ẩn thanh trạng thái (Status Bar)",
                    subtitle = "Chế độ toàn màn hình tối giản giống Niagara Launcher",
                    checked = state.hideStatusBar,
                    onCheckedChange = onSetHideStatusBar
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
            }

            // Section 2: Ngữ cảnh & Thói quen số
            SettingsSectionCard(title = "SMART SORT & NGỮ CẢNH") {
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
                SettingsSwitchRow(
                    title = "Tự chuyển ngữ cảnh theo giờ",
                    subtitle = "Tự động đổi màn hình Làm việc / Buổi tối / Đi ngủ",
                    checked = state.autoContextEnabled,
                    onCheckedChange = onToggleAutoContext
                )
                SettingsSwitchRow(
                    title = "Tự bật Đi ngủ khi sạc tối / DND",
                    subtitle = "Chuyển sang chế độ Đi ngủ khi cắm sạc sau 21:00 hoặc bật Không làm phiền",
                    checked = state.autoSleepOnCharge,
                    onCheckedChange = onSetAutoSleepOnCharge
                )
                SettingsSwitchRow(
                    title = "Icon đơn sắc ở chế độ Đi ngủ",
                    subtitle = "Giảm kích thích thị giác trước khi ngủ",
                    checked = state.monochromeInSleep,
                    onCheckedChange = onSetMonochromeInSleep
                )
                if (!state.hasUsagePermission) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsPermissionRow(
                        title = "Quyền Usage Access",
                        subtitle = "Hiển thị phút sử dụng trong khoảng dừng 5s & Smart Sort",
                        isGranted = false,
                        grantLabel = "Cấp quyền",
                        onGrant = onOpenUsageSettings
                    )
                }
            }

            // Section 3: At-a-Glance, Nhạc & Thông báo
            SettingsSectionCard(title = "AT-A-GLANCE, NHẠC & THÔNG BÁO") {
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
                    SettingsPermissionRow(
                        title = "Quyền đọc Danh bạ",
                        subtitle = "Cho phép gọi nhanh từ thanh tìm kiếm",
                        isGranted = false,
                        grantLabel = "Cấp quyền",
                        onGrant = { contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS) }
                    )
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
                    SettingsPermissionRow(
                        title = "Quyền đọc Lịch",
                        subtitle = "Hiện sự kiện tiếp theo dưới đồng hồ",
                        isGranted = false,
                        grantLabel = "Cấp quyền",
                        onGrant = { calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR) }
                    )
                }
                SettingsSwitchRow(
                    title = "Xem trước thông báo trên Yêu thích",
                    subtitle = if (state.hasNotificationPermission) "Đã bật quyền Thông báo" else "Chưa bật quyền đọc Thông báo",
                    checked = state.showNotificationPreviews,
                    onCheckedChange = onSetShowNotificationPreviews
                )
                if (!state.hasNotificationPermission) {
                    SettingsPermissionRow(
                        title = "Quyền đọc Thông báo",
                        subtitle = "Hiển thị tin nhắn gọn ngay dưới app yêu thích",
                        isGranted = false,
                        grantLabel = "Bật quyền",
                        onGrant = onOpenNotificationSettings
                    )
                }
            }

            // Section 4: Widget, Nút Niagara & Sao lưu
            SettingsSectionCard(title = "TIỆN ÍCH & SAO LƯU") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickWidget,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (state.pinnedWidgetIds.isNotEmpty()) {
                                "Thêm Widget (${state.pinnedWidgetIds.size})"
                            } else {
                                "Gắn Widget"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (state.pinnedWidgetIds.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearWidget,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Gỡ hết")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFabDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Nút Niagara", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { showTipsDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Hướng dẫn & Mẹo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("stillness_niagara_backup.json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Sao lưu JSON", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Khôi phục", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Section 5: Ứng dụng đã ẩn
            SettingsSectionCard(title = "ỨNG DỤNG ĐÃ ẨN (${hiddenApps.size})") {
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
                            Text(
                                text = app.label,
                                color = colors.onBackground,
                                style = typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            TextButton(onClick = { onUnhideApp(app.packageName) }) {
                                Text("Hiện lại", color = colors.secondary)
                            }
                        }
                    }
                }
            }

            // Section 6: Zero-Telemetry & Default Launcher
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF81C784).copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Zero-Telemetry", color = Color(0xFF81C784), style = typography.labelLarge)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            "Ứng dụng không xin quyền Internet. Mọi ghi chú và cấu hình chỉ nằm trên điện thoại của bạn.",
                            color = colors.onSurfaceVariant,
                            style = typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = onOpenDefaultLauncher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onBackground
                )
            ) {
                Text("Mở cài đặt Launcher mặc định", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
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
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = colors.secondary,
                style = typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsChipGroupLabel(text: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Text(text, color = colors.onBackground, style = typography.bodyLarge)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun SettingsPermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    grantLabel: String,
    onGrant: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isGranted, onClick = onGrant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colors.onSurface, style = typography.labelLarge)
                Text(subtitle, color = colors.onSurfaceVariant, style = typography.bodySmall)
            }
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Đã cấp",
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.secondary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = grantLabel,
                        color = colors.secondary,
                        style = typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
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
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(title, color = colors.onBackground, style = typography.bodyLarge)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = colors.onSurfaceVariant, style = typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
