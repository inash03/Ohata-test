package com.example.calculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var displayText: TextView
    private lateinit var expressionText: TextView

    private var currentInput = ""
    private var operator = ""
    private var firstOperand = 0.0
    private var isNewInput = false
    private var hasResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayText = findViewById(R.id.displayText)
        expressionText = findViewById(R.id.expressionText)

        setupButtons()
    }

    private fun setupButtons() {
        val numberMap = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        numberMap.forEach { (id, num) ->
            findViewById<Button>(id).setOnClickListener { haptic(it); onNumberClick(num) }
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener     { haptic(it); onOperatorClick("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener    { haptic(it); onOperatorClick("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { haptic(it); onOperatorClick("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener   { haptic(it); onOperatorClick("÷") }

        findViewById<Button>(R.id.btnEquals).setOnClickListener   { haptic(it); onEqualsClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener    { haptic(it); onClearClick() }
        findViewById<Button>(R.id.btnDecimal).setOnClickListener  { haptic(it); onDecimalClick() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener{ haptic(it); onBackspaceClick() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener{ haptic(it); onPlusMinusClick() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener  { haptic(it); onPercentClick() }
    }

    private fun haptic(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun onNumberClick(number: String) {
        if (hasResult && !isNewInput) {
            // Start fresh after a result unless continuing with operator
            currentInput = ""
            hasResult = false
        }
        if (isNewInput) {
            currentInput = if (number == "0") "0" else number
            isNewInput = false
        } else {
            currentInput = if (currentInput == "0") number else currentInput + number
        }
        displayText.text = currentInput
    }

    private fun onOperatorClick(op: String) {
        hasResult = false
        if (currentInput.isEmpty() && operator.isEmpty()) return

        // Chain operators: calculate first if mid-expression
        if (currentInput.isNotEmpty() && operator.isNotEmpty() && !isNewInput) {
            calculate()
        }

        val operand = currentInput.toDoubleOrNull()
        if (operand != null) {
            firstOperand = operand
        }
        operator = op
        expressionText.text = formatNumber(firstOperand) + " $op"
        isNewInput = true
    }

    private fun onEqualsClick() {
        if (operator.isEmpty() || isNewInput) return
        val expression = "${formatNumber(firstOperand)} $operator $currentInput"
        calculate()
        expressionText.text = "$expression ="
        operator = ""
        hasResult = true
    }

    private fun calculate() {
        val second = currentInput.toDoubleOrNull() ?: return
        val result = when (operator) {
            "+" -> firstOperand + second
            "-" -> firstOperand - second
            "×" -> firstOperand * second
            "÷" -> if (second != 0.0) firstOperand / second else Double.NaN
            else -> return
        }

        if (result.isNaN() || result.isInfinite()) {
            displayText.text = getString(R.string.error)
            currentInput = ""
            firstOperand = 0.0
            isNewInput = true
            return
        }

        currentInput = formatNumber(result)
        displayText.text = currentInput
        firstOperand = result
        isNewInput = true
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toBigDecimal().stripTrailingZeros().toPlainString()
        }
    }

    private fun onClearClick() {
        currentInput = ""
        operator = ""
        firstOperand = 0.0
        isNewInput = false
        hasResult = false
        displayText.text = "0"
        expressionText.text = ""
    }

    private fun onDecimalClick() {
        hasResult = false
        if (isNewInput) {
            currentInput = "0."
            isNewInput = false
        } else if (!currentInput.contains(".")) {
            currentInput = if (currentInput.isEmpty()) "0." else "$currentInput."
        }
        displayText.text = currentInput
    }

    private fun onBackspaceClick() {
        if (isNewInput || currentInput.isEmpty()) return
        currentInput = currentInput.dropLast(1)
        displayText.text = if (currentInput.isEmpty() || currentInput == "-") "0".also { currentInput = "" } else currentInput
    }

    private fun onPlusMinusClick() {
        if (currentInput.isEmpty() || currentInput == "0") return
        currentInput = if (currentInput.startsWith("-")) {
            currentInput.substring(1)
        } else {
            "-$currentInput"
        }
        displayText.text = currentInput
    }

    private fun onPercentClick() {
        val value = currentInput.toDoubleOrNull() ?: return
        val result = value / 100.0
        currentInput = formatNumber(result)
        displayText.text = currentInput
    }
}
