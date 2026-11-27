package com.indie.mindfullauncher.domain

import com.indie.mindfullauncher.model.AppInfo
import java.text.DecimalFormat
import java.text.Normalizer
import kotlin.random.Random

sealed interface CommandResult {
    data object Empty : CommandResult
    data class NoteAction(val noteText: String) : CommandResult
    data class MathResult(val expression: String, val formattedValue: String) : CommandResult
    data class ConversionResult(val fromExpr: String, val formattedValue: String, val unitLabel: String) : CommandResult
    data class SystemActionResult(
        val actionId: String,
        val title: String,
        val subtitle: String,
        val argInt: Int? = null
    ) : CommandResult
    data class UtilityResult(val kind: String, val title: String, val value: String, val hint: String) : CommandResult
    data class AppMatches(val apps: List<AppInfo>) : CommandResult
}

object CommandBarEngine {

    private val mathPattern = Regex("""^[\d\s+\-*/().%]+$""")
    private val conversionPattern = Regex("""^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)\s+(?:to|sang|->)\s+([a-zA-Z]+)$""")
    private val timerPattern = Regex("""^/timer(?:\s+(\d+)\s*([ms]?))?$""")
    private val focusPattern = Regex("""^/focus(?:\s+(\d+)\s*m?)?$""")
    private val combiningMarks = Regex("""\p{InCombiningDiacriticalMarks}+""")
    private val formatter = DecimalFormat("#,##0.####")

    fun normalizeVietnamese(text: String): String {
        val replaced = text.replace('đ', 'd').replace('Đ', 'D')
        val decomposed = Normalizer.normalize(replaced, Normalizer.Form.NFD)
        return combiningMarks.replace(decomposed, "").lowercase()
    }

    fun evaluate(rawQuery: String, installedApps: List<AppInfo>, random: Random = Random.Default): CommandResult {
        val query = rawQuery.trim()
        if (query.isEmpty()) return CommandResult.Empty

        if (query.startsWith("!")) {
            return CommandResult.NoteAction(query.removePrefix("!").trim())
        }

        val lower = query.lowercase()
        val normalized = normalizeVietnamese(query)

        if (lower == "/coin" || lower == "coin" || normalized == "tung dong xu") {
            val isHeads = random.nextBoolean()
            return CommandResult.UtilityResult(
                kind = "coin",
                title = "Tung đồng xu",
                value = if (isHeads) "Ngửa (Heads)" else "Sấp (Tails)",
                hint = "Chạm để tung lại"
            )
        }
        if (lower == "/dice" || lower == "dice" || normalized == "xuc xac") {
            val roll = random.nextInt(1, 7)
            return CommandResult.UtilityResult(
                kind = "dice",
                title = "Gieo xúc xắc",
                value = "Mặt $roll",
                hint = "Chạm để gieo lại"
            )
        }

        if (lower == "/flash" || normalized == "den pin" || lower == "flash" || lower == "flashlight") {
            return CommandResult.SystemActionResult(
                actionId = "flash",
                title = "Bật / Tắt đèn pin",
                subtitle = "Nhấn Enter hoặc chạm để bật/tắt đèn flash"
            )
        }
        if (lower == "/wifi") {
            return CommandResult.SystemActionResult(
                actionId = "wifi",
                title = "Mở cài đặt Wi-Fi",
                subtitle = "Chạm để mở nhanh cài đặt Wi-Fi"
            )
        }
        if (lower == "/bt" || lower == "/bluetooth") {
            return CommandResult.SystemActionResult(
                actionId = "bluetooth",
                title = "Mở cài đặt Bluetooth",
                subtitle = "Chạm để mở nhanh cài đặt Bluetooth"
            )
        }
        if (lower == "/dnd") {
            return CommandResult.SystemActionResult(
                actionId = "dnd",
                title = "Cài đặt Không làm phiền",
                subtitle = "Chạm để mở cài đặt âm thanh / DND"
            )
        }
        if (lower == "/" || lower == "/help") {
            return CommandResult.SystemActionResult(
                actionId = "help",
                title = "Lệnh nhanh Command Bar",
                subtitle = "! ghi chú · 2+2 · 5km to miles · /flash · /timer 15m · /focus 25m · /wifi · /bt · /coin · /dice"
            )
        }

        timerPattern.matchEntire(lower)?.let { match ->
            val rawNum = match.groupValues[1].toIntOrNull() ?: 5
            val unit = match.groupValues[2]
            val seconds = if (unit == "s") rawNum else rawNum * 60
            val display = if (unit == "s") "${rawNum} giây" else "${rawNum} phút"
            return CommandResult.SystemActionResult(
                actionId = "timer",
                title = "Đặt hẹn giờ $display",
                subtitle = "Nhấn Enter hoặc chạm để mở Đồng hồ",
                argInt = seconds.coerceAtLeast(1)
            )
        }

        focusPattern.matchEntire(lower)?.let { match ->
            val minutes = (match.groupValues[1].toIntOrNull() ?: 25).coerceIn(1, 180)
            return CommandResult.SystemActionResult(
                actionId = "focus",
                title = "Bắt đầu Zen Focus ($minutes phút)",
                subtitle = "Nhấn Enter hoặc chạm để bật chế độ Tập trung",
                argInt = minutes
            )
        }

        conversionPattern.matchEntire(lower)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull()
            val fromUnit = match.groupValues[2]
            val toUnit = match.groupValues[3]
            if (amount != null) {
                evaluateConversion(amount, fromUnit, toUnit)?.let { (value, unitLabel) ->
                    return CommandResult.ConversionResult(
                        fromExpr = "$amount $fromUnit → $toUnit",
                        formattedValue = formatter.format(value),
                        unitLabel = unitLabel
                    )
                }
            }
        }

