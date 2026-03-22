package com.example.kalkapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private lateinit var copyPopup: LinearLayout

    // Logik instanziieren mit Callback für UI-Updates
    private val kalkLogic = KalkLogic { text ->
        if (::display.isInitialized) {
            display.text = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)
        copyPopup = findViewById(R.id.copy_popup)

        display.setOnClickListener {
            if (kalkLogic.isResultDisplayed()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("KalkApp Ergebnis", kalkLogic.getCurrentInput())
                clipboard.setPrimaryClip(clip)
                
                showCopyPopup()
            }
        }

        // Numbers
        findViewById<Button>(R.id.btn_zero).setOnClickListener { kalkLogic.appendToInput("0") }
        findViewById<Button>(R.id.btn_one).setOnClickListener { kalkLogic.appendToInput("1") }
        findViewById<Button>(R.id.btn_two).setOnClickListener { kalkLogic.appendToInput("2") }
        findViewById<Button>(R.id.btn_three).setOnClickListener { kalkLogic.appendToInput("3") }
        findViewById<Button>(R.id.btn_four).setOnClickListener { kalkLogic.appendToInput("4") }
        findViewById<Button>(R.id.btn_five).setOnClickListener { kalkLogic.appendToInput("5") }
        findViewById<Button>(R.id.btn_six).setOnClickListener { kalkLogic.appendToInput("6") }
        findViewById<Button>(R.id.btn_seven).setOnClickListener { kalkLogic.appendToInput("7") }
        findViewById<Button>(R.id.btn_eight).setOnClickListener { kalkLogic.appendToInput("8") }
        findViewById<Button>(R.id.btn_nine).setOnClickListener { kalkLogic.appendToInput("9") }

        // Operators
        findViewById<Button>(R.id.btn_add).setOnClickListener { kalkLogic.appendToInput("+") }
        findViewById<Button>(R.id.btn_subtract).setOnClickListener { kalkLogic.appendToInput("-") }
        findViewById<Button>(R.id.btn_mult).setOnClickListener { kalkLogic.appendToInput("*") }
        findViewById<Button>(R.id.btn_div).setOnClickListener { kalkLogic.appendToInput("/") }
        findViewById<Button>(R.id.btn_decimal).setOnClickListener { kalkLogic.appendToInput(".") }

        // +/-
        findViewById<Button>(R.id.btn_sign_change).setOnClickListener { kalkLogic.toggleSign() }

        // Clear
        findViewById<Button>(R.id.btn_clear).setOnClickListener { kalkLogic.clearDisplay() }
        findViewById<Button>(R.id.btn_clear_entry).setOnClickListener { kalkLogic.clearEntry() }

        // ( )
        findViewById<Button>(R.id.btn_parentheses).setOnClickListener { kalkLogic.toggleParentheses() }

        // Equals
        findViewById<Button>(R.id.btn_equals).setOnClickListener { kalkLogic.evaluateAndShowResult() }

        // Mem Save (MS)
        findViewById<Button>(R.id.btn_mem_save).setOnClickListener { kalkLogic.saveToMemory() }

        // Mem Read (MR)
        val btnMemRead = findViewById<Button>(R.id.btn_mem_read)
        btnMemRead.setOnClickListener { kalkLogic.loadValueFromMemory(0) }
        btnMemRead.setOnLongClickListener {
            kalkLogic.loadValueFromMemory(1)
            true
        }

        // Tablet Btns
        findViewById<Button>(R.id.btn_sin)?.setOnClickListener { kalkLogic.appendSin() }
        findViewById<Button>(R.id.btn_cos)?.setOnClickListener { kalkLogic.appendCos() }
        findViewById<Button>(R.id.btn_tan)?.setOnClickListener { kalkLogic.appendTan() }
        findViewById<Button>(R.id.btn_sqrt)?.setOnClickListener { kalkLogic.appendSqrt() }
    }

    private fun showCopyPopup() {
        copyPopup.visibility = View.VISIBLE
        copyPopup.postDelayed({
            copyPopup.visibility = View.GONE
        }, 3000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calculator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_function_sin -> kalkLogic.appendSin()
            R.id.menu_function_cos -> kalkLogic.appendCos()
            R.id.menu_function_tan -> kalkLogic.appendTan()
            R.id.menu_function_sqrt -> kalkLogic.appendSqrt()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
