package com.eduprep.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMaxChatHistoryTruncation() {
        val dummyMessages = listOf(
            "Msg 1",
            "Msg 2",
            "Msg 3",
            "Msg 4",
            "Msg 5",
            "Msg 6"
        )

        // Limit of 3 should take the last 3 elements
        val limitOfThree = dummyMessages.takeLast(3)
        assertEquals(3, limitOfThree.size)
        assertEquals("Msg 4", limitOfThree[0])
        assertEquals("Msg 5", limitOfThree[1])
        assertEquals("Msg 6", limitOfThree[2])

        // Limit of 10 (exceeding history length) should take all elements
        val limitOfTen = dummyMessages.takeLast(10)
        assertEquals(6, limitOfTen.size)
        assertEquals("Msg 1", limitOfTen.first())
        assertEquals("Msg 6", limitOfTen.last())

        // Limit of 0 should take no elements
        val limitOfZero = dummyMessages.takeLast(0)
        assertEquals(0, limitOfZero.size)
    }
}
