package com.indie.mindfullauncher.model

import android.app.Notification
import android.app.PendingIntent

enum class ContextProfile(
    val displayName: String,
    val timeHint: String
) {
    WORK("Làm việc", "08:00 - 17:30"),
    EVENING("Buổi tối", "17:30 - 22:30"),
    SLEEP("Đi ngủ", "22:30 - 06:00"),
    ALL("Tất cả", "Tự do")
}

enum class ClockStyle(val displayName: String) {
    MINIMALIST("Tối giản"),
    COMPACT("Gọn"),
    VERTICAL("Dọc")
}

enum class IconSizePreset(val displayName: String, val dpValue: Int) {
    SMALL("Nhỏ", 34),
    MEDIUM("Vừa", 42),
    LARGE("Lớn", 50)
}

enum class FontPreset(val displayName: String) {
    NIAGARA("Kumbh & Nunito"),
    SYSTEM("Hệ thống")
}

data class AppInfo(
    val label: String,
    val packageName: String
)

data class ContactMatch(
    val id: String,
    val name: String,
    val phoneNumber: String
)

data class AppShortcutItem(
    val id: String,
    val label: String,
    val packageName: String
)

data class IconPackInfo(
    val label: String,
    val packageName: String
)

data class LauncherFolder(
    val id: String,
    val name: String,
    val packages: List<String>
)

data class CalendarEventPreview(
    val title: String,
    val startTimeMillis: Long
)

data class NotificationPreview(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val summary: String,
    val postTime: Long,
    val contentIntent: PendingIntent? = null,
    val replyAction: Notification.Action? = null,
    val replyResultKey: String? = null
)

data class MediaPlaybackState(
    val packageName: String,
    val title: String,
    val artist: String,
    val isPlaying: Boolean
)

data class QuickNote(
    val id: String,
    val text: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AppUsageSummary(
    val minutesToday: Int,
    val hasPermission: Boolean
)
