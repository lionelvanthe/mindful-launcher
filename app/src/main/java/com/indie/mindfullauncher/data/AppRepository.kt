package com.indie.mindfullauncher.data

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.AppShortcutItem
import com.indie.mindfullauncher.model.AppUsageSummary
import com.indie.mindfullauncher.model.CalendarEventPreview
import com.indie.mindfullauncher.model.ContactMatch
import java.util.Calendar

class AppRepository(private val context: Context) {

    fun loadInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val selfPkg = context.packageName

        return resolved
            .asSequence()
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == selfPkg) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                if (label.isEmpty()) return@mapNotNull null
                val installedAt = runCatching { pm.getPackageInfo(pkg, 0).firstInstallTime }.getOrDefault(0L)
                AppInfo(label = label, packageName = pkg, installedAtMillis = installedAt)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == context.packageName
    }

    fun openDefaultLauncherSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadNextCalendarEvent(): CalendarEventPreview? {
        if (!hasCalendarPermission()) return null
        val now = System.currentTimeMillis()
        val end = now + 24L * 60L * 60L * 1000L
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, now)
        android.content.ContentUris.appendId(builder, end)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )
        return runCatching {
            context.contentResolver.query(
                builder.build(),
                projection,
                "${CalendarContract.Instances.ALL_DAY} = 0",
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val title = cursor.getString(0)?.trim().orEmpty()
                    val begin = cursor.getLong(1)
                    if (title.isNotEmpty()) CalendarEventPreview(title, begin) else null
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    fun hasNotificationListenerPermission(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        val cn = ComponentName(context, NiagaraNotificationListener::class.java).flattenToString()
        return enabled.contains(cn)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun loadWeeklyUsageMillis(): Map<String, Long> {
        if (!hasUsageStatsPermission()) return emptyMap()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 7L * 24L * 60L * 60L * 1000L
        return runCatching {
            usm.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, start, end)
                .orEmpty()
                .groupBy { it.packageName }
                .mapValues { (_, list) -> list.sumOf { it.totalTimeInForeground } }
        }.getOrDefault(emptyMap())
    }

    fun loadAppShortcuts(packageName: String): List<AppShortcutItem> {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return emptyList()
        if (!launcherApps.hasShortcutHostPermission()) return emptyList()
        val query = LauncherApps.ShortcutQuery().apply {
            setPackage(packageName)
            setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
            )
        }
        return runCatching {
            launcherApps.getShortcuts(query, Process.myUserHandle())
                .orEmpty()
                .take(4)
                .mapNotNull { info ->
                    val label = (info.shortLabel ?: info.longLabel)?.toString()?.trim().orEmpty()
                    if (label.isEmpty()) null
                    else AppShortcutItem(id = info.id, label = label, packageName = packageName)
                }
        }.getOrDefault(emptyList())
    }

    fun launchAppShortcut(packageName: String, shortcutId: String): Boolean {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return false
        return runCatching {
            launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle())
            true
        }.getOrDefault(false)
    }

    fun loadUpcomingCalendarEvents(daysAhead: Int = 7): List<CalendarEventPreview> {
        if (!hasCalendarPermission()) return emptyList()
        val now = System.currentTimeMillis()
        val end = now + daysAhead * 24L * 60L * 60L * 1000L
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, now)
        android.content.ContentUris.appendId(builder, end)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )
        return runCatching {
            context.contentResolver.query(
                builder.build(),
                projection,
                "${CalendarContract.Instances.ALL_DAY} = 0",
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext() && size < 15) {
                        val title = cursor.getString(0)?.trim().orEmpty()
                        val begin = cursor.getLong(1)
                        if (title.isNotEmpty()) add(CalendarEventPreview(title, begin))
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun searchContacts(query: String): List<ContactMatch> {
        if (!hasContactsPermission()) return emptyList()
        val clean = query.trim()
        if (clean.length < 2) return emptyList()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        return runCatching {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$clean%"),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val seen = HashSet<String>()
                buildList {
                    while (cursor.moveToNext() && size < 5) {
                        val id = cursor.getString(0) ?: continue
                        val name = cursor.getString(1)?.trim().orEmpty()
                        val phone = cursor.getString(2)?.trim().orEmpty()
                        if (name.isNotEmpty() && phone.isNotEmpty() && seen.add("$id:$phone")) {
                            add(ContactMatch(id = id, name = name, phoneNumber = phone))
                        }
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    fun dialContact(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.fromParts("tel", phoneNumber, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun isExternalAudioConnected(): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { dev ->
            when (dev.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_USB_HEADSET -> true
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET
                } else {
                    false
                }
            }
        }
    }

    fun getTodayUsageSummary(packageName: String): AppUsageSummary {
        if (!hasUsageStatsPermission()) {
            return AppUsageSummary(minutesToday = 0, hasPermission = false)
        }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val totalMs = stats
            ?.filter { it.packageName == packageName }
            ?.sumOf { it.totalTimeInForeground }
            ?: 0L

        return AppUsageSummary(
            minutesToday = (totalMs / 60_000L).toInt(),
            hasPermission = true
        )
    }
}