        if (mathPattern.matches(query) && query.any { it in "+-*/%" }) {
            val evaluated = evaluateMath(query)
            if (evaluated != null) {
                return CommandResult.MathResult(
                    expression = query,
                    formattedValue = formatter.format(evaluated)
                )
            }
        }

        val prefixMatches = installedApps.filter {
            it.label.lowercase().startsWith(lower) || normalizeVietnamese(it.label).startsWith(normalized)
        }
        val containsMatches = installedApps.filter { app ->
            val l = app.label.lowercase()
            val n = normalizeVietnamese(app.label)
            !l.startsWith(lower) && !n.startsWith(normalized) && (l.contains(lower) || n.contains(normalized))
        }
        return CommandResult.AppMatches((prefixMatches + containsMatches).take(12))
    }

    internal fun evaluateConversion(amount: Double, from: String, to: String): Pair<Double, String>? {
        val f = from.lowercase()
        val t = to.lowercase()
        if ((f == "c" || f == "celsius") && (t == "f" || t == "fahrenheit")) {
            return (amount * 9.0 / 5.0 + 32.0) to "°F"
        }
        if ((f == "f" || f == "fahrenheit") && (t == "c" || t == "celsius")) {
            return ((amount - 32.0) * 5.0 / 9.0) to "°C"
        }

        val lengthInMeters = mapOf(
            "mm" to 0.001, "cm" to 0.01, "m" to 1.0, "km" to 1000.0,
            "in" to 0.0254, "ft" to 0.3048, "yd" to 0.9144,
            "mi" to 1609.344, "mile" to 1609.344, "miles" to 1609.344
        )
        if (f in lengthInMeters && t in lengthInMeters) {
            val meters = amount * lengthInMeters.getValue(f)
            return (meters / lengthInMeters.getValue(t)) to t
        }

        val weightInGrams = mapOf(
            "g" to 1.0, "kg" to 1000.0, "oz" to 28.3495, "lb" to 453.592, "lbs" to 453.592
        )
        if (f in weightInGrams && t in weightInGrams) {
            val grams = amount * weightInGrams.getValue(f)
            return (grams / weightInGrams.getValue(t)) to t
        }

        val dataInMb = mapOf(
            "kb" to 1.0 / 1024.0, "mb" to 1.0, "gb" to 1024.0, "tb" to 1024.0 * 1024.0
        )
        if (f in dataInMb && t in dataInMb) {
            val mb = amount * dataInMb.getValue(f)
            return (mb / dataInMb.getValue(t)) to t.uppercase()
        }

        return null
    }

    internal fun evaluateMath(expr: String): Double? {
        return try {
            val value = MathParser(expr).parse()
            if (value.isNaN() || value.isInfinite()) null else value
        } catch (_: Exception) {
            null
        }
    }

    private class MathParser(private val input: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            while (ch == ' '.code) nextChar()
            if (pos < input.length) throw IllegalArgumentException("Unexpected char: ${ch.toChar()}")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            val startPos = pos
            val x: Double
            if (eat('('.code)) {
                x = parseExpression()
                if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis")
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = input.substring(startPos, pos).toDouble()
            } else {
                throw IllegalArgumentException("Unexpected char: ${ch.toChar()}")
            }
            return x
        }
    }
}
