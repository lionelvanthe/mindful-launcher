package com.indie.mindfullauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indie.mindfullauncher.domain.CommandResult
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ContextProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    state: LauncherUiState,
    onSelectProfile: (ContextProfile) -> Unit,
    onCommandQueryChanged: (String) -> Unit,
    onSubmitCommand: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onToggleNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDefaultLauncher: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onDismissSetupBanner: () -> Unit
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                now = System.currentTimeMillis()
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
        onDispose { context.unregisterReceiver(receiver) }
    }

    val timeStr = remember(now) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
    }
    val dateStr = remember(now) {
        SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(Date(now))
    }
    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Upper visual zone: Clock, Date, Optional First-Run Onboarding, Quick Notes
        Text(
            text = timeStr,
            color = colors.onBackground,
            style = typography.displayLarge.copy(fontWeight = FontWeight.Light)
        )
        Text(
            text = dateStr.replaceFirstChar { it.uppercase() },
            color = colors.onSurfaceVariant,
            style = typography.bodyMedium
        )

        val showOnboarding = !state.isSetupBannerDismissed &&
            (!state.isDefaultLauncher || !state.hasUsagePermission)

        if (showOnboarding && state.commandQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            FirstRunSetupCard(
                isDefaultLauncher = state.isDefaultLauncher,
                hasUsagePermission = state.hasUsagePermission,
                onOpenDefaultLauncher = onOpenDefaultLauncher,
                onOpenUsageSettings = onOpenUsageSettings,
                onDismiss = onDismissSetupBanner
            )
        }

        if (state.notes.isNotEmpty() && state.commandQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
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
                        .defaultMinSize(minHeight = 48.dp),
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
                        modifier = Modifier.size(48.dp)
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

        // Push primary interactive controls into the lower thumb zone
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            when (val res = state.commandResult) {
                is CommandResult.MathResult -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clipboard.setText(AnnotatedString(res.formattedValue)) },
                        shape = RoundedCornerShape(14.dp),
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
                            .clickable { onSubmitCommand() },
                        shape = RoundedCornerShape(14.dp),
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
                    if (res.apps.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
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
                                    text = "Thử từ khóa khác hoặc mở danh sách tất cả ứng dụng.",
                                    color = colors.onSurfaceVariant,
                                    style = typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onCommandQueryChanged("") }) {
                                        Text("Xóa tìm kiếm")
                                    }
                                    TextButton(onClick = onOpenDrawer) {
                                        Text("Mở tất cả app")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            reverseLayout = true
                        ) {
                            items(res.apps, key = { it.packageName }) { app ->
                                MinimalAppRow(
                                    app = app,
                                    isFrictioned = app.packageName in state.frictionPackages,
                                    onClick = { onAppClick(app) }
                                )
                            }
                        }
                    }
                }
                CommandResult.Empty -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NGỮ CẢNH: ${state.activeProfile.displayName.uppercase()}",
                            color = colors.onSurfaceVariant,
                            style = typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(state.visibleContextApps, key = { it.packageName }) { app ->
                                MinimalAppRow(
                                    app = app,
                                    isFrictioned = app.packageName in state.frictionPackages,
                                    onClick = { onAppClick(app) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thumb-zone: Context Profile Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContextProfile.entries.forEach { profile ->
                val selected = state.activeProfile == profile
                FilterChip(
                    selected = selected,
                    onClick = { onSelectProfile(profile) },
                    label = {
                        Text(
                            text = profile.displayName,
                            style = typography.labelLarge
                        )
                    },
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

        Spacer(modifier = Modifier.height(8.dp))

        // Thumb-zone: Command Bar
        OutlinedTextField(
            value = state.commandQuery,
            onValueChange = onCommandQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Tìm app, tính 250000/3, hoặc ! ghi chú...",
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium
                )
            },
            trailingIcon = {
                if (state.commandQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onCommandQueryChanged("") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm", tint = colors.onSurfaceVariant)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmitCommand() }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                focusedBorderColor = colors.outline,
                unfocusedBorderColor = colors.surfaceVariant,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Bar (48dp+ touch targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenDrawer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Apps,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tất cả ứng dụng",
                    color = colors.onSurfaceVariant,
                    style = typography.labelLarge
                )
            }

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
        }
    }
}

@Composable
private fun FirstRunSetupCard(
    isDefaultLauncher: Boolean,
    hasUsagePermission: Boolean,
    onOpenDefaultLauncher: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoàn tất thiết lập nhanh",
                    color = colors.onSurface,
                    style = typography.titleSmall
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Ẩn hướng dẫn",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isDefaultLauncher) {
                    OutlinedButton(onClick = onOpenDefaultLauncher) {
                        Text("Đặt làm Launcher", style = typography.labelMedium)
                    }
                }
                if (!hasUsagePermission) {
                    OutlinedButton(onClick = onOpenUsageSettings) {
                        Text("Bật thống kê 5s", style = typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalAppRow(
    app: AppInfo,
    isFrictioned: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.label,
            color = colors.onBackground,
            style = typography.titleLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isFrictioned) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Có khoảng dừng 5 giây trước khi mở"
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "5s",
                    color = colors.secondary,
                    style = typography.labelSmall
                )
            }
        }
    }
}
