package com.nddfeon.demonic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class RoomCodeGeneratorTest {

    @Test
    fun testRoomCodeFormat() {
        val random = SecureRandom()
        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')

        fun generateCode(): String {
            return (1..6)
                .map { random.nextInt(charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }

        for (i in 1..100) {
            val code = generateCode()
            assertEquals(6, code.length)
            assertTrue("Code $code must match [A-Z0-9]{6}", code.matches(Regex("^[A-Z0-9]{6}$")))
        }
    }
}
