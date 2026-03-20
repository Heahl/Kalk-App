package com.example.kalkapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalZeroDivisionException

/**
 * * Grundrechenarten (Addition, Subtraktion, Multiplikation, Division)
 * * mehr als zwei Operanden und mehr als einen Operator eingebbar
 * * Ergebnis der Berechnung für die nächste Berechnung verwenden können
 * * Fehlermeldung: "ERROR"
 * * Historienverwaltung
 *      - MR & MS mit zwei Speichernplätzen (Click und LongClick nutzen)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var currentInput = ""
    private val memorySlots = arrayOfNulls<String>(2) // zwei Speicherplätze
    private var currentMemoryIndex = 0 // auf welchen Speicherplatz wird als nächstes geschrieben

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById<TextView>(R.id.display)

        // --- OnClickListener für alle Btn registrieren --

        // Zahlen
        findViewById<Button>(R.id.btn_zero).setOnClickListener { appendToInput("0") }
        findViewById<Button>(R.id.btn_one).setOnClickListener { appendToInput("1") }
        findViewById<Button>(R.id.btn_two).setOnClickListener { appendToInput("2") }
        findViewById<Button>(R.id.btn_three).setOnClickListener { appendToInput("3") }
        findViewById<Button>(R.id.btn_four).setOnClickListener { appendToInput("4") }
        findViewById<Button>(R.id.btn_five).setOnClickListener { appendToInput("5") }
        findViewById<Button>(R.id.btn_six).setOnClickListener { appendToInput("6") }
        findViewById<Button>(R.id.btn_seven).setOnClickListener { appendToInput("7") }
        findViewById<Button>(R.id.btn_eight).setOnClickListener { appendToInput("8") }
        findViewById<Button>(R.id.btn_nine).setOnClickListener { appendToInput("9") }

        // Operatoren
        findViewById<Button>(R.id.btn_add).setOnClickListener { appendToInput("+") }
        findViewById<Button>(R.id.btn_subtract).setOnClickListener { appendToInput("-") }
        findViewById<Button>(R.id.btn_mult).setOnClickListener { appendToInput("*") }
        findViewById<Button>(R.id.btn_div).setOnClickListener { appendToInput("/") }
        findViewById<Button>(R.id.btn_decimal).setOnClickListener { appendToInput(".") }

        // Clear
        findViewById<Button>(R.id.btn_zero).setOnClickListener { clearDisplay() }

        // Equals
        findViewById<Button>(R.id.btn_equals).setOnClickListener { evaluateAndShowResult() }

        // Mem Save (MS)
        findViewById<Button>(R.id.btn_mem_save).setOnClickListener { saveToMemory() }

        // Mem Read (MR) - klick für [0], langer klick für [1]
        val btnMemRead = findViewById<Button>(R.id.btn_mem_read)
        btnMemRead.setOnClickListener { readFromMemory(0) }
        btnMemRead.setOnLongClickListener {
            readFromMemory(1)
            true
        }

    }

    private fun appendToInput(value: String){
        // Todo: zwei Operatoren hintereinander verbieten und anderes unlogisches Verhalten
        currentInput += value
        updateDisplay(currentInput)
    }

    private fun updateDisplay(text: String) {
        display.text = text
    }

    private fun clearDisplay() {
        currentInput = ""
        updateDisplay("0")
    }

    private fun evaluateAndShowResult() {
        if (currentInput.isBlank()) return

        try{
            // Keval für die Auswertung
            val result = Keval.eval(currentInput)
            // if result is decimal AND .0 -> omit digits after decimal (cast to int first)
            val resultString = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
            currentInput = resultString
            updateDisplay(resultString)
        } catch (e: KevalZeroDivisionException) {
            handleError()
        } catch (e: KevalInvalidExpressionException) {
            handleError()
        } catch (e: Exception) {
            // Rest abfangen
            handleError()
        }
    }

    private fun handleError() {
        updateDisplay("ERROR")
    }

    private fun saveToMemory() {
        if (currentInput.isNotBlank() && currentInput != "ERROR") {
            memorySlots[currentMemoryIndex] = currentInput
            // zum nächsten Index wechseln
            currentMemoryIndex = (currentMemoryIndex + 1) % 2
        }
    }

    private fun readFromMemory(slotIndex: Int) {
        val storedValue = memorySlots[slotIndex]
        if(!storedValue.isNullOrBlank()) {
            currentInput = storedValue
            updateDisplay(currentInput)
        }
    }
}
