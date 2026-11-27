package com.indie.mindfullauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.BuiltInContextConfig
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.CustomContextProfile
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.NotificationPreview
import com.indie.mindfullauncher.model.QuickNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NiagaraFolderPopupDialog(
    folder: LauncherFolder,
    apps: List<AppInfo>,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onRemoveApp: (String) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDeleteFolder: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var isRenaming by remember(folder.id) { mutableStateOf(false) }
    var renameText by remember(folder.id, folder.name) { mutableStateOf(folder.name) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = colors.secondary
                        )
                        Text(
                            text = folder.name,
                            color = colors.onSurface,
                            style = typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row {
                        IconButton(onClick = { isRenaming = !isRenaming }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Đổi tên thư mục",
                                tint = colors.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDeleteFolder) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Xóa thư mục",
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                }
                if (isRenaming) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Tên thư mục...") },
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = {
                                if (renameText.isNotBlank()) {
                                    onRenameFolder(renameText.trim())
                                    isRenaming = false
                                }
                            }
                        ) {
                            Text("Lưu")
                        }
                    }
                }
                HorizontalDivider(color = colors.surfaceVariant)
                if (apps.isEmpty()) {
                    Text(
                        text = "Thư mục trống. Nhấn giữ ứng dụng bất kỳ để thêm vào đây.",
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    apps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppClick(app) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                contentDescription = app.label,
                                size = 36.dp,
                                iconPackPackage = iconPackPackage,
                                monochrome = monochromeIcons
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = app.label,
                                color = colors.onSurface,
                                style = typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(onClick = { onRemoveApp(app.packageName) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Bỏ khỏi thư mục",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddToFolderDialog(
    app: AppInfo,
    folders: List<LauncherFolder>,
    onDismiss: () -> Unit,
    onSelectFolder: (String) -> Unit,
    onCreateFolder: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var newFolderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Thêm '${app.label}' vào thư mục",
                    color = colors.onSurface,
                    style = typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                folders.forEach { folder ->
                    val inFolder = app.packageName in folder.packages
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectFolder(folder.id) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = colors.secondary)
                            Text(folder.name, color = colors.onSurface, style = typography.bodyLarge)
                        }
                        if (inFolder) {
                            Text("Đã thêm", color = colors.secondary, style = typography.labelSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Tên thư mục mới...", style = typography.bodyMedium) },
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onCreateFolder(newFolderName.ifBlank { "Thư mục mới" }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tạo thư mục & Thêm")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NiagaraAppOptionsDialog(
    app: AppInfo,
    shortcuts: List<AppShortcutItem>,
    popupWidgetId: Int,
    isFavorite: Boolean,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    isHidden: Boolean,
    isSummarized: Boolean,
    isFrictionEnabled: Boolean = false,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveFavoriteUp: () -> Unit = {},
    onMoveFavoriteDown: () -> Unit = {},
    onAddToFolder: () -> Unit,
    onRenameApp: () -> Unit,
    onPickPopupWidget: () -> Unit,
    onClearPopupWidget: () -> Unit,
    onToggleSummary: () -> Unit,
    onToggleFriction: () -> Unit = {},
    onToggleHidden: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    packageName = app.packageName,
                    contentDescription = app.label,
                    size = 40.dp,
                    iconPackPackage = iconPackPackage,
                    monochrome = monochromeIcons
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = app.label,
                    color = colors.onSurface,
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (popupWidgetId > 0) {
                HorizontalDivider(color = colors.surfaceVariant)
                AndroidView(
                    factory = { ctx ->
                        WidgetHostHelper.createWidgetView(ctx, popupWidgetId)
                            ?: android.widget.FrameLayout(ctx)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp, max = 140.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (shortcuts.isNotEmpty()) {
                HorizontalDivider(color = colors.surfaceVariant)
                shortcuts.forEach { sc ->
                    DialogOptionRow(
                        icon = Icons.Default.Bolt,
                        label = sc.label,
                        onClick = { onLaunchShortcut(sc) }
                    )
                }
            }

            HorizontalDivider(color = colors.surfaceVariant)

            DialogOptionRow(
                icon = Icons.Default.PushPin,
                label = if (isFavorite) "Bỏ khỏi Yêu thích" else "Ghim vào Yêu thích",
                onClick = onToggleFavorite
            )
            if (isFavorite && canMoveUp) {
                DialogOptionRow(
                    icon = Icons.Default.ArrowUpward,
                    label = "Di chuyển lên trong Yêu thích",
                    onClick = onMoveFavoriteUp
                )
            }
            if (isFavorite && canMoveDown) {
                DialogOptionRow(
                    icon = Icons.Default.ArrowDownward,
                    label = "Di chuyển xuống trong Yêu thích",
                    onClick = onMoveFavoriteDown
                )
            }
            DialogOptionRow(
                icon = Icons.Default.CreateNewFolder,
                label = "Thêm vào thư mục",
                onClick = onAddToFolder
            )
            DialogOptionRow(
                icon = Icons.Default.Edit,
                label = "Đổi tên ứng dụng",
                onClick = onRenameApp
            )
            DialogOptionRow(
                icon = Icons.Default.Widgets,
                label = if (popupWidgetId > 0) "Gỡ Widget khỏi Pop-up" else "Gắn Widget vào Pop-up",
                onClick = if (popupWidgetId > 0) onClearPopupWidget else onPickPopupWidget
            )
            DialogOptionRow(
                icon = Icons.Default.NotificationsOff,
                label = if (isSummarized) "Bỏ khỏi Tóm tắt thông báo" else "Gom vào Tóm tắt thông báo",
                onClick = onToggleSummary
            )
            DialogOptionRow(
                icon = Icons.Default.HourglassBottom,
                label = if (isFrictionEnabled) "Tắt khoảng dừng 5 giây" else "Bật khoảng dừng 5 giây (Chống nghiện)",
                onClick = onToggleFriction
            )
            DialogOptionRow(
                icon = Icons.Default.VisibilityOff,
                label = if (isHidden) "Hiện lại trong danh sách" else "Ẩn khỏi danh sách A–Z",
                onClick = onToggleHidden
            )
            DialogOptionRow(
                icon = Icons.Default.Info,
                label = "Thông tin ứng dụng",
                onClick = onOpenAppInfo
            )
            DialogOptionRow(
                icon = Icons.Default.DeleteOutline,
                label = "Gỡ cài đặt",
                onClick = onUninstall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NiagaraAppPopupSheet(
    app: AppInfo,
    shortcuts: List<AppShortcutItem>,
    popupWidgetId: Int,
    notificationPreview: NotificationPreview?,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onOpenOptions: () -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onPickPopupWidget: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onSendReply: (NotificationPreview, String) -> Boolean
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var replyText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppIcon(
                        packageName = app.packageName,
                        contentDescription = app.label,
                        size = 38.dp,
                        iconPackPackage = iconPackPackage,
                        monochrome = monochromeIcons
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = app.label,
                        color = colors.onSurface,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onOpenOptions) {
                        Text("Tùy chỉnh")
                    }
                    Button(onClick = onOpenApp) {
                        Text("Mở")
                    }
                }
            }

            if (notificationPreview != null) {
                HorizontalDivider(color = colors.surfaceVariant)
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notificationPreview.title.ifEmpty { app.label },
                            color = colors.secondary,
                            style = typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onDismissNotification(notificationPreview.key) }) {
                            Text("Xóa thông báo")
                        }
                    }
                    if (notificationPreview.text.isNotEmpty()) {
                        Text(
                            text = notificationPreview.text,
                            color = colors.onSurface,
                            style = typography.bodyMedium
                        )
                    }
                    if (notificationPreview.replyAction != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Trả lời nhanh...") },
                                shape = RoundedCornerShape(14.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (replyText.isNotBlank() && onSendReply(notificationPreview, replyText)) {
                                        onDismiss()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gửi",
                                    tint = colors.secondary
                                )
                            }
                        }
                    }
                }
            }

            if (popupWidgetId > 0) {
                HorizontalDivider(color = colors.surfaceVariant)
                AndroidView(
                    factory = { ctx ->
                        WidgetHostHelper.createWidgetView(ctx, popupWidgetId)
                            ?: android.widget.FrameLayout(ctx)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp, max = 160.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            if (shortcuts.isNotEmpty()) {
                HorizontalDivider(color = colors.surfaceVariant)
                shortcuts.forEach { sc ->
                    DialogOptionRow(
                        icon = Icons.Default.Bolt,
                        label = sc.label,
                        onClick = { onLaunchShortcut(sc) }
                    )
                }
            }

            if (notificationPreview == null && popupWidgetId <= 0 && shortcuts.isEmpty()) {
                HorizontalDivider(color = colors.surfaceVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Chưa có thông báo, phím tắt hay widget gắn kèm cho ${app.label}.",
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium
                    )
                    OutlinedButton(onClick = onPickPopupWidget) {
                        Text("Gắn Widget vào Pop-up")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPopupDialog(
    app: AppInfo,
    preview: NotificationPreview,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onDismissNotification: () -> Unit,
    onSendReply: (String) -> Boolean
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var replyText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        packageName = app.packageName,
                        contentDescription = app.label,
                        size = 36.dp,
                        iconPackPackage = iconPackPackage,
                        monochrome = monochromeIcons
                    )
                    Text(
                        text = app.label,
                        color = colors.onSurface,
                        style = typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (preview.title.isNotEmpty()) {
                    Text(preview.title, color = colors.onSurface, style = typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (preview.text.isNotEmpty()) {
                    Text(preview.text, color = colors.onSurfaceVariant, style = typography.bodyMedium)
                }

                if (preview.replyAction != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Trả lời nhanh...", style = typography.bodySmall) },
                            shape = RoundedCornerShape(50)
                        )
                        IconButton(
                            onClick = {
                                if (onSendReply(replyText)) {
                                    replyText = ""
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi trả lời",
                                tint = colors.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissNotification,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Xóa thông báo")
                    }
                    Button(
                        onClick = onOpenApp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mở app")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSummaryDialog(
    items: List<Pair<AppInfo, NotificationPreview>>,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onOpenApp: (AppInfo) -> Unit,
    onDismissAll: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tóm tắt thông báo", color = colors.onSurface, style = typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.second.key }) { (app, preview) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenApp(app) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                contentDescription = app.label,
                                size = 32.dp,
                                iconPackPackage = iconPackPackage,
                                monochrome = monochromeIcons
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.label, color = colors.onSurface, style = typography.labelLarge)
                                Text(
                                    preview.summary,
                                    color = colors.onSurfaceVariant,
                                    style = typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissAll) { Text("Xóa tất cả") }
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                }
            }
        }
    }
}

@Composable
fun NiagaraFabActionDialog(
    installedApps: List<AppInfo>,
    selectedPackage: String?,
    onDismiss: () -> Unit,
    onSelectPackage: (String?) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tùy chỉnh nút Niagara", color = colors.onSurface, style = typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectPackage(null) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.secondary)
                        Text("Tìm kiếm nhanh (Mặc định)", color = colors.onSurface, style = typography.bodyLarge)
                    }
                    if (selectedPackage == null) Text("Đang chọn", color = colors.secondary, style = typography.labelSmall)
                }
                HorizontalDivider(color = colors.surfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(installedApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPackage(app.packageName) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                app.label,
                                color = colors.onSurface,
                                style = typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (selectedPackage == app.packageName) {
                                Text("Đang chọn", color = colors.secondary, style = typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTipsDialog(
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val tips = remember {
        listOf(
            "Kéo thanh A–Z cạnh phải" to "Duyệt nhanh toàn bộ ứng dụng theo chữ cái.",
            "Nhấn giữ ứng dụng bất kỳ" to "Ghim Favorites, tạo Thư mục, mở App Shortcuts, gom Tóm tắt thông báo hoặc Ẩn app.",
            "Vuốt lên hoặc chạm nút tròn" to "Mở Tìm kiếm nhanh, tính toán (vd: 250*3) hoặc tạo ghi chú (! mua sữa).",
            "Nhấn giữ nút tròn góc phải" to "Đổi chức năng nút Niagara sang mở nhanh 1 ứng dụng bất kỳ.",
            "Chạm vào dòng thông báo" to "Đọc đầy đủ nội dung, xóa hoặc trả lời nhanh ngay trên Home."
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Hướng dẫn thao tác nhanh", color = colors.onSurface, style = typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                tips.forEachIndexed { idx, (title, desc) ->
                    Text(title, color = colors.secondary, style = typography.labelLarge)
                    Text(desc, color = colors.onSurfaceVariant, style = typography.bodySmall)
                    if (idx < tips.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Đã hiểu")
                }
            }
        }
    }
}

@Composable
fun CalendarAgendaDialog(
    events: List<CalendarEventPreview>,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val fmt = remember { SimpleDateFormat("EEE dd/MM · HH:mm", Locale("vi", "VN")) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = colors.secondary)
                    Text("Lịch sắp tới (7 ngày)", color = colors.onSurface, style = typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (events.isEmpty()) {
                    Text(
                        "Không có sự kiện nào trong 7 ngày tới.",
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(events) { ev ->
                            Column {
                                Text(
                                    text = fmt.format(Date(ev.startTimeMillis)),
                                    color = colors.secondary,
                                    style = typography.labelSmall
                                )
                                Text(
                                    text = ev.title,
                                    color = colors.onSurface,
                                    style = typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Đóng")
                }
            }
        }
    }
}

@Composable
fun RenameAppDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var text by remember { mutableStateOf(app.label) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Đổi tên ứng dụng", color = colors.onSurface, style = typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSave(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tên gốc")
                    }
                    Button(
                        onClick = { onSave(text) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = colors.onSurface,
            style = typography.bodyLarge
        )
    }
}

@Composable
fun QuickNotesDialog(
    notes: List<QuickNote>,
    onToggleNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Việc cần làm",
                    color = colors.onSurface,
                    style = typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gõ \"! nội dung\" trong thanh tìm kiếm để thêm nhanh.",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (notes.isEmpty()) {
                    Text(
                        text = "Chưa có ghi chú nào.",
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
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
                                    color = if (note.isDone) colors.onSurfaceVariant else colors.onSurface,
                                    style = typography.bodyLarge,
                                    textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteNote(note.id) },
                                    modifier = Modifier.size(40.dp)
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
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenTimeUsageDialog(
    installedApps: List<AppInfo>,
    weeklyUsageMillis: Map<String, Long>,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val topUsage = remember(installedApps, weeklyUsageMillis) {
        val map = installedApps.associateBy { it.packageName }
        weeklyUsageMillis.entries
            .filter { it.value >= 60_000L && map.containsKey(it.key) }
            .sortedByDescending { it.value }
            .take(10)
            .mapNotNull { (pkg, ms) -> map[pkg]?.let { it to ms } }
    }
    val totalMinutes = remember(weeklyUsageMillis) {
        (weeklyUsageMillis.values.sum() / 60_000L).toInt()
    }
    val maxMillis = topUsage.firstOrNull()?.second?.coerceAtLeast(1L) ?: 1L

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Thống kê sử dụng (7 ngày)",
                    color = colors.onSurface,
                    style = typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                val totalHours = totalMinutes / 60
                val remMins = totalMinutes % 60
                Text(
                    text = if (totalMinutes > 0) "Tổng cộng: ${totalHours}g ${remMins}p" else "Chưa có dữ liệu sử dụng",
                    color = colors.secondary,
                    style = typography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (topUsage.isEmpty()) {
                    Text(
                        text = "Cấp quyền Usage Access trong Cài đặt để xem thời gian sử dụng từng ứng dụng.",
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(topUsage, key = { it.first.packageName }) { (app, ms) ->
                            val mins = (ms / 60_000L).toInt()
                            val h = mins / 60
                            val m = mins % 60
                            val formatted = if (h > 0) "${h}g ${m}p" else "${m}p"
                            val progress = (ms.toFloat() / maxMillis.toFloat()).coerceIn(0.05f, 1f)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(
                                    packageName = app.packageName,
                                    contentDescription = app.label,
                                    size = 32.dp,
                                    iconPackPackage = iconPackPackage,
                                    monochrome = monochromeIcons
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = app.label,
                                            color = colors.onSurface,
                                            style = typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatted,
                                            color = colors.onSurfaceVariant,
                                            style = typography.labelMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = colors.secondary,
                                        trackColor = colors.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NiagaraSearchOptionsSheet(
    showContactSearch: Boolean,
    smartSortEnabled: Boolean,
    hasLastResult: Boolean,
    onDismiss: () -> Unit,
    onToggleContactSearch: (Boolean) -> Unit,
    onToggleSmartSort: (Boolean) -> Unit,
    onQuickInsert: (String) -> Unit,
    onClearLastResult: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "Tìm kiếm",
                color = colors.onSurface,
                style = typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .clickable { onToggleContactSearch(!showContactSearch) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tìm kiếm trong danh bạ", color = colors.onSurface, style = typography.bodyLarge)
                    Text(
                        "Thêm các liên hệ vào mục tìm kiếm",
                        color = colors.onSurfaceVariant,
                        style = typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(checked = showContactSearch, onCheckedChange = onToggleContactSearch)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .clickable { onToggleSmartSort(!smartSortEnabled) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sắp xếp thông minh", color = colors.onSurface, style = typography.bodyLarge)
                    Text(
                        "Ưu tiên ứng dụng mở thường xuyên",
                        color = colors.onSurfaceVariant,
                        style = typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(checked = smartSortEnabled, onCheckedChange = onToggleSmartSort)
            }

            HorizontalDivider(
                color = colors.surfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Lệnh nhanh offline",
                color = colors.secondary,
                style = typography.labelMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { onQuickInsert("! ") }) {
                    Text("! Ghi chú")
                }
                OutlinedButton(onClick = { onQuickInsert("/coin") }) {
                    Text("/coin")
                }
                OutlinedButton(onClick = { onQuickInsert("/dice") }) {
                    Text("/dice")
                }
            }

            if (hasLastResult) {
                DialogOptionRow(
                    icon = Icons.Default.DeleteOutline,
                    label = "Xóa \"Kết quả cuối\"",
                    onClick = {
                        onClearLastResult()
                        onDismiss()
                    }
                )
            }

            DialogOptionRow(
                icon = Icons.Default.Info,
                label = "Cài đặt Launcher",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextSwitcherSheet(
    activeProfile: ContextProfile,
    activeCustomContextId: String?,
    customContexts: List<CustomContextProfile>,
    builtInConfigs: Map<ContextProfile, BuiltInContextConfig> = emptyMap(),
    pinnedByProfile: Map<ContextProfile, Set<String>>,
    isFocusRunning: Boolean = false,
    onDismiss: () -> Unit,
    onSelectBuiltIn: (ContextProfile) -> Unit,
    onSelectCustom: (String) -> Unit,
    onPickAppsForBuiltIn: (ContextProfile) -> Unit,
    onPickAppsForCustom: (CustomContextProfile) -> Unit,
    onEditBuiltInContext: (ContextProfile) -> Unit = {},
    onEditCustomContext: (CustomContextProfile) -> Unit = {},
    onCreateCustomContext: () -> Unit,
    onStartFocus: () -> Unit = {},
    onStopFocus: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Ngữ cảnh màn hình chính",
                color = colors.onSurface,
                style = typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Chọn ngữ cảnh hoặc tùy chỉnh ứng dụng hiển thị",
                color = colors.onSurfaceVariant,
                style = typography.bodySmall
            )
            Spacer(modifier = Modifier.height(14.dp))

            listOf(ContextProfile.WORK, ContextProfile.EVENING, ContextProfile.SLEEP).forEach { profile ->
                val isSelected = activeCustomContextId == null && activeProfile == profile
                val count = pinnedByProfile[profile]?.size ?: 0
                val cfg = builtInConfigs[profile]
                val dotColor = Color(
                    cfg?.colorHex ?: when (profile) {
                        ContextProfile.EVENING -> 0xFFFFB74DL
                        ContextProfile.SLEEP -> 0xFF5C6BC0L
                        else -> 0xFF6B8CFFL
                    }
                )
                val rules = buildList {
                    if (cfg?.autoTimeStartHour != null && cfg.autoTimeEndHour != null) {
                        add("${cfg.autoTimeStartHour}–${cfg.autoTimeEndHour}h")
                    } else {
                        add(profile.timeHint)
                    }
                    add("$count app")
                    if (cfg?.restrictOtherApps == true) add("Hạn chế app")
                    if (cfg?.monochromeIcons == true) add("Trắng đen")
                    if (cfg?.oledPureBlack == true) add("Nền đen")
                }.joinToString(" · ")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectBuiltIn(profile)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                color = if (isSelected) colors.primary else colors.onSurface,
                                style = typography.bodyLarge
                            )
                            Text(
                                text = rules,
                                color = colors.onSurfaceVariant,
                                style = typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    TextButton(onClick = {
                        onDismiss()
                        onEditBuiltInContext(profile)
                    }) {
                        Text("Tùy chỉnh", color = colors.secondary)
                    }
                }
            }

            if (customContexts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = colors.surfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                customContexts.forEach { ctx ->
                    val isSelected = activeCustomContextId == ctx.id
                    val rules = buildList {
                        add("${ctx.packages.size} app")
                        if (ctx.restrictOtherApps) add("Hạn chế app")
                        if (ctx.monochromeIcons) add("Trắng đen")
                        if (ctx.oledPureBlack) add("Nền đen")
                        if (ctx.autoOnHeadphones) add("Tai nghe")
                        if (ctx.autoOnCharging) add("Cắm sạc")
                        if (ctx.autoTimeStartHour != null && ctx.autoTimeEndHour != null) {
                            add("${ctx.autoTimeStartHour}–${ctx.autoTimeEndHour}h")
                        }
                    }.joinToString(" · ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelectCustom(ctx.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(ctx.colorHex))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ctx.name,
                                    color = if (isSelected) colors.primary else colors.onSurface,
                                    style = typography.bodyLarge
                                )
                                Text(
                                    text = rules,
                                    color = colors.onSurfaceVariant,
                                    style = typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        TextButton(onClick = {
                            onDismiss()
                            onEditCustomContext(ctx)
                        }) {
                            Text("Tùy chỉnh", color = colors.secondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    onDismiss()
                    if (isFocusRunning) onStopFocus() else onStartFocus()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isFocusRunning) "Kết thúc Tập trung" else "Bắt đầu Tập trung (25p)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onDismiss()
                    onCreateCustomContext()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("+ Tạo ngữ cảnh riêng")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextAppPickerDialog(
    contextTitle: String,
    isCustomContext: Boolean,
    installedApps: List<AppInfo>,
    selectedPackages: Set<String>,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit,
    onDeleteContext: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var currentSelection by remember(selectedPackages) { mutableStateOf(selectedPackages) }
    val filteredApps = remember(installedApps, query) {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) installedApps
        else installedApps.filter { it.label.lowercase().contains(clean) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chọn app cho \"$contextTitle\"",
                        color = colors.onSurface,
                        style = typography.titleMedium
                    )
                    Text(
                        text = "Đã chọn ${currentSelection.size} ứng dụng",
                        color = colors.onSurfaceVariant,
                        style = typography.bodySmall
                    )
                }
                if (isCustomContext && onDeleteContext != null) {
                    TextButton(onClick = {
                        onDeleteContext()
                        onDismiss()
                    }) {
                        Text("Xóa", color = colors.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tìm ứng dụng...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val checked = app.packageName in currentSelection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSelection = if (checked) {
                                    currentSelection - app.packageName
                                } else {
                                    currentSelection + app.packageName
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            packageName = app.packageName,
                            contentDescription = app.label,
                            size = 34.dp,
                            iconPackPackage = iconPackPackage,
                            monochrome = monochromeIcons
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = app.label,
                            color = colors.onSurface,
                            style = typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                currentSelection = if (isChecked) {
                                    currentSelection + app.packageName
                                } else {
                                    currentSelection - app.packageName
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Hủy")
                }
                Button(
                    onClick = {
                        onSave(currentSelection)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Lưu")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CreateCustomContextDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var name by remember { mutableStateOf("") }
    var monochrome by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Tạo ngữ cảnh riêng",
                    color = colors.onSurface,
                    style = typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ví dụ: Học tập, Tập Gym, Đọc sách, Lái xe...",
                    color = colors.onSurfaceVariant,
                    style = typography.bodySmall
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên ngữ cảnh") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { monochrome = !monochrome }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = monochrome,
                        onCheckedChange = { monochrome = it },
                        colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Icon đơn sắc (giảm xao nhãng)",
                        color = colors.onSurface,
                        style = typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name.trim(), monochrome)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Tạo")
                    }
                }
            }
        }
    }
}

private val MODE_COLOR_PRESETS = listOf(
    0xFF7CDC86L,
    0xFF6B8CFFL,
    0xFFFFB74DL,
    0xFFD4BBFFL,
    0xFFFF8A80L,
    0xFF4DB6ACL
)

private val MODE_TIME_PRESETS = listOf(
    Triple("Tắt", null, null),
    Triple("08–12h", 8, 12),
    Triple("13–17h", 13, 17),
    Triple("19–22h", 19, 22),
    Triple("22–06h", 22, 6)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContextModeEditorSheet(
    initialProfile: CustomContextProfile?,
    isBuiltIn: Boolean = false,
    installedApps: List<AppInfo> = emptyList(),
    initialPackages: Set<String> = initialProfile?.packages ?: emptySet(),
    iconPackPackage: String? = null,
    onDismiss: () -> Unit,
    onSave: (CustomContextProfile) -> Unit,
    onSavePackages: ((Set<String>) -> Unit)? = null,
    onDelete: ((String) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = initialProfile != null

    var name by remember(initialProfile) { mutableStateOf(initialProfile?.name.orEmpty()) }
    var colorHex by remember(initialProfile) { mutableStateOf(initialProfile?.colorHex ?: 0xFF7CDC86L) }
    var selectedPackages by remember(initialProfile, initialPackages) { mutableStateOf(initialPackages) }
    var showAppPicker by remember { mutableStateOf(false) }
    var restrictOtherApps by remember(initialProfile) { mutableStateOf(initialProfile?.restrictOtherApps ?: false) }
    var hideNotifications by remember(initialProfile) { mutableStateOf(initialProfile?.hideNotifications ?: false) }
    var monochromeIcons by remember(initialProfile) { mutableStateOf(initialProfile?.monochromeIcons ?: false) }
    var oledPureBlack by remember(initialProfile) { mutableStateOf(initialProfile?.oledPureBlack ?: false) }
    var muteMediaOnEnter by remember(initialProfile) { mutableStateOf(initialProfile?.muteMediaOnEnter ?: false) }
    var autoOnHeadphones by remember(initialProfile) { mutableStateOf(initialProfile?.autoOnHeadphones ?: false) }
    var autoOnCharging by remember(initialProfile) { mutableStateOf(initialProfile?.autoOnCharging ?: false) }
    var autoStartHour by remember(initialProfile) { mutableStateOf(initialProfile?.autoTimeStartHour) }
    var autoEndHour by remember(initialProfile) { mutableStateOf(initialProfile?.autoTimeEndHour) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Tùy chỉnh chế độ" else "Tạo chế độ mới",
                    color = colors.onSurface,
                    style = typography.titleMedium
                )
                if (!isBuiltIn && isEditing && initialProfile != null && onDelete != null) {
                    TextButton(onClick = {
                        onDelete(initialProfile.id)
                        onDismiss()
                    }) {
                        Text("Xóa", color = colors.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (!isBuiltIn) name = it },
                enabled = !isBuiltIn,
                label = { Text("Tên chế độ (vd: Học tập, Gym, Đọc sách)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text("Ứng dụng hiển thị", color = colors.primary, style = typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = { showAppPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Chọn ứng dụng (${selectedPackages.size} đã chọn)")
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Màu sắc không khí", color = colors.onSurfaceVariant, style = typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MODE_COLOR_PRESETS.forEach { hex ->
                    val selected = colorHex == hex
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .then(
                                if (selected) Modifier.border(2.5.dp, colors.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Bật tự động", color = colors.primary, style = typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            ModeEditorSwitchRow(
                title = "Khi kết nối tai nghe / Bluetooth",
                checked = autoOnHeadphones,
                onCheckedChange = { autoOnHeadphones = it }
            )
            ModeEditorSwitchRow(
                title = "Khi đang cắm sạc",
                checked = autoOnCharging,
                onCheckedChange = { autoOnCharging = it }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Theo khung giờ", color = colors.onSurfaceVariant, style = typography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MODE_TIME_PRESETS.forEach { (label, s, e) ->
                    val selected = autoStartHour == s && autoEndHour == e
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) colors.primary.copy(alpha = 0.2f) else colors.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                autoStartHour = s
                                autoEndHour = e
                            }
                    ) {
                        Text(
                            text = label,
                            color = if (selected) colors.primary else colors.onSurfaceVariant,
                            style = typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Kiểm soát & Chống xao nhãng", color = colors.primary, style = typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            ModeEditorSwitchRow(
                title = "Hạn chế ứng dụng ngoài chế độ (dừng 5s)",
                checked = restrictOtherApps,
                onCheckedChange = { restrictOtherApps = it }
            )
            ModeEditorSwitchRow(
                title = "Ẩn xem trước thông báo",
                checked = hideNotifications,
                onCheckedChange = { hideNotifications = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Thay đổi cài đặt khi bật", color = colors.primary, style = typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            ModeEditorSwitchRow(
                title = "Trắng đen (Icon đơn sắc)",
                checked = monochromeIcons,
                onCheckedChange = { monochromeIcons = it }
            )
            ModeEditorSwitchRow(
                title = "Nền đen thuần OLED (Ẩn hình nền)",
                checked = oledPureBlack,
                onCheckedChange = { oledPureBlack = it }
            )
            ModeEditorSwitchRow(
                title = "Tắt tiếng Media khi bật chế độ",
                checked = muteMediaOnEnter,
                onCheckedChange = { muteMediaOnEnter = it }
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Thoát")
                }
                Button(
                    onClick = {
                        val clean = name.trim()
                        if (clean.isNotEmpty()) {
                            val result = (initialProfile ?: CustomContextProfile(
                                id = java.util.UUID.randomUUID().toString(),
                                name = clean
                            )).copy(
                                name = clean,
                                colorHex = colorHex,
                                packages = selectedPackages,
                                favoriteOrder = (initialProfile?.favoriteOrder.orEmpty().filter { it in selectedPackages } +
                                    selectedPackages.filter { it !in initialProfile?.favoriteOrder.orEmpty() }),
                                restrictOtherApps = restrictOtherApps,
                                hideNotifications = hideNotifications,
                                monochromeIcons = monochromeIcons,
                                oledPureBlack = oledPureBlack,
                                muteMediaOnEnter = muteMediaOnEnter,
                                autoTimeStartHour = autoStartHour,
                                autoTimeEndHour = autoEndHour,
                                autoOnHeadphones = autoOnHeadphones,
                                autoOnCharging = autoOnCharging
                            )
                            onSavePackages?.invoke(selectedPackages)
                            onSave(result)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Hoàn tất")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showAppPicker) {
        ContextAppPickerDialog(
            contextTitle = name.ifBlank { "Chế độ mới" },
            isCustomContext = false,
            installedApps = installedApps,
            selectedPackages = selectedPackages,
            iconPackPackage = iconPackPackage,
            monochromeIcons = monochromeIcons,
            onDismiss = { showAppPicker = false },
            onSave = { updated -> selectedPackages = updated }
        )
    }
}

@Composable
private fun ModeEditorSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.onSurface,
            style = typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
