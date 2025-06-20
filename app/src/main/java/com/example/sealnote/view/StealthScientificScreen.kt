package com.example.sealnote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// --- START: Impor warna kustom dari Color.kt ---
import com.example.sealnote.ui.theme.SciCalcScreenBackground
import com.example.sealnote.ui.theme.SciCalcDisplayCardBackground
import com.example.sealnote.ui.theme.SciCalcDisplayModeColor
import com.example.sealnote.ui.theme.SciCalcDisplayResultColor
import com.example.sealnote.ui.theme.SciCalcDividerColor
import com.example.sealnote.ui.theme.SciCalcScientificButtonBg
import com.example.sealnote.ui.theme.SciCalcScientificButtonTextColor
import com.example.sealnote.ui.theme.SciCalcBasicNumberButtonBg
import com.example.sealnote.ui.theme.SciCalcBasicFunctionButtonBg
import com.example.sealnote.ui.theme.SciCalcBasicOperatorGradientStart
import com.example.sealnote.ui.theme.SciCalcBasicOperatorGradientEnd
import com.example.sealnote.ui.theme.SciCalcBasicButtonTextColor
// --- END: Impor warna kustom ---

import com.example.sealnote.viewmodel.StealthScientificViewModel
import com.example.sealnote.viewmodel.CalculatorHistoryViewModel

// --- Data Class dan Enum untuk Tombol ---
private data class SciCalcButtonUIData(
    val symbol: String,
    val description: String,
    val textSize: TextUnit,
    val type: SciButtonType,
    val icon: ImageVector? = null
)

private enum class SciButtonType {
    SCIENTIFIC,
    BASIC_NUMBER,
    BASIC_FUNCTION,
    BASIC_OPERATOR,
    BASIC_CLEAR,
    BASIC_DECIMAL
}

