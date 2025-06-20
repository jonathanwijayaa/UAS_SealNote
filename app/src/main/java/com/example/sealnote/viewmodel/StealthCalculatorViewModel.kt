package com.example.sealnote.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal

class StealthCalculatorViewModel : ViewModel() {

    // --- Triple Click Logic ---
    private var clickCount = 0
    // Sandi default. Nantinya bisa diubah dari halaman pengaturan.
    var targetButtonSymbolForTripleClick: String = "C"
        private set
    private val requiredClickCount: Int = 3

    private val resetTime = 1000L // Waktu dalam milidetik untuk mereset hitungan klik
    private val handler = Handler(Looper.getMainLooper())
    private val resetCounter = Runnable { clickCount = 0 }


    // --- Calculator State ---
    var displayText by mutableStateOf("0")
        private set

    private var currentExpression: String = ""
    private var lastInputWasOperator: Boolean = false
    private var lastInputWasEquals: Boolean = false

    var onCalculationFinished: ((expression: String, result: String) -> Unit)? = null


    fun onCalculatorButtonClick(symbol: String, onTripleClickAction: () -> Unit) {
        // Daftarkan setiap klik tombol untuk dideteksi sebagai triple-click
        registerButtonClickForTripleClick(symbol, onTripleClickAction)

        // Jalankan logika kalkulator
        when (symbol) {
            "C" -> clearAll()
            "+", "-", "×", "÷" -> handleOperator(symbol)
            "=" -> handleEquals()
            "," -> handleDecimal()
            else -> handleNumber(symbol) // Angka 0-9
        }
    }

    private fun registerButtonClickForTripleClick(buttonSymbol: String, onTripleClick: () -> Unit) {
        if (buttonSymbol == targetButtonSymbolForTripleClick) {
            clickCount++
            handler.removeCallbacks(resetCounter)
            handler.postDelayed(resetCounter, resetTime)

            if (clickCount >= requiredClickCount) {
                onTripleClick() // Panggil aksi navigasi
                clickCount = 0 // Reset hitungan
                handler.removeCallbacks(resetCounter)
            }
        } else {
            // Jika tombol lain ditekan, reset hitungan
            clickCount = 0
            handler.removeCallbacks(resetCounter)
        }
    }

    fun onBackspaceClick() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
            displayText = if (currentExpression.isEmpty()) "0" else currentExpression
            lastInputWasOperator = if (currentExpression.isNotEmpty()) isOperator(currentExpression.last().toString()) else false
            lastInputWasEquals = false
        }
    }

    private fun clearAll() {
        displayText = "0"
        currentExpression = ""
        lastInputWasOperator = false
        lastInputWasEquals = false
    }

    private fun handleNumber(numberChar: String) {
        if (lastInputWasEquals) {
            currentExpression = ""
            lastInputWasEquals = false
        }
        if (currentExpression == "0") {
            currentExpression = numberChar
        } else {
            currentExpression += numberChar
        }
        displayText = currentExpression
        lastInputWasOperator = false
    }

    private fun handleDecimal() {
        if (lastInputWasEquals) {
            currentExpression = "0,"
            lastInputWasEquals = false
        } else if (currentExpression.isEmpty() || lastInputWasOperator) {
            currentExpression += "0,"
        } else {
            val lastNumber = currentExpression.split(Regex("[+\\-×÷]")).last()
            if (!lastNumber.contains(',')) {
                currentExpression += ","
            }
        }
        displayText = currentExpression
        lastInputWasOperator = false
    }

    private fun handleOperator(newOperator: String) {
        if (currentExpression.isNotEmpty() && !lastInputWasOperator) {
            currentExpression += newOperator
            lastInputWasEquals = false
            lastInputWasOperator = true
            displayText = currentExpression
        }
    }

    private fun handleEquals() {
        if (currentExpression.isEmpty() || lastInputWasOperator) {
            return
        }

        val expressionToEvaluate = currentExpression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(",", ".")

        val originalExpressionForHistory = currentExpression

        try {
            val expression = ExpressionBuilder(expressionToEvaluate).build()
            val result = expression.evaluate()
            val formattedResult = formatResult(result.toBigDecimal())

            displayText = formattedResult
            onCalculationFinished?.invoke("$originalExpressionForHistory =", formattedResult)
            currentExpression = formattedResult.replace(",",".")
            lastInputWasEquals = true
            lastInputWasOperator = false
        } catch (e: Exception) {
            displayText = "Error"
            onCalculationFinished?.invoke("$originalExpressionForHistory =", "Error")
            currentExpression = ""
            lastInputWasEquals = false
            lastInputWasOperator = false
        }
    }

    private fun formatResult(result: BigDecimal): String {
        val formatted = result.stripTrailingZeros().toPlainString()
        return formatted.replace(".", ",")
    }

    private fun isOperator(symbol: String): Boolean {
        return symbol == "+" || symbol == "-" || symbol == "×" || symbol == "÷"
    }
}