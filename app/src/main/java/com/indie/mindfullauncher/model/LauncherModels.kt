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
    CLASSIC("Cổ điển"),
    HORIZONTAL("Ngang"),
    VERTICAL("Dọc"),
    ANALOG("Đồng hồ kim"),
    FLIP("Lật số"),
    OVERLAP("Chồng số"),
    DAY_OF_WEEK("Thứ trong tuần"),
    MINIMALIST("Tối giản"),
    COMPACT("Gọn"),
    NONE("Ẩn đồng hồ")
}

enum class IconShapePreset(val displayName: String) {
    DEFAULT("Mặc định"),
    CIRCLE("Tròn"),
    SQUIRCLE("Bo góc"),
    DOTS("Niagara Dots")
}

enum class AccentPreset(val displayName: String, val colorHex: Long) {
    NIAGARA_BLUE("Niagara Blue", 0xFFB2C5FF),
    AMBER("Hổ phách", 0xFFFFB74D),
    GREEN("Xanh lá", 0xFF7CDC86),
    LAVENDER("Oải hương", 0xFFD4BBFF),
    CORAL("San hô", 0xFFFFB4AA)
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
    val packageName: String,
    val installedAtMillis: Long = 0L
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
    val opensToday: Int = 0,
    val hasPermission: Boolean
)

data class PostSessionNudge(
    val app: AppInfo,
    val sessionMinutes: Int,
    val opensToday: Int
)
