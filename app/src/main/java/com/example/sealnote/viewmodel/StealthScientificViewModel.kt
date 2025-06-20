package com.example.sealnote.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.math.BigInteger
import kotlin.math.*
import android.os.Handler
import android.os.Looper

class StealthScientificViewModel : ViewModel() {

    // --- Kalkulator Logic ---
    var displayMode by mutableStateOf("Deg")
        private set
    var mainDisplay by mutableStateOf("0")
        private set

    private var currentNumber: BigDecimal = BigDecimal.ZERO
    private var operator: String? = null
    private var awaitingNewNumber: Boolean = true
    private var calculationPerformed: Boolean = false

    var onCalculationFinished: ((expression: String, result: String) -> Unit)? = null

    private var currentExpressionString: StringBuilder = StringBuilder()

    fun onScientificButtonClick(symbol: String, onTripleClick: () -> Unit) { // Tambahkan onTripleClick
        when (symbol) {
            "Rad", "Deg" -> toggleMode(symbol)
            "C" -> clearAll()
            "⌫" -> onBackspaceClick()
            "%", "√", "∛", "∜", "x!", "sin", "cos", "tan", "e", "EE",
            "ln", "log", "sinh", "cosh", "tanh", "sin⁻¹", "cos⁻¹", "tan⁻¹",
            "1/x", "x²", "x³", "π" -> handleFunction(symbol)
            "+", "-", "×", "÷" -> handleOperator(symbol)
            "=" -> handleEquals()
            "," -> handleDecimal()
            else -> handleNumber(symbol)
        }
        // Registrasikan setiap klik tombol untuk triple-click
        registerButtonClickForTripleClick(symbol, onTripleClick)
    }

    private fun toggleMode(mode: String) {
        displayMode = mode
    }

    private fun onBackspaceClick() {
        if (mainDisplay.length > 1) {
            mainDisplay = mainDisplay.dropLast(1)
            if (mainDisplay == "," || (mainDisplay.startsWith("0") && mainDisplay.length == 1 && mainDisplay != "0")) {
                mainDisplay = "0"
            }
        } else {
            mainDisplay = "0"
        }
        if (currentExpressionString.isNotEmpty()) {
            currentExpressionString.deleteCharAt(currentExpressionString.length - 1)
        }
        awaitingNewNumber = false
        calculationPerformed = false
    }

    private fun clearAll() {
        mainDisplay = "0"
        currentNumber = BigDecimal.ZERO
        operator = null
        awaitingNewNumber = true
        calculationPerformed = false
        currentExpressionString = StringBuilder()
        // Reset triple click counter saat "C" diklik
        handler.removeCallbacks(resetCounter)
        clickCount = 0
    }

    private fun handleNumber(numberChar: String) {
        if (awaitingNewNumber || calculationPerformed) {
            mainDisplay = numberChar
            currentExpressionString = StringBuilder(numberChar)
            awaitingNewNumber = false
            calculationPerformed = false
        } else {
            if (mainDisplay == "0" && numberChar == "0") return
            if (mainDisplay == "0" && numberChar != "0") {
                mainDisplay = numberChar
                currentExpressionString = StringBuilder(numberChar)
            } else {
                if (mainDisplay.length < 15) {
                    mainDisplay += numberChar
                    currentExpressionString.append(numberChar)
                }
            }
        }
    }

    private fun handleDecimal() {
        if (awaitingNewNumber || calculationPerformed) {
            mainDisplay = "0,"
            currentExpressionString = StringBuilder("0,")
            awaitingNewNumber = false
            calculationPerformed = false
        } else if (!mainDisplay.contains(",")) {
            mainDisplay += ","
            currentExpressionString.append(",")
        }
    }

    private fun handleOperator(newOperator: String) {
        if (currentExpressionString.isEmpty() && mainDisplay == "0" && newOperator == "-") {
            currentExpressionString.append("-")
            mainDisplay = "-"
            awaitingNewNumber = false
            return
        }

        val inputVal = try {
            mainDisplay.replace(",", ".").toBigDecimal()
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }

        if (operator == null || awaitingNewNumber || calculationPerformed) {
            currentNumber = inputVal
            currentExpressionString.append(newOperator)
        } else {
            val result = performCalculation(currentNumber, operator, inputVal)
            currentNumber = result
            mainDisplay = formatResult(result)
            currentExpressionString.append(newOperator)
        }
        operator = newOperator
        awaitingNewNumber = true
        calculationPerformed = false
    }

