package com.example.kalkapp

import com.notkamui.keval.Keval
import kotlin.math.*

/**
 * Diese Klasse kapselt die gesamte Logik des Taschenrechners.
 *
 * Sie verwaltet den aktuellen Eingabestatus ([currentInput]), den Zustand,
 * ob ein Ergebnis angezeigt wird ([isResultCurrentlyDisplayed]), die Speicherplätze
 * für Zwischenergebnisse ([memorySlots]) und kommuniziert Änderungen an die
 * Benutzeroberfläche über den [onUpdate]-Callback.
 *
 * Die mathematische Auswertung erfolgt über die externe Bibliothek Keval.
 * Die Klasse unterstützt grundlegende arithmetische Operationen, Klammern,
 * wissenschaftliche Funktionen (sin, cos, tan, sqrt) und eine rudimentäre
 * Speicherfunktion (MS, MR).
 *
 * @param onUpdate Ein Callback, das aufgerufen wird, wenn sich der anzuzeigende
 *                 Text ändert (Eingabe, Ergebnis, Fehler). Die Implementierung
 *                 dieser Funktion sollte die Benutzeroberfläche aktualisieren.
 */
class KalkLogic(private val onUpdate: (String) -> Unit) {
    private var currentInput = ""
    private val memorySlots = arrayOfNulls<String>(2) // zwei Speicherplätze
    private var currentMemoryIndex = 0 // auf welchen Speicherplatz wird als nächstes geschrieben
    private var isResultCurrentlyDisplayed = false

    private val keval = Keval.create()

    /**
     * Fügt dem aktuellen Eingabepuffer einen Wert (Zahl, Operator, Punkt) hinzu.
     *
     * Wenn aktuell ein Ergebnis angezeigt wird ([isResultCurrentlyDisplayed] = true)
     * und der hinzuzufügende Wert eine Zahl oder ein Punkt ist, wird das Ergebnis
     * durch den neuen Wert ersetzt. Andernfalls wird der Wert an das Ende
     * des aktuellen Puffers angehängt. Der Zustand [isResultCurrentlyDisplayed]
     * wird auf `false` gesetzt, sobald eine Eingabe erfolgt.
     *
     * @param value Der Wert (z.B. "5", "+", ".") der hinzugefügt werden soll.
     * @return Der aktualisierte Wert von [currentInput].
     */
    fun appendToInput(value: String): String {
        val isDigitOrDot = value.matches(Regex("[0-9.]"))

        if (isResultCurrentlyDisplayed) {
            if (isDigitOrDot) {
                // Wenn ein Ergebnis da steht und eine Zahl gedrückt wird -> neu anfangen
                currentInput = value
            } else {
                // Wenn ein Operator gedrückt wird oder wir im normalen Modus sind -> anhängen
                currentInput += value
            }
            isResultCurrentlyDisplayed = false
        } else {
            currentInput += value
        }

        onUpdate(currentInput)
        return currentInput
    }

    /**
     * Löscht den gesamten Inhalt des Eingabepuffers und setzt den Anzeigewert auf "0".
     *
     * Der Zustand [isResultCurrentlyDisplayed] wird auf `false` gesetzt.
     *
     * @return Der neue Anzeigewert, immer "0".
     */
    fun clearDisplay(): String {
        currentInput = ""
        isResultCurrentlyDisplayed = false
        onUpdate("0")
        return "0"
    }

    /**
     * Löscht das letzte Zeichen aus dem aktuellen Eingabepuffer.
     *
     * Dies ist nur möglich, wenn aktuell *kein* Ergebnis angezeigt wird
     * ([isResultCurrentlyDisplayed] = false) und der Puffer nicht leer ist.
     * Wenn der Puffer nach dem Löschen leer ist, wird stattdessen "0" angezeigt.
     *
     * @return Der aktualisierte Wert von [currentInput].
     */
    fun clearEntry(): String {
        if(!isResultCurrentlyDisplayed && currentInput.isNotBlank()) {
            currentInput = currentInput.dropLast(1)
        }
        val displayText = currentInput.ifEmpty() {"0"}
        onUpdate(displayText)
        return currentInput
    }

