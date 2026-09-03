package com.nddfeon.demonic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RoomSyncCalculationTest {

    @Test
    fun testPlayingTargetPositionCalculation() {
        val lastPosition = 15.0 // seconds
        val updatedAt = 1000000L // ms
        val serverNow = 1005000L // 5 seconds later (ms)

        val deltaSeconds = (serverNow - updatedAt) / 1000.0
        val targetPosition = lastPosition + deltaSeconds

        assertEquals(20.0, targetPosition, 0.001)
    }

    @Test
    fun testNegativeDeltaProtection() {
        val lastPosition = 15.0
        val updatedAt = 1005000L
        val serverNow = 1000000L // clock slightly behind or skew

        val deltaSeconds = ((serverNow - updatedAt).coerceAtLeast(0L)) / 1000.0
        val targetPosition = lastPosition + deltaSeconds

        assertEquals(15.0, targetPosition, 0.001)
    }

    @Test
    fun testDriftThresholdDetection() {
        val expectedPosition = 45.0f

        // Within 1.5s tolerance: no reseek
        val smallDriftPosition = 44.2f
        val smallDrift = abs(smallDriftPosition - expectedPosition)
        assertFalse("Small drift (0.8s) should not trigger reseek", smallDrift > 1.5f)

        // Beyond 1.5s tolerance: triggers silent reseek
        val largeDriftPosition = 42.0f
        val largeDrift = abs(largeDriftPosition - expectedPosition)
        assertTrue("Large drift (3.0s) should trigger silent reseek", largeDrift > 1.5f)
    }
}
