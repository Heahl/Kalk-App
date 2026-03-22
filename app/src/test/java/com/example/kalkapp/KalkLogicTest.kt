package com.example.kalkapp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

internal class KalkLogicTest {
    private lateinit var kalk: KalkLogic

    @Before
    fun setUp() {
        kalk = KalkLogic { }
    }

    @Test
    fun `Grundrechenarten Addition`() {
        kalk.appendToInput("2+3")
        val result = kalk.evaluateAndShowResult()
        assertEquals("5", result)
    }

    @Test
    fun `Grundrechenarten Subtraktion`(){
        kalk.appendToInput("5-3")
        val result = kalk.evaluateAndShowResult()
        assertEquals("2", result)
    }

    @Test
    fun `Grundrechenarten Multiplikation`(){
        kalk.appendToInput("4*3")
        val result = kalk.evaluateAndShowResult()
        assertEquals("12", result)
    }

    @Test
    fun `Grundrechenarten Division`() {
        kalk.appendToInput("15/3")
        val result = kalk.evaluateAndShowResult()
        assertEquals("5", result)
    }

    @Test
    fun `Mehrere Operanden und Operatoren`() {
        kalk.appendToInput("2+3*4-1")
        val result = kalk.evaluateAndShowResult()
        assertEquals("13", result)
    }

    @Test
    fun `Ergebnis für nächste Berechnung verwenden`(){
        kalk.appendToInput("5+3")
        kalk.evaluateAndShowResult() // "8"

        kalk.appendToInput("*2")
        val result = kalk.evaluateAndShowResult()
        assertEquals("16", result)
    }

    @Test
    fun `Zahleneingabe nach Ergebnis ersetzt altes Ergebnis`() {
        kalk.appendToInput("2+3")
        kalk.evaluateAndShowResult() // "5"
        
        kalk.appendToInput("9") // Tippe neue Zahl
        assertEquals("9", kalk.getCurrentInput())
    }