    /**
     * Evaluiert den aktuellen Eingabepuffer ([currentInput]) mithilfe von Keval
     * und zeigt das Ergebnis an.
     *
     * Bei leerem Eingabepuffer wird "0" angezeigt.
     * Bei erfolgreichem Auswerten wird das Ergebnis formatiert (siehe [formatResult]),
     * in [currentInput] gespeichert, [isResultCurrentlyDisplayed] auf `true` gesetzt
     * und der Zustand aktualisiert.
     * Bei einem Fehler (z.B. Division durch 0, ungültiger Ausdruck) wird der Zustand
     * auf einen Fehlerzustand gesetzt (siehe [handleError]).
     *
     * @return Das berechnete Ergebnis als String oder "ERROR" bei Misserfolg.
     */
    fun evaluateAndShowResult(): String {
        if (currentInput.isBlank()) {
            isResultCurrentlyDisplayed = true
            onUpdate("0")
            return "0"
        }

        println("Evaluating expression: $currentInput")
        try {
            val result = keval.eval(currentInput)
            println("Result (number): $result")

            val resultString = formatResult(result)
            println("ResultString: $resultString")

            currentInput = resultString
            isResultCurrentlyDisplayed = true
            onUpdate(currentInput)
            return resultString
        } catch (e: Exception) {
            println("Eval Error: ${e.message}")
            handleError()
            return "ERROR"
        }
    }