// --- Composable Utama ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StealthScientificScreen(
    navController: NavHostController,
    onNavigateToLogin: () -> Unit, // Tambahkan callback untuk navigasi Login
    viewModel: StealthScientificViewModel = viewModel(),
    historyViewModel: CalculatorHistoryViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.onCalculationFinished = { expression, result ->
            historyViewModel.addHistoryEntry(expression, result)
        }
        // Atur tombol target untuk triple-click
        viewModel.targetButtonSymbolForTripleClick = "C" // Atur ke "C" atau tombol lain yang diinginkan
        onDispose {
            viewModel.onCalculationFinished = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("Mode Kalkulator", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                NavigationDrawerItem(
                    label = { Text("Kalkulator Standar") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthCalculator") {
                            popUpTo("stealthScientific") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Kalkulator Ilmiah") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Riwayat Kalkulasi") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthHistory") {
                            popUpTo("stealthScientific") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Scientific Calculator",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SciCalcScreenBackground,
                        titleContentColor = SciCalcDisplayResultColor
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ScientificCalculatorDisplay(
                    mode = viewModel.displayMode,
                    result = viewModel.mainDisplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )

                ScientificFunctionsGrid(
                    onButtonClick = { symbol -> viewModel.onScientificButtonClick(symbol, onNavigateToLogin) }, // Teruskan onNavigateToLogin
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )

                BasicCalculatorPad(
                    onButtonClick = { symbol -> viewModel.onScientificButtonClick(symbol, onNavigateToLogin) }, // Teruskan onNavigateToLogin
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// ... (ScientificCalculatorDisplay, ScientificFunctionsGrid, BasicCalculatorPad, SciCalcActualButton tetap sama)

@Composable
private fun ScientificCalculatorDisplay(
    mode: String,
    result: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SciCalcDisplayCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode,
                    color = SciCalcDisplayModeColor,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = result,
                    color = SciCalcDisplayResultColor,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider(
                color = SciCalcDividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ScientificFunctionsGrid(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scientificButtonsConfig = listOf(
        SciCalcButtonUIData("/", "Divide (fraction part)", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("√", "Square Root", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("∛", "Cube Root", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("∜", "Fourth Root", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("ln", "Natural Log", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("log", "Logarithm base 10", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("x!", "Factorial", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("sin", "Sine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("cos", "Cosine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("tan", "Tangent", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("e", "Euler's Number", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("EE", "Exponent", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("Rad", "Radians Mode", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("sinh", "Hyperbolic Sine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("cosh", "Hyperbolic Cosine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("tanh", "Hyperbolic Tangent", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("sin⁻¹", "Arc Sine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("cos⁻¹", "Arc Cosine", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("tan⁻¹", "Arc Tangent", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("1/x", "Reciprocal", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("x²", "Square", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("x³", "Cube", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("π", "Pi", 14.sp, SciButtonType.SCIENTIFIC),
        SciCalcButtonUIData("Deg", "Degrees Mode", 14.sp, SciButtonType.SCIENTIFIC)
    )

    Column(modifier = modifier) {
        scientificButtonsConfig.chunked(6).forEach { rowButtons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                rowButtons.forEach { buttonInfo ->
                    SciCalcActualButton(
                        info = buttonInfo,
                        onClick = { onButtonClick(buttonInfo.symbol) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun BasicCalculatorPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val basicButtonsConfig = listOf(
        SciCalcButtonUIData("⌫", "Backspace", 18.sp, SciButtonType.BASIC_FUNCTION, Icons.AutoMirrored.Filled.Backspace),
        SciCalcButtonUIData("√", "Square Root", 18.sp, SciButtonType.BASIC_FUNCTION),
        SciCalcButtonUIData("%", "Percent", 18.sp, SciButtonType.BASIC_FUNCTION),
        SciCalcButtonUIData("÷", "Divide", 20.sp, SciButtonType.BASIC_OPERATOR),
        SciCalcButtonUIData("7", "Seven", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("8", "Eight", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("9", "Nine", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("×", "Multiply", 20.sp, SciButtonType.BASIC_OPERATOR),
        SciCalcButtonUIData("4", "Four", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("5", "Five", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("6", "Six", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("-", "Subtract", 20.sp, SciButtonType.BASIC_OPERATOR),
        SciCalcButtonUIData("1", "One", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("2", "Two", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("3", "Three", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData("+", "Add", 20.sp, SciButtonType.BASIC_OPERATOR),
        SciCalcButtonUIData("C", "Clear", 18.sp, SciButtonType.BASIC_CLEAR),
        SciCalcButtonUIData("0", "Zero", 18.sp, SciButtonType.BASIC_NUMBER),
        SciCalcButtonUIData(",", "Decimal", 18.sp, SciButtonType.BASIC_DECIMAL),
        SciCalcButtonUIData("=", "Equals", 20.sp, SciButtonType.BASIC_OPERATOR)
    )

    Column(modifier = modifier) {
        basicButtonsConfig.chunked(4).forEach { rowButtons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                rowButtons.forEach { buttonInfo ->
                    SciCalcActualButton(
                        info = buttonInfo,
                        onClick = { onButtonClick(buttonInfo.symbol) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun SciCalcActualButton(
    info: SciCalcButtonUIData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonShape = RoundedCornerShape(8.dp)

    val backgroundColor: Color?
    val backgroundBrush: Brush?
    val textColor: Color

    val basicOperatorGradient = Brush.horizontalGradient(
        listOf(SciCalcBasicOperatorGradientStart, SciCalcBasicOperatorGradientEnd)
    )

    when (info.type) {
        SciButtonType.SCIENTIFIC -> {
            backgroundColor = SciCalcScientificButtonBg
            backgroundBrush = null
            textColor = SciCalcScientificButtonTextColor
        }
        SciButtonType.BASIC_NUMBER, SciButtonType.BASIC_CLEAR, SciButtonType.BASIC_DECIMAL -> {
            backgroundColor = SciCalcBasicNumberButtonBg
            backgroundBrush = null
            textColor = SciCalcBasicButtonTextColor
        }
        SciButtonType.BASIC_FUNCTION -> {
            backgroundColor = SciCalcBasicFunctionButtonBg
            backgroundBrush = null
            textColor = SciCalcBasicButtonTextColor
        }
        SciButtonType.BASIC_OPERATOR -> {
            backgroundColor = null
            backgroundBrush = basicOperatorGradient
            textColor = SciCalcBasicButtonTextColor
        }
    }

    val finalBackgroundModifier = when {
        backgroundBrush != null -> Modifier.background(brush = backgroundBrush, shape = buttonShape)
        backgroundColor != null -> Modifier.background(color = backgroundColor, shape = buttonShape)
        else -> Modifier.background(Color.DarkGray, shape = buttonShape)
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .then(finalBackgroundModifier)
            .clip(buttonShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = info.description },
        contentAlignment = Alignment.Center
    ) {
        if (info.icon != null) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.description,
                tint = textColor,
                modifier = Modifier.size(if (info.type == SciButtonType.SCIENTIFIC) 18.dp else 22.dp)
            )
        } else {
            Text(
                text = info.symbol,
                color = textColor,
                fontSize = info.textSize,
                fontWeight = if (info.type == SciButtonType.BASIC_OPERATOR || info.type == SciButtonType.SCIENTIFIC) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScientificCalculatorScreenPreview() {
    MaterialTheme {
        // Untuk preview, buat NavController dummy dan onNavigateToLogin lambda
        StealthScientificScreen(navController = rememberNavController(), onNavigateToLogin = {})
    }
}