package com.indie.mindfullauncher.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.indie.mindfullauncher.model.AccentPreset
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ClockStyle
import com.indie.mindfullauncher.model.ContextProfile
import com.indie.mindfullauncher.model.IconShapePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryInstrumentedTest {

    private lateinit var context: Context
    private lateinit var repo: PreferencesRepository

    private val sampleApps = listOf(
        AppInfo("Calendar", "com.example.calendar"),
        AppInfo("Chrome", "com.example.chrome"),
        AppInfo("Notes", "com.example.notes")
    )

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("stillness_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        repo = PreferencesRepository(context)
    }

    @Test
    fun persistsClockAccentIconShapeAndHideStatusBar() {
        repo.setClockStyle(ClockStyle.ANALOG)
        repo.setAccentPreset(AccentPreset.LAVENDER)
        repo.setIconShapePreset(IconShapePreset.DOTS)
        repo.setHideStatusBar(true)

        assertEquals(ClockStyle.ANALOG, repo.getClockStyle())
        assertEquals(AccentPreset.LAVENDER, repo.getAccentPreset())
        assertEquals(IconShapePreset.DOTS, repo.getIconShapePreset())
        assertTrue(repo.isHideStatusBar())
    }

    @Test
    fun reordersFavoritesAndPersistsCustomOrder() {
        val favs = setOf("com.example.calendar", "com.example.chrome", "com.example.notes")
        repo.savePinnedPackages(ContextProfile.WORK, favs)
        repo.saveFavoriteOrder(listOf("com.example.calendar", "com.example.chrome", "com.example.notes"))

        val moved = repo.moveFavorite("com.example.notes", -1, favs)
        assertEquals(
            listOf("com.example.calendar", "com.example.notes", "com.example.chrome"),
            moved
        )
        assertEquals(moved, repo.getFavoriteOrder(favs))
    }

    @Test
    fun createsRenamesTogglesAndDeletesFolders() {
        val created = repo.createFolder("Work", "com.example.calendar")
        assertEquals(1, created.size)
        val folderId = created.first().id

        val renamed = repo.renameFolder(folderId, "Productivity")
        assertEquals("Productivity", renamed.first().name)

        val added = repo.toggleAppInFolder(folderId, "com.example.notes")
        assertEquals(listOf("com.example.calendar", "com.example.notes"), added.first().packages)

        val deleted = repo.deleteFolder(folderId)
        assertTrue(deleted.isEmpty())
    }

    @Test
    fun managesWidgetStackAndBackupRoundTrip() {
        val added = repo.addPinnedWidgetId(101)
        val added2 = repo.addPinnedWidgetId(202)
        assertEquals(listOf(101), added)
        assertEquals(listOf(101, 202), added2)

        val removed = repo.removePinnedWidgetId(101)
        assertEquals(listOf(202), removed)

        repo.setClockStyle(ClockStyle.FLIP)
        repo.setAccentPreset(AccentPreset.GREEN)
        val json = repo.exportBackupJson(sampleApps)

        context.getSharedPreferences("stillness_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        assertFalse(repo.getClockStyle() == ClockStyle.FLIP)
        assertTrue(repo.importBackupJson(json))
        assertEquals(ClockStyle.FLIP, repo.getClockStyle())
        assertEquals(AccentPreset.GREEN, repo.getAccentPreset())
    }
}
