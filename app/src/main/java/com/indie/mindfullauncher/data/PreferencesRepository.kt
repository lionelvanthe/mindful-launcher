package com.indie.mindfullauncher.data

import android.content.Context
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.QuickNote
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
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
        private const val KEY_PINNED_PREFIX = "pinned_"
        private const val KEY_FRICTION_APPS = "friction_apps"
        private const val KEY_NOTES = "quick_notes"
        private val DEFAULT_DISTRACTING_KEYWORDS = listOf(
            "tiktok", "musically", "facebook", "katana", "instagram",
            "youtube", "threads", "reddit", "twitter", "x.android", "netflix"
        )
    }
}
