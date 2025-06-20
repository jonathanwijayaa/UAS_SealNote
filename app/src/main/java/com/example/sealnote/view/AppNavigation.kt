package com.example.sealnote.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sealnote.viewmodel.CalculatorHistoryViewModel
import com.example.sealnote.viewmodel.StealthCalculatorViewModel
import com.example.sealnote.viewmodel.StealthScientificViewModel

/**
 * Mendefinisikan semua rute dan Composables untuk navigasi aplikasi.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "stealthCalculator"
) {
    val calculatorHistoryViewModel: CalculatorHistoryViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- Stealth Mode ---
        composable("stealthCalculator") {
            val stealthCalculatorViewModel: StealthCalculatorViewModel = hiltViewModel()
            StealthCalculatorScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("stealthCalculator") { inclusive = true }
                    }
                },
                navController = navController,
                viewModel = stealthCalculatorViewModel,
                historyViewModel = calculatorHistoryViewModel
            )
        }
        composable("stealthScientific") {
            val stealthScientificViewModel: StealthScientificViewModel = hiltViewModel()
            StealthScientificScreen(
                navController = navController,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("stealthScientific") { inclusive = true }
                    }
                },
                viewModel = stealthScientificViewModel,
                historyViewModel = calculatorHistoryViewModel
            )
        }
        composable("stealthHistory") {
            StealthHistoryScreen(
                navController = navController,
                historyViewModel = calculatorHistoryViewModel
            )
        }

        // --- SealNote Login & Signup ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("homepage") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    navController.navigate("homepage") { popUpTo("login") { inclusive = true } }
                },
                onForgotPasswordClick = {},
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    navController.navigate("homepage") { popUpTo("signup") { inclusive = true } }
                },
                onLoginClick = {
                    navController.navigate("login") { popUpTo("signup") { inclusive = true } }
                }
            )
        }

        // --- Note Mode ---
        composable("homepage") {
            HomepageRoute(navController = navController)
        }

        composable("profile") {
            ProfileRoute(navController = navController)
        }

        composable("bookmarks") {
            BookmarksRoute(navController = navController)
        }

        composable("secretNotes") {
            SecretNotesRoute(navController = navController)
        }

        composable("secretNotesLocked") {
            AuthenticationRoute(
                onUsePinClick = {
                    navController.navigate("login") { popUpTo("secretNotesLocked") { inclusive = true } }
                },
                onAuthSuccess = {
                    navController.navigate("secretNotes") { popUpTo("secretNotesLocked") { inclusive = true } }
                }
            )
        }

        composable("authentication") {
            AuthenticationRoute(
                onUsePinClick = {
                    navController.navigate("login") { popUpTo("authentication") { inclusive = true } }
                },
                onAuthSuccess = {
                    navController.navigate("secretNotes") { popUpTo("authentication") { inclusive = true } }
                }
            )
        }

        composable("trash") {
            TrashRoute(navController = navController)
        }

        composable("settings") {
            SettingsRoute(navController = navController)
        }

        composable(
            route = "add_edit_note_screen/{noteId}?isSecret={isSecret}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("isSecret") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            AddEditNoteRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}