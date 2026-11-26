package com.indie.mindfullauncher.domain

import com.indie.mindfullauncher.model.AppInfo
import java.text.DecimalFormat
import kotlin.random.Random

sealed interface CommandResult {
    data object Empty : CommandResult
    data class NoteAction(val noteText: String) : CommandResult
    data class MathResult(val expression: String, val formattedValue: String) : CommandResult
    data class UtilityResult(val kind: String, val title: String, val value: String, val hint: String) : CommandResult
    data class AppMatches(val apps: List<AppInfo>) : CommandResult
}

object CommandBarEngine {

    private val mathPattern = Regex("""^[\d\s+\-*/().%]+$""")
    private val formatter = DecimalFormat("#,##0.####")

    fun evaluate(rawQuery: String, installedApps: List<AppInfo>, random: Random = Random.Default): CommandResult {
        val query = rawQuery.trim()
        if (query.isEmpty()) return CommandResult.Empty

        if (query.startsWith("!")) {
            return CommandResult.NoteAction(query.removePrefix("!").trim())
        }

        val lower = query.lowercase()
        if (lower == "/coin" || lower == "coin" || lower == "tung đồng xu" || lower == "tung dong xu") {
            val isHeads = random.nextBoolean()
            return CommandResult.UtilityResult(
                kind = "coin",
                title = "Tung đồng xu",
                value = if (isHeads) "Ngửa (Heads)" else "Sấp (Tails)",
                hint = "Chạm để tung lại"
            )
        }
        if (lower == "/dice" || lower == "dice" || lower == "xúc xắc" || lower == "xuc xac") {
            val roll = random.nextInt(1, 7)
            return CommandResult.UtilityResult(
                kind = "dice",
                title = "Gieo xúc xắc",
                value = "Mặt $roll",
                hint = "Chạm để gieo lại"
            )
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

        val prefixMatches = installedApps.filter { it.label.lowercase().startsWith(lower) }
        val containsMatches = installedApps.filter {
            !it.label.lowercase().startsWith(lower) && it.label.lowercase().contains(lower)
        }
        return CommandResult.AppMatches((prefixMatches + containsMatches).take(12))
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
