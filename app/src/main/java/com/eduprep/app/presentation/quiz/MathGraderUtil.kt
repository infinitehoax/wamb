package com.eduprep.app.presentation.quiz

object MathGraderUtil {
    fun compareExactMath(student: String, actual: String): Boolean {
        val cleanStudent = cleanMathString(student)
        val cleanActual = cleanMathString(actual)
        return cleanStudent == cleanActual && cleanStudent.isNotEmpty()
    }

    private fun cleanMathString(input: String): String {
        var str = input.lowercase().trim()

        // Remove spaces
        str = str.replace("\\s+".toRegex(), "")

        // Define common units to strip (longest first to avoid partial replacement)
        val units = listOf(
            "m/s^2", "m/s2", "meters/second^2", "meters/second2",
            "m/s", "meters/second", "meters", "seconds", "second",
            "ohms", "ohm", "ω", "Ω", "volts", "volt", "amperes", "ampere",
            "amps", "amp", "watts", "watt", "joules", "joule", "newtons", "newton",
            "pascals", "pascal", "hertz", "hz", "degrees", "deg", "radians", "rad",
            "%", "kg", "g", "v", "a", "w", "j", "n", "pa", "m", "s"
        )

        for (unit in units) {
            // Match trailing unit, or unit preceded by a digit/symbol, or just the unit itself at the end
            val regex = Regex("${Regex.escape(unit)}\$")
            str = str.replace(regex, "")
        }

        return str.trim()
    }
}