    @Test
    fun `Division durch Null führt zu ERROR`() {
        kalk.appendToInput("3/0")
        val result = kalk.evaluateAndShowResult()
        assertEquals("ERROR", result)
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `Ungültiger Ausdruck führt zu ERROR`() {
        kalk.appendToInput("((2+3)")
        val result = kalk.evaluateAndShowResult()
        assertEquals("ERROR", result)
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `Historieverwaltung Speicherplatz initial leer`() {
        assertNull(kalk.getFromMemory(0))
        assertNull(kalk.getFromMemory(1))
    }

    @Test
    fun `Historienverwaltung MS speichert aktuellen Wert`() {
        kalk.appendToInput("42")
        kalk.saveToMemory()
        assertArrayEquals(arrayOf("42", null), kalk.getMemSlots())

        // Neuer Input
        kalk.appendToInput("+6")
        kalk.evaluateAndShowResult() // "48"
        kalk.saveToMemory() // "48" in den nächsten freien Slot

        assertArrayEquals(arrayOf("42", "48"), kalk.getMemSlots())
    }

    @Test
    fun `Historienverwaltung MR liest Wert über Click (Slot 0)`() {
        kalk.appendToInput("42")
        kalk.saveToMemory() // speichert in [0]
        kalk.clearDisplay()
        assertEquals("", kalk.getCurrentInput())

        kalk.loadValueFromMemory(0)
        assertEquals("42", kalk.getCurrentInput())
    }

    @Test
    fun `Historienverwaltung MR liest Wert über LongClick (Slot 1)`() {
        kalk.appendToInput("42")
        kalk.saveToMemory()
        kalk.clearDisplay()

        kalk.appendToInput("58")
        kalk.saveToMemory()
        kalk.clearDisplay()
        assertEquals("", kalk.getCurrentInput())

        kalk.loadValueFromMemory(1)
        assertEquals("58", kalk.getCurrentInput())
    }

    @Test
    fun `Historienverwaltung MS überschreibt korrekt`() {
        kalk.appendToInput("7")
        kalk.saveToMemory() // ["7", null]
        kalk.clearDisplay()

        kalk.appendToInput("8")
        kalk.saveToMemory() // ["7", "8"]
        kalk.clearDisplay()

        kalk.appendToInput("9")
        kalk.saveToMemory() // ["9", "8"]

        assertArrayEquals(arrayOf("9", "8"), kalk.getMemSlots())
    }

    @Test
    fun `Historienverwaltung MR mit leerem Speicherplatz`() {
        kalk.loadValueFromMemory(0)
        assertEquals("", kalk.getCurrentInput())

        kalk.appendToInput("42")
        kalk.loadValueFromMemory(1) // 1 muss auch leer sein -> no change

        assertEquals("42", kalk.getCurrentInput())
    }

    @Test
    fun `clearEntry löscht letztes Zeichen wenn kein Ergebnis angezeigt wird`() {
        kalk.appendToInput("123")
        assertEquals("123", kalk.getCurrentInput())

        kalk.clearEntry() // "123" -> "12"
        assertEquals("12", kalk.getCurrentInput())

        kalk.clearEntry() // "12" -> "1"
        assertEquals("1", kalk.getCurrentInput())

        kalk.clearEntry() // "1" -> ""
        assertEquals("", kalk.getCurrentInput())

        kalk.clearEntry() // "" -> ""
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `clearEntry tut nichts wenn Ergebnis angezeigt wird`() {
        kalk.appendToInput("5+3")
        kalk.evaluateAndShowResult() // "8"
        assertEquals("8", kalk.getCurrentInput())

        kalk.clearEntry()
        assertEquals("8", kalk.getCurrentInput())
    }

    @Test
    fun `clearEntry tut nichts wenn Input leer ist`() {
        assertEquals("", kalk.getCurrentInput())

        kalk.clearEntry()
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `+ - kehrt Vorzeichen des Ergebnisses um`() {
        kalk.appendToInput("5+3")
        kalk.evaluateAndShowResult()
        assertEquals("8", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("-8", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("8", kalk.getCurrentInput())
    }

    @Test
    fun `+ - kehrt Vorzeichen des aktuellen Operanden um, wenn kein Ergebnis angezeigt`() {
        kalk.appendToInput("5")
        assertEquals("5", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("-5", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("5", kalk.getCurrentInput())
    }

    @Test
    fun `+ - kehrt Vorzeichen bei Dezimalzahl um`() {
        kalk.appendToInput("3.14")
        assertEquals("3.14", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("-3.14", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("3.14", kalk.getCurrentInput())
    }

    @Test
    fun `+ - tut nicht bei leerem Input`() {
        assertEquals("", kalk.getCurrentInput())

        kalk.toggleSign()
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `+ - tut nichts bei ERROR`() {
        kalk.appendToInput("2/0")
        kalk.evaluateAndShowResult() // -> "ERROR"
        kalk.toggleSign()
        assertEquals("", kalk.getCurrentInput())
    }

    @Test
    fun `appendSin fügt sin() hinzu`() {
        kalk.appendToInput("90")
        assertEquals("90", kalk.getCurrentInput())

        kalk.appendSin()
        assertEquals("sin(90)", kalk.getCurrentInput())
    }

    @Test
    fun `appendCos fügt cos() hinzu`() {
        kalk.appendToInput("45")
        assertEquals("45", kalk.getCurrentInput())

        kalk.appendCos()
        assertEquals("cos(45)", kalk.getCurrentInput())
    }

    @Test
    fun `appendTan füg tan() hinzu`() {
        kalk.appendToInput("42")
        assertEquals("42", kalk.getCurrentInput())

        kalk.appendTan()
        assertEquals("tan(42)", kalk.getCurrentInput())
    }

    @Test
    fun `appendSqrt fügt sqrt() hinzu`() {
        kalk.appendToInput("25")
        assertEquals("25", kalk.getCurrentInput())

        kalk.appendSqrt()
        assertEquals("sqrt(25)",kalk.getCurrentInput())
    }


    @Test
    fun `appendSin fuegt hinzu wenn Ergebnis angezeigt wird`() {
        kalk.appendToInput("30+45")
        kalk.evaluateAndShowResult() // currentInput = "75", isResult = true
        assertEquals("75", kalk.getCurrentInput())

        kalk.appendSin()
        assertEquals("sin(75)", kalk.getCurrentInput())
    }

    @Test
    fun `appendCos fuegt hinzu wenn Ergebnis angezeigt wird`() {
        kalk.appendToInput("60*2")
        kalk.evaluateAndShowResult() // currentInput = "120", isResult = true
        assertEquals("120", kalk.getCurrentInput())

        kalk.appendCos()
        assertEquals("cos(120)", kalk.getCurrentInput())
    }

    @Test
    fun `appendTan fuegt hinzu wenn Ergebnis angezeigt wird`() {
        kalk.appendToInput("180/4")
        kalk.evaluateAndShowResult() // currentInput = "45", isResult = true
        assertEquals("45", kalk.getCurrentInput())

        kalk.appendTan()
        assertEquals("tan(45)", kalk.getCurrentInput())
    }

    @Test
    fun `appendSqrt fuegt hinzu wenn Ergebnis angezeigt wird`() {
        kalk.appendToInput("100")
        kalk.evaluateAndShowResult() // currentInput = "100", isResult = true
        assertEquals("100", kalk.getCurrentInput())

        kalk.appendSqrt()
        assertEquals("sqrt(100)", kalk.getCurrentInput())
    }

    @Test
    fun `sqrt funktioniert`() {
        kalk.appendToInput("25")
        kalk.appendSqrt()

        kalk.evaluateAndShowResult()
        assertEquals("5", kalk.getCurrentInput())
    }

    @Test
    fun `sin funktioniert`() {
        kalk.appendToInput("90")
        kalk.appendSin()

        kalk.evaluateAndShowResult()
        assertEquals("0.893996663601", kalk.getCurrentInput())
    }

    @Test
    fun `cos funktioniert`() {
        kalk.appendToInput("45")
        kalk.appendCos()

        kalk.evaluateAndShowResult()
        assertEquals("0.525321988818", kalk.getCurrentInput())
    }

    @Test
    fun `tan funktioniert`() {
        kalk.appendToInput("60")
        kalk.appendTan()

        kalk.evaluateAndShowResult()
        assertEquals("0.32004038938", kalk.getCurrentInput())
    }
}

