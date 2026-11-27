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
        AppInfo("Notes", "com.example.notes"),
        AppInfo("Tin nhắn", "com.google.android.apps.messaging"),
        AppInfo("Đồng hồ", "com.google.android.deskclock")
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

    @Test
    fun `matches vietnamese app names without diacritics`() {
        val msg = CommandBarEngine.evaluate("tin nhan", sampleApps) as CommandResult.AppMatches
        assertEquals("Tin nhắn", msg.apps.first().label)

        val clock = CommandBarEngine.evaluate("dong ho", sampleApps) as CommandResult.AppMatches
        assertEquals("Đồng hồ", clock.apps.first().label)
    }

    @Test
    fun `evaluates offline unit and temperature conversions`() {
        val km = CommandBarEngine.evaluate("5km to m", sampleApps) as CommandResult.ConversionResult
        assertEquals("5,000", km.formattedValue)
        assertEquals("m", km.unitLabel)

        val temp = CommandBarEngine.evaluate("25c sang f", sampleApps) as CommandResult.ConversionResult
        assertEquals("77", temp.formattedValue)
        assertEquals("°F", temp.unitLabel)
    }

    @Test
    fun `parses raycast system quick actions`() {
        val flash = CommandBarEngine.evaluate("/flash", sampleApps) as CommandResult.SystemActionResult
        assertEquals("flash", flash.actionId)

        val timer = CommandBarEngine.evaluate("/timer 15m", sampleApps) as CommandResult.SystemActionResult
        assertEquals("timer", timer.actionId)
        assertEquals(900, timer.argInt)

        val focus = CommandBarEngine.evaluate("/focus 30m", sampleApps) as CommandResult.SystemActionResult
        assertEquals("focus", focus.actionId)
        assertEquals(30, focus.argInt)
    }
}