    /**
     * Formatiert das von Keval berechnete Ergebnis.
     *
     * Rundet auf 12 Nachkommastellen, um Gleitkommawerte zu stabilisieren.
     * Ganzzahlige Ergebnisse werden als Integer dargestellt.
     *
     * @param result Das Roh-Ergebnis von Keval als Double.
     * @return Der formatierte String des Ergebnisses.
     */
    private fun formatResult(result: Double): String {
        if (!result.isFinite()) return result.toString()

        val factor = 10.0.pow(12)
        val rounded = round(result * factor) / factor

        return if (rounded % 1 == 0.0 && rounded <= Long.MAX_VALUE && rounded >= Long.MIN_VALUE) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    /**
     * Setzt den internen Zustand auf einen Fehlerzustand.
     *
     * Der [currentInput] wird geleert, [isResultCurrentlyDisplayed] auf `true` gesetzt
     * und "ERROR" wird über den [onUpdate]-Callback an die UI gesendet.
     */
    private fun handleError() {
        currentInput = ""
        // "ERROR" ist ein Ergebnis!
        isResultCurrentlyDisplayed = true
        onUpdate("ERROR")
    }

    /**
     * Speichert den aktuellen Inhalt des Eingabepuffers ([currentInput])
     * in einen der beiden verfügbaren Speicherplätze.
     *
     * Der Speicherplatz wird zyklisch ausgewählt. Der Inhalt wird nur gespeichert,
     * wenn [currentInput] nicht leer oder "ERROR" ist.
     */
    fun saveToMemory() {
        if (currentInput.isNotBlank() && currentInput != "ERROR") {
            memorySlots[currentMemoryIndex] = currentInput
            currentMemoryIndex = (currentMemoryIndex + 1) % 2
        }
    }

    /**
     * Liest den Inhalt eines bestimmten Speicherplatzes.
     *
     * @param slotIndex Der Index des Speicherplatzes (0 oder 1).
     * @return Der Inhalt des Speicherplatzes oder `null`, falls leer oder Index ungültig.
     */
    fun getFromMemory(slotIndex: Int): String? {
        return memorySlots[slotIndex]
    }

    /**
     * Lädt den Inhalt eines bestimmten Speicherplatzes in den aktuellen
     * Eingabepuffer ([currentInput]).
     *
     * Der Inhalt des Speicherplatzes ersetzt den aktuellen Inhalt.
     * [isResultCurrentlyDisplayed] wird auf `true` gesetzt, da ein Wert
     * geladen wird.
     *
     * @param slotIndex Der Index des Speicherplatzes (0 oder 1), aus dem geladen werden soll.
     * @return Der neue Wert von [currentInput].
     */
    fun loadValueFromMemory(slotIndex: Int): String {
        val storedValue = getFromMemory(slotIndex)
        if (!storedValue.isNullOrBlank()) {
            currentInput = storedValue
            isResultCurrentlyDisplayed = true
            onUpdate(currentInput)
        }
        return currentInput
    }

    /**
     * Wechselt das Vorzeichen des aktuellen Eingabepuffers oder des letzten
     * gefundenen Operanden im Puffer.
     *
     * * Wenn ein Ergebnis angezeigt wird ([isResultCurrentlyDisplayed] = true),
     *   wird das Vorzeichen des Ergebniswerts geändert.
     * * Wenn kein Ergebnis angezeigt wird, wird das Vorzeichen des *letzten*
     *   gefundenen numerischen Terms (Zahl, mit/ohne Vorzeichen) geändert.
     *
     * @return Der aktualisierte Wert von [currentInput].
     */
    fun toggleSign(): String {
        if (isResultCurrentlyDisplayed && currentInput.isNotBlank() && currentInput != "ERROR") {
            try {
                val currentValue = currentInput.toDouble()
                val newValue = -currentValue
                val newValueString = formatResult(newValue)
                currentInput = newValueString
                onUpdate(currentInput)
                return currentInput
            } catch (e: NumberFormatException) {
                return currentInput
            }
        }

        var index = currentInput.length-1
        while (index >= 0 && (currentInput[index].isDigit() || currentInput[index] == '.')) {
            index--
        }
        if (index >= 0 && currentInput[index] == '-') {
            if (index == 0 || "+-*/(^".contains(currentInput[index-1])) {
                index--
            }
        }
        val numberStartIndex = index + 1

        if (numberStartIndex < currentInput.length) {
            val numberPart = currentInput.substring(numberStartIndex)
            val beforeNumberPart = currentInput.substring(0, numberStartIndex)

            val newNumberPart = if (numberPart.startsWith("-")) {
                numberPart.substring(1)
            } else {
                "-$numberPart"
            }
            currentInput = beforeNumberPart + newNumberPart
        }
        onUpdate(currentInput)
        return currentInput
    }

    /**
     * Fügt die Funktion `sin()` um den letzten gefundenen Term oder das aktuelle
     * Ergebnis herum ein.
     *
     * *   Wenn ein Ergebnis angezeigt wird, wird `sin(Ergebnis)` gebildet.
     * *   Andernfalls wird der letzte Term im aktuellen Puffer gefunden und
     *     `sin(Term)` gebildet.
     * *   Wenn weder ein Ergebnis angezeigt wird noch ein Term gefunden werden kann,
     *     wird `sin(` angehängt.
     *
     * @see findLastTerm
     */
    fun appendSin() { appendScientificFunction("sin")}

    /**
     * Fügt die Funktion `cos()` um den letzten gefundenen Term oder das aktuelle
     * Ergebnis herum ein.
     *
     * *   Wenn ein Ergebnis angezeigt wird, wird `cos(Ergebnis)` gebildet.
     * *   Andernfalls wird der letzte Term im aktuellen Puffer gefunden und
     *     `cos(Term)` gebildet.
     * *   Wenn weder ein Ergebnis angezeigt wird noch ein Term gefunden werden kann,
     *     wird `cos(` angehängt.
     *
     * @see findLastTerm
     */
    fun appendCos() { appendScientificFunction("cos")}

    /**
     * Fügt die Funktion `tan()` um den letzten gefundenen Term oder das aktuelle
     * Ergebnis herum ein.
     *
     * *   Wenn ein Ergebnis angezeigt wird, wird `tan(Ergebnis)` gebildet.
     * *   Andernfalls wird der letzte Term im aktuellen Puffer gefunden und
     *     `tan(Term)` gebildet.
     * *   Wenn weder ein Ergebnis angezeigt wird noch ein Term gefunden werden kann,
     *     wird `tan(` angehängt.
     *
     * @see findLastTerm
     */
    fun appendTan() { appendScientificFunction("tan")}

    /**
     * Fügt die Funktion `sqrt()` um den letzten gefundenen Term oder das aktuelle
     * Ergebnis herum ein.
     *
     * *   Wenn ein Ergebnis angezeigt wird, wird `sqrt(Ergebnis)` gebildet.
     * *   Andernfalls wird der letzte Term im aktuellen Puffer gefunden und
     *     `sqrt(Term)` gebildet.
     * *   Wenn weder ein Ergebnis angezeigt wird noch ein Term gefunden werden kann,
     *     wird `sqrt(` angehängt.
     *
     * @see findLastTerm
     */
    fun appendSqrt() { appendScientificFunction("sqrt")}

    /**
     * Schaltet zwischen dem Hinzufügen einer öffnenden '(' und einer
     * schließenden ')' Klammer um.
     *
     * *   Wenn ein Ergebnis angezeigt wird, wird `(` an den Anfang des Ergebnisses
     *     angehängt.
     * *   Wenn der Puffer leer ist, wird `(` hinzugefügt.
     * *   Andernfalls wird gezählt, wie viele Klammern geöffnet und geschlossen sind.
     *     *   Wenn mehr Klammern geöffnet sind als geschlossen, wird `)` hinzugefügt.
     *     *   Andernfalls wird `(` hinzugefügt.
     *
     * Diese Implementierung berücksichtigt *nicht* geschachtelte Klammern innerhalb
     * des *aktuellsten* Terms bei der Entscheidung, sondern zählt global.
     */
    fun toggleParentheses() {
        if (isResultCurrentlyDisplayed) {
            currentInput = "($currentInput"
            isResultCurrentlyDisplayed = false
            onUpdate(currentInput)
            return
        }

        if (currentInput.isEmpty()) {
            currentInput = "("
            onUpdate(currentInput)
            return
        }

        // Start des Terms finden
        var i = currentInput.length - 1
        while (i >= 0 && !"+-*/(^".contains(currentInput[i])) {
            i--
        }
        // Falls Klammer gefunden, oder start -> substring
        val termStart = i + 1
        val currentTerm = currentInput.substring(termStart)

        // nicht geschlossene Klammern zählen (global)
        val openCount = currentInput.count { it == '(' }
        val closeCount = currentInput.count { it == ')' }

        if (openCount > closeCount) {
            currentInput += ")"
        } else {
            currentInput += "("
        }
        onUpdate(currentInput)
    }

    // Helper ---------------------------------------------

    /**
     * Hilfsmethode zum Hinzufügen einer wissenschaftlichen Funktion.
     *
     * Implementiert die Logik für [appendSin], [appendCos], [appendTan], [appendSqrt].
     *
     * @param functionName Der Name der Funktion (z.B. "sin").
     */
    private fun appendScientificFunction(functionName: String) {
        if (currentInput == "ERROR") {
            currentInput = "$functionName("
            isResultCurrentlyDisplayed = false
            onUpdate(currentInput)
            return
        }

        val (startIndex, endIndex, lastTerm) = findLastTerm(currentInput)
        if (startIndex != -1 && endIndex != -1 && lastTerm.isNotBlank() && !isResultCurrentlyDisplayed) {
            val prefix = currentInput.substring(0, startIndex)
            val suffix = currentInput.substring(endIndex)
            currentInput = "$prefix$functionName($lastTerm)$suffix"
        } else if (isResultCurrentlyDisplayed && currentInput.isNotBlank()) {
            currentInput = "$functionName($currentInput)"
        } else {
            currentInput += "$functionName("
        }
        isResultCurrentlyDisplayed = false
        onUpdate(currentInput)
    }

    /**
     * Findet den letzten Term (Zahl, Klammerausdruck) im gegebenen Eingabestring.
     *
     * Unterstützt Zahlen (auch negative) und einfache Klammerausdrücke.
     * Funktionen wie `sin(...)` werden *nicht* als einzelne Terme erkannt,
     * sondern als `(...)` (z.B. bei `sin(30)` wird `(30)` erkannt).
     *
     * @param input Der Eingabestring, z.B. "2+3" oder "sqrt(4)".
     * @return Ein [Triple] mit (Startindex des Terms, Endindex (exklusiv), Term als String).
     *         Gibt `Triple(-1, -1, "")` zurück, wenn kein gültiger Term gefunden wird
     *         oder der     */
    private fun findLastTerm(input: String): Triple<Int, Int, String> {
        if (input.isEmpty()) return Triple(-1, -1, "")

        val index = input.length - 1
        val lastChar = input[index]

        return when {
            lastChar == ')' -> {
                var parenCount = 1
                var i = index - 1
                while (i >= 0 && parenCount > 0) {
                    if (input[i] == ')') parenCount++
                    else if (input[i] == '(') parenCount--
                    i--
                }
                if (parenCount == 0) {
                    val startIndex = i + 1
                    var funcStart = startIndex - 1
                    while (funcStart >= 0 && input[funcStart].isLetter()) {
                        funcStart--
                    }
                    val finalStart = funcStart + 1
                    Triple(finalStart, index + 1, input.substring(finalStart, index + 1))
                } else {
                    Triple(-1, -1, "")
                }
            }
            lastChar.isDigit() || lastChar == '.' -> {
                var i = index
                while (i >= 0 && (input[i].isDigit() || input[i] == '.')) {
                    i--
                }
                if (i >= 0 && input[i] == '-') {
                    if (i == 0 || "+-*/(^".contains(input[i-1])) {
                        i--
                    }
                }
                val startIndex = i + 1
                Triple(startIndex, index + 1, input.substring(startIndex, index + 1))
            }
            else -> Triple(-1, -1, "")
        }
    }

    // Für Tests ---------------------------------------------

    /**
     * Gibt den internen Zustand des aktuellen Eingabepuffers zurück.
     *
     * Diese Methode ist primär für Unit-Tests gedacht.
     *
     * @return Der aktuelle Wert von [currentInput].
     */
    fun getCurrentInput(): String {
        return currentInput
    }

    /**
     * Gibt den Inhalt der internen Speicherplätze zurück.
     *
     * Diese Methode ist primär für Unit-Tests gedacht.
     *
     * @return Ein Array mit den Inhalten der Speicherplätze.
     */
    fun getMemSlots(): Array<String?> {
        return memorySlots
    }

    /**
     * Gibt an, ob aktuell ein Ergebnis angezeigt wird.
     *
     * Diese Methode ist primär für Unit-Tests gedacht.
     *
     * @return `true`, wenn ein Ergebnis oder ein Fehler angezeigt wird, andernfalls `false`.
     */
    fun isResultDisplayed(): Boolean {
        return isResultCurrentlyDisplayed
    }
}