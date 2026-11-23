package com.indie.mindfullauncher.model

enum class ContextProfile(
    val displayName: String,
    val timeHint: String
) {
    WORK("Làm việc", "08:00 - 17:30"),
    EVENING("Buổi tối", "17:30 - 22:30"),
    SLEEP("Đi ngủ", "22:30 - 06:00"),
    ALL("Tất cả", "Tự do")
}

data class AppInfo(
    val label: String,
    val packageName: String
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