    private fun handleEquals() {
        val inputVal = try {
            mainDisplay.replace(",", ".").toBigDecimal()
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }

        val finalExpression = currentExpressionString.toString()

        if (operator != null && !awaitingNewNumber) {
            val result = performCalculation(currentNumber, operator, inputVal)
            mainDisplay = formatResult(result)
            currentNumber = result
            operator = null
            awaitingNewNumber = true
            calculationPerformed = true

            onCalculationFinished?.invoke(finalExpression + " = " + formatResult(inputVal), mainDisplay)
        } else if (calculationPerformed) {
            // Do nothing if equals is pressed repeatedly after a calculation
        } else {
            onCalculationFinished?.invoke(mainDisplay, mainDisplay)
        }
        currentExpressionString = StringBuilder(mainDisplay)
    }

    private fun performCalculation(num1: BigDecimal, op: String?, num2: BigDecimal): BigDecimal {
        val mc = MathContext(10, RoundingMode.HALF_UP)
        return when (op) {
            "+" -> num1.add(num2, mc)
            "-" -> num1.subtract(num2, mc)
            "×" -> num1.multiply(num2, mc)
            "÷" -> {
                if (num2 == BigDecimal.ZERO) {
                    throw ArithmeticException("Division by zero")
                }
                num1.divide(num2, mc)
            }
            else -> num2
        }
    }

    private fun handleFunction(symbol: String) {
        val currentValue = try {
            mainDisplay.replace(",", ".").toBigDecimal()
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }

        val originalExpressionPart = if (calculationPerformed) {
            mainDisplay
        } else {
            currentExpressionString.toString()
        }

        val result = when (symbol) {
            "%" -> currentValue.divide(BigDecimal(100), MathContext(10, RoundingMode.HALF_UP))
            "√" -> sqrt(currentValue)
            "∛" -> cbrt(currentValue)
            "∜" -> fourthRoot(currentValue)
            "x!" -> factorial(currentValue.toBigInteger())?.toBigDecimal() ?: BigDecimal.ZERO
            "sin" -> sinTrig(currentValue)
            "cos" -> cosTrig(currentValue)
            "tan" -> tanTrig(currentValue)
            "e" -> BigDecimal(Math.E, MathContext(10, RoundingMode.HALF_UP))
            "EE" -> {
                currentExpressionString.append("E+")
                mainDisplay += "E+"
                awaitingNewNumber = true
                return
            }
            "ln" -> ln(currentValue)
            "log" -> log10(currentValue)
            "sinh" -> sinh(currentValue)
            "cosh" -> cosh(currentValue)
            "tanh" -> tanh(currentValue)
            "sin⁻¹" -> asinTrig(currentValue)
            "cos⁻¹" -> acosTrig(currentValue)
            "tan⁻¹" -> atanTrig(currentValue)
            "1/x" -> if (currentValue != BigDecimal.ZERO) BigDecimal.ONE.divide(currentValue, MathContext(10, RoundingMode.HALF_UP)) else BigDecimal.ZERO
            "x²" -> currentValue.multiply(currentValue, MathContext(10, RoundingMode.HALF_UP))
            "x³" -> currentValue.multiply(currentValue, MathContext(10, RoundingMode.HALF_UP)).multiply(currentValue, MathContext(10, RoundingMode.HALF_UP))
            "π" -> BigDecimal(Math.PI, MathContext(10, RoundingMode.HALF_UP))
            else -> BigDecimal.ZERO
        }

        val formattedResult = formatResult(result ?: BigDecimal.ZERO)
        val fullExpression = when (symbol) {
            "EE" -> originalExpressionPart
            "x!", "x²", "x³", "1/x" -> "($originalExpressionPart)$symbol"
            "%" -> "($originalExpressionPart)$symbol"
            "π", "e" -> symbol
            else -> "$symbol($originalExpressionPart)"
        }
        onCalculationFinished?.invoke(fullExpression, formattedResult)

        mainDisplay = formattedResult
        currentExpressionString = StringBuilder(formattedResult)
        awaitingNewNumber = true
        calculationPerformed = true
        operator = null
    }

    private fun formatResult(result: BigDecimal): String {
        val formatted = result.stripTrailingZeros().toPlainString()
        val maxLength = 15
        return if (formatted.length > maxLength) {
            if (formatted.contains(".")) {
                val integerPart = formatted.substringBefore(".")
                if (integerPart.length >= maxLength) formatted.substring(0, minOf(maxLength, formatted.length)).replace(".", ",")
                else formatted.substring(0, minOf(maxLength, formatted.length)).replace(".", ",")
            } else {
                formatted.substring(0, minOf(maxLength, formatted.length)).replace(".", ",")
            }
        } else {
            formatted.replace(".", ",")
        }
    }

