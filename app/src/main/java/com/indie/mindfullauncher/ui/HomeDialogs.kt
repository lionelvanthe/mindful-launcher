package com.indie.mindfullauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.NotificationPreview
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
    onDeleteFolder: () -> Unit
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = colors.secondary
                        )
                        Text(
                            text = folder.name,
                            color = colors.onSurface,
                            style = typography.titleMedium
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

@Composable
fun NiagaraAppOptionsDialog(
    app: AppInfo,
    shortcuts: List<AppShortcutItem>,
    popupWidgetId: Int,
    isFavorite: Boolean,
    isHidden: Boolean,
    isSummarized: Boolean,
    iconPackPackage: String?,
    monochromeIcons: Boolean,
    onDismiss: () -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToFolder: () -> Unit,
    onRenameApp: () -> Unit,
    onPickPopupWidget: () -> Unit,
    onClearPopupWidget: () -> Unit,
    onToggleSummary: () -> Unit,
    onToggleHidden: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit
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
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
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
            .defaultMinSize(minHeight = 50.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
