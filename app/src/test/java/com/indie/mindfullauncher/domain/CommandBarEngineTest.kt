package com.indie.mindfullauncher.domain

import com.indie.mindfullauncher.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandBarEngineTest {

    private val sampleApps = listOf(
        AppInfo("Calendar", "com.google.android.calendar"),
        AppInfo("Calculator", "com.google.android.calculator"),
        AppInfo("TikTok", "com.zhiliaoapp.musically"),
        AppInfo("Notes", "com.example.notes")
    )

    @Test
    fun `evaluates math expressions accurately`() {
        val result = CommandBarEngine.evaluate("(150 + 50) * 1.1", sampleApps)
        assertTrue(result is CommandResult.MathResult)
        assertEquals("220", (result as CommandResult.MathResult).formattedValue)
    }

    @Test
    fun `parses quick note with exclamation prefix`() {
        val result = CommandBarEngine.evaluate("! mua sua tuoi", sampleApps)
        assertTrue(result is CommandResult.NoteAction)
        assertEquals("mua sua tuoi", (result as CommandResult.NoteAction).noteText)
    }

    @Test
    fun `prioritizes prefix app matches`() {
        val result = CommandBarEngine.evaluate("cal", sampleApps)
        assertTrue(result is CommandResult.AppMatches)
        val apps = (result as CommandResult.AppMatches).apps
        assertEquals(2, apps.size)
        assertEquals("Calendar", apps[0].label)
    }

    @Test
    fun `evaluates niagara secret coin and dice utilities`() {
        val coin = CommandBarEngine.evaluate("/coin", sampleApps, kotlin.random.Random(42))
        assertTrue(coin is CommandResult.UtilityResult)
        assertEquals("coin", (coin as CommandResult.UtilityResult).kind)

        val dice = CommandBarEngine.evaluate("/dice", sampleApps, kotlin.random.Random(42))
        assertTrue(dice is CommandResult.UtilityResult)
        assertEquals("dice", (dice as CommandResult.UtilityResult).kind)
        assertTrue(dice.value.startsWith("Mặt "))
    }
}
