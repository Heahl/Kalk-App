package com.example.kalkapp

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class KalkLogicScientificUnitTest {
    private lateinit var kalk: KalkLogic

    @Before
    fun setUp() {
        kalk = KalkLogic { }
    }

    @Test
    fun `test all scientific buttons with step asserts`() {
        // Test sin
        kalk.appendToInput("90")
        assertEquals("90", kalk.getCurrentInput())
        kalk.appendSin()
        assertEquals("sin(90)", kalk.getCurrentInput())
        kalk.evaluateAndShowResult()
        assertEquals("0.893996663601", kalk.getCurrentInput())

        kalk.clearDisplay()
        assertEquals("", kalk.getCurrentInput())

        // Test cos
        kalk.appendToInput("45")
        assertEquals("45", kalk.getCurrentInput())
        kalk.appendCos()
        assertEquals("cos(45)", kalk.getCurrentInput())
        kalk.evaluateAndShowResult()
        assertEquals("0.525321988818", kalk.getCurrentInput())

        kalk.clearDisplay()
        assertEquals("", kalk.getCurrentInput())

        // Test tan
        kalk.appendToInput("60")
        assertEquals("60", kalk.getCurrentInput())
        kalk.appendTan()
        assertEquals("tan(60)", kalk.getCurrentInput())
        kalk.evaluateAndShowResult()
        assertEquals("0.32004038938", kalk.getCurrentInput())

        kalk.clearDisplay()
        assertEquals("", kalk.getCurrentInput())

        // Test sqrt
        kalk.appendToInput("25")
        assertEquals("25", kalk.getCurrentInput())
        kalk.appendSqrt()
        assertEquals("sqrt(25)", kalk.getCurrentInput())
        kalk.evaluateAndShowResult()
        assertEquals("5", kalk.getCurrentInput())
    }
}