    // --- Scientific Function Implementations (Simplified) ---
    private fun sqrt(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO
        val result = value.toDouble().pow(0.5)
        return BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun cbrt(value: BigDecimal): BigDecimal {
        val result = value.toDouble().pow(1.0/3.0)
        return BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun fourthRoot(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO
        val result = value.toDouble().pow(1.0/4.0)
        return BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun factorial(n: BigInteger): BigInteger? {
        if (n.compareTo(BigInteger.ZERO) < 0) return null
        if (n.compareTo(BigInteger.ZERO) == 0 || n.compareTo(BigInteger.ONE) == 0) return BigInteger.ONE
        var result = BigInteger.ONE
        var i = BigInteger.TWO
        while (i.compareTo(n) <= 0) {
            result = result.multiply(i)
            i = i.add(BigInteger.ONE)
        }
        return result
    }

    private fun sinTrig(value: BigDecimal): BigDecimal {
        val angle = if (displayMode == "Deg") Math.toRadians(value.toDouble()) else value.toDouble()
        return BigDecimal(sin(angle), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun cosTrig(value: BigDecimal): BigDecimal {
        val angle = if (displayMode == "Deg") Math.toRadians(value.toDouble()) else value.toDouble()
        return BigDecimal(cos(angle), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun tanTrig(value: BigDecimal): BigDecimal {
        val angle = if (displayMode == "Deg") Math.toRadians(value.toDouble()) else value.toDouble()
        return BigDecimal(tan(angle), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun ln(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        return BigDecimal(ln(value.toDouble()), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun log10(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        return BigDecimal(log10(value.toDouble()), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun sinh(value: BigDecimal): BigDecimal {
        val x = value.toDouble()
        return BigDecimal((exp(x) - exp(-x)) / 2.0, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun cosh(value: BigDecimal): BigDecimal {
        val x = value.toDouble()
        return BigDecimal((exp(x) + exp(-x)) / 2.0, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun tanh(value: BigDecimal): BigDecimal {
        val x = value.toDouble()
        return BigDecimal((exp(2 * x) - 1) / (exp(2 * x) + 1), MathContext(10, RoundingMode.HALF_UP))
    }

    private fun asinTrig(value: BigDecimal): BigDecimal {
        if (value.abs().compareTo(BigDecimal.ONE) > 0) return BigDecimal.ZERO
        val result = asin(value.toDouble())
        return if (displayMode == "Deg") BigDecimal(Math.toDegrees(result), MathContext(10, RoundingMode.HALF_UP)) else BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun acosTrig(value: BigDecimal): BigDecimal {
        if (value.abs().compareTo(BigDecimal.ONE) > 0) return BigDecimal.ZERO
        val result = acos(value.toDouble())
        return if (displayMode == "Deg") BigDecimal(Math.toDegrees(result), MathContext(10, RoundingMode.HALF_UP)) else BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    private fun atanTrig(value: BigDecimal): BigDecimal {
        val result = atan(value.toDouble())
        return if (displayMode == "Deg") BigDecimal(Math.toDegrees(result), MathContext(10, RoundingMode.HALF_UP)) else BigDecimal(result, MathContext(10, RoundingMode.HALF_UP))
    }

    // --- Triple Click Logic ---
    private var clickCount = 0
    var targetButtonSymbolForTripleClick: String = "C" // Default target: "C"
        set(value) {
            field = value
            clickCount = 0 // Reset counter jika target diubah
            handler.removeCallbacks(resetCounter)
        }

    private val resetTime = 1000L // Reset dalam 1 detik (dipercepat sedikit)
    private val handler = Handler(Looper.getMainLooper())
    private val resetCounter = Runnable { clickCount = 0 }

    fun registerButtonClickForTripleClick(buttonSymbol: String, onTripleClick: () -> Unit) {
        if (buttonSymbol == targetButtonSymbolForTripleClick) {
            clickCount++
            handler.removeCallbacks(resetCounter)
            handler.postDelayed(resetCounter, resetTime)

            if (clickCount >= 3) {
                onTripleClick() // Panggil callback jika sudah 3 kali klik
                resetCounter.run() // Reset counter setelah menavigasi
            }
        } else {
            // Jika tombol yang berbeda diklik, reset counter
            clickCount = 0
            handler.removeCallbacks(resetCounter)
        }
    }
}