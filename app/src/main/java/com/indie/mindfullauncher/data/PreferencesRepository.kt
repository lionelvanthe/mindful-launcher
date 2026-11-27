package com.indie.mindfullauncher.data

import android.content.Context
import com.indie.mindfullauncher.model.AccentPreset
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.FontPreset
import com.indie.mindfullauncher.model.IconShapePreset
import com.indie.mindfullauncher.model.IconSizePreset
import com.indie.mindfullauncher.model.LauncherFolder
import com.indie.mindfullauncher.model.QuickNote
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("stillness_prefs", Context.MODE_PRIVATE)

    fun isAutoContextEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CONTEXT, true)

    fun setAutoContextEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONTEXT, enabled).apply()
    }

    fun isSetupBannerDismissed(): Boolean = prefs.getBoolean(KEY_SETUP_BANNER_DISMISSED, false)

    fun dismissSetupBanner() {
        prefs.edit().putBoolean(KEY_SETUP_BANNER_DISMISSED, true).apply()
    }

    fun isTipsBannerDismissed(): Boolean = prefs.getBoolean(KEY_TIPS_BANNER_DISMISSED, false)

    fun dismissTipsBanner() {
        prefs.edit().putBoolean(KEY_TIPS_BANNER_DISMISSED, true).apply()
    }

    fun getHiddenPackages(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN_APPS, emptySet())?.toSet() ?: emptySet()

    fun toggleHiddenPackage(packageName: String): Set<String> {
        val current = getHiddenPackages().toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, current).apply()
        return current
    }

    fun getIconSizePreset(): IconSizePreset {
        val raw = prefs.getString(KEY_ICON_SIZE, IconSizePreset.MEDIUM.name)
        return IconSizePreset.entries.firstOrNull { it.name == raw } ?: IconSizePreset.MEDIUM
    }

    fun setIconSizePreset(preset: IconSizePreset) {
        prefs.edit().putString(KEY_ICON_SIZE, preset.name).apply()
    }

    fun getIconShapePreset(): IconShapePreset {
        val raw = prefs.getString(KEY_ICON_SHAPE, IconShapePreset.DEFAULT.name)
        return IconShapePreset.entries.firstOrNull { it.name == raw } ?: IconShapePreset.DEFAULT
    }

    fun setIconShapePreset(preset: IconShapePreset) {
        prefs.edit().putString(KEY_ICON_SHAPE, preset.name).apply()
    }

    fun isHideStatusBar(): Boolean = prefs.getBoolean(KEY_HIDE_STATUS_BAR, false)

    fun setHideStatusBar(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_STATUS_BAR, hide).apply()
    }

    fun getFavoriteOrder(favorites: Set<String>): List<String> {
        val raw = prefs.getString(KEY_FAVORITE_ORDER, null)
        val saved = if (raw != null) {
            runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) add(arr.getString(i))
                }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val valid = saved.filter { it in favorites }
        val remaining = favorites.filter { it !in valid }
        return valid + remaining
    }

    fun saveFavoriteOrder(order: List<String>) {
        prefs.edit().putString(KEY_FAVORITE_ORDER, JSONArray(order).toString()).apply()
    }

    fun moveFavorite(packageName: String, delta: Int, currentFavorites: Set<String>): List<String> {
        val order = getFavoriteOrder(currentFavorites).toMutableList()
        val idx = order.indexOf(packageName)
        if (idx < 0) return order
        val target = (idx + delta).coerceIn(0, order.lastIndex)
        if (target != idx) {
            val item = order.removeAt(idx)
            order.add(target, item)
            saveFavoriteOrder(order)
        }
        return order
    }

    fun getClockStyle(): ClockStyle {
        val raw = prefs.getString(KEY_CLOCK_STYLE, ClockStyle.CLASSIC.name)
        return ClockStyle.entries.firstOrNull { it.name == raw } ?: ClockStyle.CLASSIC
    }

    fun setClockStyle(style: ClockStyle) {
        prefs.edit().putString(KEY_CLOCK_STYLE, style.name).apply()
    }

    fun getAccentPreset(): AccentPreset {
        val raw = prefs.getString(KEY_ACCENT_PRESET, AccentPreset.NIAGARA_BLUE.name)
        return AccentPreset.entries.firstOrNull { it.name == raw } ?: AccentPreset.NIAGARA_BLUE
    }

    fun setAccentPreset(preset: AccentPreset) {
        prefs.edit().putString(KEY_ACCENT_PRESET, preset.name).apply()
    }

    fun isHideFavoriteLabels(): Boolean = prefs.getBoolean(KEY_HIDE_FAV_LABELS, false)

    fun setHideFavoriteLabels(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_FAV_LABELS, hide).apply()
    }

    fun isShowBatteryOnHome(): Boolean = prefs.getBoolean(KEY_SHOW_BATTERY, true)

    fun setShowBatteryOnHome(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()
    }

    fun isShowAlphabetWave(): Boolean = prefs.getBoolean(KEY_SHOW_ALPHABET_WAVE, true)

    fun setShowAlphabetWave(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ALPHABET_WAVE, show).apply()
    }

    fun isShowWallpaperBackground(): Boolean = prefs.getBoolean(KEY_SHOW_WALLPAPER_BG, true)

    fun setShowWallpaperBackground(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WALLPAPER_BG, show).apply()
    }

    fun isShowCalendarEvents(): Boolean = prefs.getBoolean(KEY_SHOW_CALENDAR, true)

    fun setShowCalendarEvents(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_CALENDAR, show).apply()
    }

    fun isShowNotificationPreviews(): Boolean = prefs.getBoolean(KEY_SHOW_NOTIF_PREVIEW, true)

    fun setShowNotificationPreviews(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NOTIF_PREVIEW, show).apply()
    }

    fun isShowMediaPlayer(): Boolean = prefs.getBoolean(KEY_SHOW_MEDIA_PLAYER, true)

    fun setShowMediaPlayer(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MEDIA_PLAYER, show).apply()
    }

    fun isSmartSortEnabled(): Boolean = prefs.getBoolean(KEY_SMART_SORT, false)

    fun setSmartSortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMART_SORT, enabled).apply()
    }

    fun isShowMostUsedApps(): Boolean = prefs.getBoolean(KEY_SHOW_MOST_USED, true)

    fun setShowMostUsedApps(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MOST_USED, show).apply()
    }

    fun isMonochromeIcons(): Boolean = prefs.getBoolean(KEY_MONOCHROME_ICONS, false)

    fun setMonochromeIcons(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONOCHROME_ICONS, enabled).apply()
    }

    fun getIconPackPackage(): String? = prefs.getString(KEY_ICON_PACK_PKG, null)

    fun setIconPackPackage(packageName: String?) {
        prefs.edit().putString(KEY_ICON_PACK_PKG, packageName).apply()
    }

    fun getNiagaraButtonPackage(): String? = prefs.getString(KEY_NIAGARA_FAB_PKG, null)

    fun setNiagaraButtonPackage(packageName: String?) {
        prefs.edit().putString(KEY_NIAGARA_FAB_PKG, packageName).apply()
    }

    fun getPinnedWidgetId(): Int = prefs.getInt(KEY_PINNED_WIDGET_ID, -1)

    fun setPinnedWidgetId(widgetId: Int) {
        prefs.edit().putInt(KEY_PINNED_WIDGET_ID, widgetId).apply()
    }

    fun getPinnedWidgetIds(): List<Int> {
        val raw = prefs.getString(KEY_PINNED_WIDGET_IDS, null)
        val list = if (raw != null) {
            runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val id = arr.optInt(i, -1)
                        if (id > 0) add(id)
                    }
                }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        if (list.isNotEmpty()) return list
        val legacy = getPinnedWidgetId()
        return if (legacy > 0) listOf(legacy) else emptyList()
    }

    fun addPinnedWidgetId(widgetId: Int): List<Int> {
        if (widgetId <= 0) return getPinnedWidgetIds()
        val current = (getPinnedWidgetIds() + widgetId).distinct()
        savePinnedWidgetIds(current)
        return current
    }

    fun removePinnedWidgetId(widgetId: Int): List<Int> {
        val current = getPinnedWidgetIds().filterNot { it == widgetId }
        savePinnedWidgetIds(current)
        return current
    }

    private fun savePinnedWidgetIds(ids: List<Int>) {
        prefs.edit()
            .putString(KEY_PINNED_WIDGET_IDS, JSONArray(ids).toString())
            .putInt(KEY_PINNED_WIDGET_ID, ids.firstOrNull() ?: -1)
            .apply()
    }

    fun getSummaryPackages(): Set<String> =
        prefs.getStringSet(KEY_SUMMARY_APPS, emptySet())?.toSet() ?: emptySet()

    fun toggleSummaryPackage(packageName: String): Set<String> {
        val current = getSummaryPackages().toMutableSet()
        if (!current.add(packageName)) current.remove(packageName)
        prefs.edit().putStringSet(KEY_SUMMARY_APPS, current).apply()
        return current
    }

    fun getFontPreset(): FontPreset {
        val raw = prefs.getString(KEY_FONT_PRESET, FontPreset.NIAGARA.name)
        return FontPreset.entries.firstOrNull { it.name == raw } ?: FontPreset.NIAGARA
    }

    fun setFontPreset(preset: FontPreset) {
        prefs.edit().putString(KEY_FONT_PRESET, preset.name).apply()
    }

    fun isShowContactSearch(): Boolean = prefs.getBoolean(KEY_SHOW_CONTACTS, true)

    fun setShowContactSearch(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_CONTACTS, show).apply()
    }

    fun isShowMusicOnHeadphones(): Boolean = prefs.getBoolean(KEY_SHOW_MUSIC_HEADPHONES, true)

    fun setShowMusicOnHeadphones(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MUSIC_HEADPHONES, show).apply()
    }

    fun isTwoColumnLargeScreen(): Boolean = prefs.getBoolean(KEY_TWO_COLUMN, true)

    fun setTwoColumnLargeScreen(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TWO_COLUMN, enabled).apply()
    }

    fun getCustomAppNames(): Map<String, String> {
        val raw = prefs.getString(KEY_CUSTOM_APP_NAMES, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    val v = obj.optString(key).trim()
                    if (v.isNotEmpty()) put(key, v)
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun setCustomAppName(packageName: String, newName: String?): Map<String, String> {
        val current = getCustomAppNames().toMutableMap()
        val clean = newName?.trim().orEmpty()
        if (clean.isEmpty()) current.remove(packageName) else current[packageName] = clean
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_CUSTOM_APP_NAMES, obj.toString()).apply()
        return current
    }

    fun getAppPopupWidgets(): Map<String, Int> {
        val raw = prefs.getString(KEY_APP_POPUP_WIDGETS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    val id = obj.optInt(key, -1)
                    if (id > 0) put(key, id)
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun setAppPopupWidget(packageName: String, widgetId: Int): Map<String, Int> {
        val current = getAppPopupWidgets().toMutableMap()
        if (widgetId > 0) current[packageName] = widgetId else current.remove(packageName)
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_APP_POPUP_WIDGETS, obj.toString()).apply()
        return current
    }

    fun getFolders(): List<LauncherFolder> {
        val raw = prefs.getString(KEY_FOLDERS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val pkgsArr = obj.optJSONArray("packages") ?: JSONArray()
                    val pkgs = buildList {
                        for (j in 0 until pkgsArr.length()) {
                            add(pkgsArr.getString(j))
                        }
                    }
                    add(
                        LauncherFolder(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            packages = pkgs
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createFolder(name: String, initialPackage: String? = null): List<LauncherFolder> {
        val clean = name.trim().ifEmpty { "Thư mục" }
        val folder = LauncherFolder(
            id = UUID.randomUUID().toString(),
            name = clean,
            packages = if (initialPackage != null) listOf(initialPackage) else emptyList()
        )
        val updated = getFolders() + folder
        saveFolders(updated)
        return updated
    }

    fun deleteFolder(folderId: String): List<LauncherFolder> {
        val updated = getFolders().filterNot { it.id == folderId }
        saveFolders(updated)
        return updated
    }

    fun renameFolder(folderId: String, newName: String): List<LauncherFolder> {
        val clean = newName.trim().ifEmpty { return getFolders() }
        val updated = getFolders().map { if (it.id == folderId) it.copy(name = clean) else it }
        saveFolders(updated)
        return updated
    }

    fun toggleAppInFolder(folderId: String, packageName: String): List<LauncherFolder> {
        val updated = getFolders().map { folder ->
            if (folder.id != folderId) {
                folder
            } else {
                val next = if (packageName in folder.packages) {
                    folder.packages - packageName
                } else {
                    folder.packages + packageName
                }
                folder.copy(packages = next)
            }
        }
        saveFolders(updated)
        return updated
    }

    private fun saveFolders(folders: List<LauncherFolder>) {
        val arr = JSONArray()
        folders.forEach { folder ->
            arr.put(
                JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("packages", JSONArray(folder.packages))
                }
            )
        }
        prefs.edit().putString(KEY_FOLDERS, arr.toString()).apply()
    }

    fun exportBackupJson(installedApps: List<AppInfo>): String {
        val root = JSONObject().apply {
            put("version", 1)
            put("favorites", JSONArray(getPinnedPackages(ContextProfile.WORK, installedApps).toList()))
            put("hidden", JSONArray(getHiddenPackages().toList()))
            put("iconSize", getIconSizePreset().name)
            put("clockStyle", getClockStyle().name)
            put("accentPreset", getAccentPreset().name)
            put("hideFavoriteLabels", isHideFavoriteLabels())
            put("showBattery", isShowBatteryOnHome())
            put("showAlphabetWave", isShowAlphabetWave())
            put("showWallpaperBg", isShowWallpaperBackground())
            put("showCalendar", isShowCalendarEvents())
            put("showNotifPreview", isShowNotificationPreviews())
            put("folders", JSONArray(prefs.getString(KEY_FOLDERS, "[]")))
            put("notes", JSONArray(prefs.getString(KEY_NOTES, "[]")))
        }
        return root.toString(2)
    }

    fun importBackupJson(rawJson: String): Boolean {
        return try {
            val root = JSONObject(rawJson)
            val favArr = root.optJSONArray("favorites")
            if (favArr != null) {
                val favs = buildSet {
                    for (i in 0 until favArr.length()) add(favArr.getString(i))
                }
                savePinnedPackages(ContextProfile.WORK, favs)
            }
            val hiddenArr = root.optJSONArray("hidden")
            if (hiddenArr != null) {
                val hidden = buildSet {
                    for (i in 0 until hiddenArr.length()) add(hiddenArr.getString(i))
                }
                prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
            }
            root.optString("iconSize").takeIf { it.isNotEmpty() }?.let { raw ->
                IconSizePreset.entries.firstOrNull { it.name == raw }?.let { setIconSizePreset(it) }
            }
            root.optString("clockStyle").takeIf { it.isNotEmpty() }?.let { raw ->
                ClockStyle.entries.firstOrNull { it.name == raw }?.let { setClockStyle(it) }
            }
            root.optString("accentPreset").takeIf { it.isNotEmpty() }?.let { raw ->
                AccentPreset.entries.firstOrNull { it.name == raw }?.let { setAccentPreset(it) }
            }
            if (root.has("hideFavoriteLabels")) setHideFavoriteLabels(root.getBoolean("hideFavoriteLabels"))
            if (root.has("showBattery")) setShowBatteryOnHome(root.getBoolean("showBattery"))
            if (root.has("showAlphabetWave")) setShowAlphabetWave(root.getBoolean("showAlphabetWave"))
            if (root.has("showWallpaperBg")) setShowWallpaperBackground(root.getBoolean("showWallpaperBg"))
            if (root.has("showCalendar")) setShowCalendarEvents(root.getBoolean("showCalendar"))
            if (root.has("showNotifPreview")) setShowNotificationPreviews(root.getBoolean("showNotifPreview"))
            root.optJSONArray("folders")?.let { prefs.edit().putString(KEY_FOLDERS, it.toString()).apply() }
            root.optJSONArray("notes")?.let { prefs.edit().putString(KEY_NOTES, it.toString()).apply() }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentTimeProfile(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): ContextProfile {
        return when {
            hourOfDay in 8..17 -> ContextProfile.WORK
            hourOfDay in 18..22 -> ContextProfile.EVENING
            else -> ContextProfile.SLEEP
        }
    }

    fun getPinnedPackages(profile: ContextProfile, installedApps: List<AppInfo>): Set<String> {
        val key = "$KEY_PINNED_PREFIX${profile.name}"
        if (!prefs.contains(key)) {
            val defaults = suggestDefaults(profile, installedApps)
            savePinnedPackages(profile, defaults)
            return defaults
        }
        return prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    }

    fun savePinnedPackages(profile: ContextProfile, packages: Set<String>) {
        prefs.edit().putStringSet("$KEY_PINNED_PREFIX${profile.name}", packages).apply()
    }

    fun togglePinnedPackage(profile: ContextProfile, packageName: String, installedApps: List<AppInfo>): Set<String> {
        val current = getPinnedPackages(profile, installedApps).toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        savePinnedPackages(profile, current)
        return current
    }

    fun getFrictionPackages(installedApps: List<AppInfo>): Set<String> {
        if (!prefs.contains(KEY_FRICTION_APPS)) {
            val defaults = installedApps
                .filter { app ->
                    val p = app.packageName.lowercase()
                    DEFAULT_DISTRACTING_KEYWORDS.any { p.contains(it) }
                }
                .map { it.packageName }
                .toSet()
            prefs.edit().putStringSet(KEY_FRICTION_APPS, defaults).apply()
            return defaults
        }
        return prefs.getStringSet(KEY_FRICTION_APPS, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleFrictionPackage(packageName: String, installedApps: List<AppInfo>): Set<String> {
        val current = getFrictionPackages(installedApps).toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_FRICTION_APPS, current).apply()
        return current
    }

    private fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun getFrictionResistedToday(): Int {
        val today = todayDateKey()
        if (prefs.getString(KEY_FRICTION_STATS_DATE, null) != today) {
            return 0
        }
        return prefs.getInt(KEY_FRICTION_RESISTED_TODAY, 0)
    }

    fun incrementFrictionResistedToday(): Int {
        val today = todayDateKey()
        val next = getFrictionResistedToday() + 1
        prefs.edit()
            .putString(KEY_FRICTION_STATS_DATE, today)
            .putInt(KEY_FRICTION_RESISTED_TODAY, next)
            .apply()
        return next
    }

    fun getLocalAppOpensToday(packageName: String): Int {
        val today = todayDateKey()
        if (prefs.getString(KEY_FRICTION_STATS_DATE, null) != today) {
            return 0
        }
        val raw = prefs.getString(KEY_LOCAL_APP_OPENS_TODAY, null) ?: return 0
        return runCatching { JSONObject(raw).optInt(packageName, 0) }.getOrDefault(0)
    }

    fun incrementLocalAppOpensToday(packageName: String): Int {
        val today = todayDateKey()
        val obj = if (prefs.getString(KEY_FRICTION_STATS_DATE, null) == today) {
            runCatching {
                JSONObject(prefs.getString(KEY_LOCAL_APP_OPENS_TODAY, null) ?: "{}")
            }.getOrElse { JSONObject() }
        } else {
            JSONObject()
        }
        val next = obj.optInt(packageName, 0) + 1
        obj.put(packageName, next)
        prefs.edit()
            .putString(KEY_FRICTION_STATS_DATE, today)
            .putString(KEY_LOCAL_APP_OPENS_TODAY, obj.toString())
            .apply()
        return next
    }

    fun getNotes(): List<QuickNote> {
        val raw = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        QuickNote(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            isDone = obj.optBoolean("isDone", false),
                            createdAt = obj.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addNote(text: String): List<QuickNote> {
        val clean = text.trim()
        if (clean.isEmpty()) return getNotes()
        val updated = listOf(QuickNote(id = UUID.randomUUID().toString(), text = clean)) + getNotes()
        saveNotes(updated)
        return updated
    }

    fun toggleNoteDone(id: String): List<QuickNote> {
        val updated = getNotes().map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        saveNotes(updated)
        return updated
    }

    fun deleteNote(id: String): List<QuickNote> {
        val updated = getNotes().filterNot { it.id == id }
        saveNotes(updated)
        return updated
    }

    private fun saveNotes(notes: List<QuickNote>) {
        val arr = JSONArray()
        notes.take(50).forEach { note ->
            arr.put(
                JSONObject().apply {
                    put("id", note.id)
                    put("text", note.text)
                    put("isDone", note.isDone)
                    put("createdAt", note.createdAt)
                }
            )
        }
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }

    private fun suggestDefaults(profile: ContextProfile, installedApps: List<AppInfo>): Set<String> {
        val keywords = when (profile) {
            ContextProfile.WORK -> listOf("mail", "calendar", "slack", "teams", "note", "docs", "chrome", "dialer", "phone", "contacts")
            ContextProfile.EVENING -> listOf("spotify", "music", "maps", "camera", "health", "fit", "book", "kindle", "podcast")
            ContextProfile.SLEEP -> listOf("clock", "alarm", "dialer", "phone")
            ContextProfile.ALL -> return installedApps.take(8).map { it.packageName }.toSet()
        }
        val matched = installedApps
            .filter { app ->
                val target = "${app.packageName} ${app.label}".lowercase()
                keywords.any { target.contains(it) }
            }
            .take(7)
            .map { it.packageName }
            .toSet()
        return matched.ifEmpty { installedApps.take(5).map { it.packageName }.toSet() }
    }

    companion object {
        private const val KEY_AUTO_CONTEXT = "auto_context_enabled"
        private const val KEY_SETUP_BANNER_DISMISSED = "setup_banner_dismissed"
        private const val KEY_TIPS_BANNER_DISMISSED = "tips_banner_dismissed"
        private const val KEY_PINNED_PREFIX = "pinned_"
        private const val KEY_FRICTION_APPS = "friction_apps"
        private const val KEY_FRICTION_STATS_DATE = "friction_stats_date"
        private const val KEY_FRICTION_RESISTED_TODAY = "friction_resisted_today"
        private const val KEY_LOCAL_APP_OPENS_TODAY = "local_app_opens_today"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_ICON_SIZE = "icon_size_preset"
        private const val KEY_ICON_SHAPE = "icon_shape_preset"
        private const val KEY_HIDE_STATUS_BAR = "hide_status_bar"
        private const val KEY_FAVORITE_ORDER = "favorite_order"
        private const val KEY_PINNED_WIDGET_IDS = "pinned_widget_ids"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_ACCENT_PRESET = "accent_preset"
        private const val KEY_HIDE_FAV_LABELS = "hide_favorite_labels"
        private const val KEY_SHOW_BATTERY = "show_battery_on_home"
        private const val KEY_SHOW_ALPHABET_WAVE = "show_alphabet_wave"
        private const val KEY_SHOW_WALLPAPER_BG = "show_wallpaper_bg"
        private const val KEY_SHOW_CALENDAR = "show_calendar_events"
        private const val KEY_SHOW_NOTIF_PREVIEW = "show_notif_preview"
        private const val KEY_SHOW_MEDIA_PLAYER = "show_media_player"
        private const val KEY_SMART_SORT = "smart_sort_enabled"
        private const val KEY_SHOW_MOST_USED = "show_most_used_apps"
        private const val KEY_MONOCHROME_ICONS = "monochrome_icons"
        private const val KEY_ICON_PACK_PKG = "icon_pack_pkg"
        private const val KEY_NIAGARA_FAB_PKG = "niagara_fab_pkg"
        private const val KEY_PINNED_WIDGET_ID = "pinned_widget_id"
        private const val KEY_SUMMARY_APPS = "summary_apps"
        private const val KEY_FONT_PRESET = "font_preset"
        private const val KEY_SHOW_CONTACTS = "show_contact_search"
        private const val KEY_SHOW_MUSIC_HEADPHONES = "show_music_headphones"
        private const val KEY_TWO_COLUMN = "two_column_large_screen"
        private const val KEY_CUSTOM_APP_NAMES = "custom_app_names"
        private const val KEY_APP_POPUP_WIDGETS = "app_popup_widgets"
        private const val KEY_FOLDERS = "launcher_folders"
        private const val KEY_NOTES = "quick_notes"
        private val DEFAULT_DISTRACTING_KEYWORDS = listOf(
            "tiktok", "musically", "facebook", "katana", "instagram",
            "youtube", "threads", "reddit", "twitter", "x.android", "netflix"
        )
    }
}
