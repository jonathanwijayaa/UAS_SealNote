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
import com.example.sealnote.ui.theme.*
import com.example.sealnote.viewmodel.CalculatorHistoryViewModel
import com.example.sealnote.viewmodel.StealthCalculatorViewModel
import kotlinx.coroutines.launch

private data class CalcButtonInfo(
    val symbol: String,
    val type: ButtonType,
    val textSize: TextUnit,
    val description: String,
    val icon: ImageVector? = null
)

private enum class ButtonType {
    NUMBER, OPERATOR, FUNCTION, CLEAR, DECIMAL, BACKSPACE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StealthCalculatorScreen(
    onNavigateToLogin: () -> Unit,
    navController: NavHostController,
    viewModel: StealthCalculatorViewModel = viewModel(),
    historyViewModel: CalculatorHistoryViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Mengatur callback untuk menyimpan riwayat saat composable pertama kali dijalankan
    LaunchedEffect(historyViewModel) {
        viewModel.onCalculationFinished = { expression, result ->
            historyViewModel.addHistoryEntry(expression, result)
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
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Kalkulator Ilmiah") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthScientific") {
                            popUpTo("stealthCalculator") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Riwayat Kalkulasi") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthHistory")
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
                            text = "Kalkulator",
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
                        containerColor = CalcScreenBackground,
                        titleContentColor = CalcDisplayTextColor
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CalcScreenBackground)
                    .padding(paddingValues)
            ) {
                CalculatorDisplay(
                    displayText = viewModel.displayText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                )

                CalculatorButtonsGridModified(
                    onButtonClick = { buttonSymbol, isBackspace ->
                        if (isBackspace) {
                            viewModel.onBackspaceClick()
                        } else {
                            // Meneruskan aksi navigasi ke ViewModel pada setiap klik
                            viewModel.onCalculatorButtonClick(buttonSymbol, onNavigateToLogin)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CalculatorDisplay(displayText: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CalcDisplayCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = displayText,
                color = CalcDisplayTextColor,
                fontSize = 56.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalculatorButtonsGridModified(
    onButtonClick: (symbol: String, isBackspace: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonCornerRadius = 16.dp

    val buttonRowsConfig = listOf(
        listOf(
            CalcButtonInfo("C", ButtonType.CLEAR, 26.sp, "Clear"),
            CalcButtonInfo("X/Y", ButtonType.FUNCTION, 20.sp, "X divided by Y"),
            CalcButtonInfo("%", ButtonType.FUNCTION, 24.sp, "Percent"),
            CalcButtonInfo("÷", ButtonType.OPERATOR, 30.sp, "Divide")
        ),
        listOf(
            CalcButtonInfo("7", ButtonType.NUMBER, 26.sp, "Seven"),
            CalcButtonInfo("8", ButtonType.NUMBER, 26.sp, "Eight"),
            CalcButtonInfo("9", ButtonType.NUMBER, 26.sp, "Nine"),
            CalcButtonInfo("×", ButtonType.OPERATOR, 30.sp, "Multiply")
        ),
        listOf(
            CalcButtonInfo("4", ButtonType.NUMBER, 26.sp, "Four"),
            CalcButtonInfo("5", ButtonType.NUMBER, 26.sp, "Five"),
            CalcButtonInfo("6", ButtonType.NUMBER, 26.sp, "Six"),
            CalcButtonInfo("-", ButtonType.OPERATOR, 30.sp, "Subtract")
        ),
        listOf(
            CalcButtonInfo("1", ButtonType.NUMBER, 26.sp, "One"),
            CalcButtonInfo("2", ButtonType.NUMBER, 26.sp, "Two"),
            CalcButtonInfo("3", ButtonType.NUMBER, 26.sp, "Three"),
            CalcButtonInfo("+", ButtonType.OPERATOR, 30.sp, "Add")
        ),
        listOf(
            CalcButtonInfo("", ButtonType.BACKSPACE, 24.sp, "Backspace", Icons.AutoMirrored.Filled.Backspace),
            CalcButtonInfo("0", ButtonType.NUMBER, 26.sp, "Zero"),
            CalcButtonInfo(",", ButtonType.DECIMAL, 26.sp, "Comma"),
            CalcButtonInfo("=", ButtonType.OPERATOR, 30.sp, "Equal")
        )
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttonRowsConfig.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { buttonInfo ->
                    CalculatorButtonModified(
                        info = buttonInfo,
                        onClick = { onButtonClick(buttonInfo.symbol, buttonInfo.type == ButtonType.BACKSPACE) },
                        shape = RoundedCornerShape(buttonCornerRadius),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorButtonModified(
    info: CalcButtonInfo,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color?
    val backgroundBrush: Brush?

    val CalcOperatorGradient = Brush.horizontalGradient(
        listOf(CalcOperatorGradientStart, CalcOperatorGradientEnd)
    )

    when (info.type) {
        ButtonType.OPERATOR -> {
            backgroundColor = null; backgroundBrush = CalcOperatorGradient
        }
        else -> {
            backgroundColor = CalcNonOperatorButtonBg; backgroundBrush = null
        }
    }

    val finalBackgroundModifier = when {
        backgroundBrush != null -> Modifier.background(brush = backgroundBrush, shape = shape)
        backgroundColor != null -> Modifier.background(color = backgroundColor, shape = shape)
        else -> Modifier // Harusnya tidak pernah terjadi
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .then(finalBackgroundModifier)
            .clickable(onClick = onClick)
            .semantics { contentDescription = info.description },
        contentAlignment = Alignment.Center
    ) {
        if (info.icon != null) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.description,
                tint = CalcButtonIconColor,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = info.symbol,
                color = CalcButtonTextColor,
                fontSize = info.textSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalculatorScreenModifiedPreview() {
    MaterialTheme {
        StealthCalculatorScreen(onNavigateToLogin = {}, navController = rememberNavController())
    }
}