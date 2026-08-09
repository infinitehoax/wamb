package com.eduprep.app

import com.eduprep.app.presentation.quiz.MathGraderUtil
import org.junit.Assert.*
import org.junit.Test

class MathGraderUtilTest {

    @Test
    fun `compareExactMath returns true for exact numbers`() {
        assertTrue(MathGraderUtil.compareExactMath("15", "15"))
    }

    @Test
    fun `compareExactMath ignores whitespaces and is case insensitive`() {
        assertTrue(MathGraderUtil.compareExactMath(" 15  ", "15"))
        assertTrue(MathGraderUtil.compareExactMath("15 OHMS", "15"))
    }

    @Test
    fun `compareExactMath strips common mathematical units`() {
        assertTrue(MathGraderUtil.compareExactMath("15 m/s", "15"))
        assertTrue(MathGraderUtil.compareExactMath("15 m/s^2", "15"))
        assertTrue(MathGraderUtil.compareExactMath("15m/s2", "15"))
        assertTrue(MathGraderUtil.compareExactMath("10 ohms", "10 Ω"))
    }

    @Test
    fun `compareExactMath returns false for mismatched numbers`() {
        assertFalse(MathGraderUtil.compareExactMath("12", "15"))
    }
}
