package com.example.sealnote.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sealnote.R
import com.example.sealnote.ui.theme.SealnoteTheme
import com.example.sealnote.viewmodel.LoginViewModel

// Definisi Warna
val LoginScreenBackground = Color(0xFF152332)
val LoginWelcomeTextColor = Color(0xFFFDFDFD)
val LoginInfoTextColor = Color(0xFFEDEDED)
val LoginInputFieldBackground = Color(0xFF2A2E45)
val LoginInputTextHintColor = Color(0xFFBBBBBB)
val LoginInputTextColor = Color.White
val LoginButtonGradientStart = Color(0xFF8000FF)
val LoginButtonGradientEnd = Color(0xFF00D1FF)
val LoginButtonTextColor = Color.White
val ForgotPasswordTextColor = Color(0xFF2493D7)
val NoAccountTextColor = Color(0xFFF8F8F8)
val SignUpLinkColor = Color(0xFFEDEDED)
val GoogleButtonBackground = Color(0xFF3E5166)
val GoogleButtonTextColor = Color.White

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val loginResult by viewModel.loginResult.observeAsState()
    val isLoggedIn by viewModel.isLoggedIn.observeAsState()

    // Menangani hasil dari proses login
    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginViewModel.LoginResult.Success -> {
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is LoginViewModel.LoginResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Menangani kasus jika pengguna sudah login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LoginScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo_sealnote),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(173.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome Back!",
                    color = LoginWelcomeTextColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter your login information to continue",
                    color = LoginInfoTextColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email", color = LoginInputTextHintColor) },
                    placeholder = { Text("youremail@gmail.com", color = LoginInputTextHintColor) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = "Email Icon",
                            tint = LoginInputTextHintColor
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    textStyle = TextStyle(color = LoginInputTextColor, fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LoginInputFieldBackground,
                        unfocusedContainerColor = LoginInputFieldBackground,
                        disabledContainerColor = LoginInputFieldBackground,
                        cursorColor = LoginInputTextColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password", color = LoginInputTextHintColor) },
                    placeholder = { Text("password", color = LoginInputTextHintColor) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Password Icon",
                            tint = LoginInputTextHintColor
                        )
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    ),
                    textStyle = TextStyle(color = LoginInputTextColor, fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LoginInputFieldBackground,
                        unfocusedContainerColor = LoginInputFieldBackground,
                        disabledContainerColor = LoginInputFieldBackground,
                        cursorColor = LoginInputTextColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot Password?",
                    color = ForgotPasswordTextColor,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onForgotPasswordClick() }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login(email, password)
                    },
                    enabled = loginResult !is LoginViewModel.LoginResult.Loading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(143.dp)
                        .height(39.dp)
                        .align(Alignment.End),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LoginButtonGradientStart, LoginButtonGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            color = LoginButtonTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Google Sign-In Button
                Button(
                    onClick = onGoogleSignInClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoogleButtonBackground,
                        contentColor = GoogleButtonTextColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            color = GoogleButtonTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                ClickableSignUpText(onSignUpClick)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (loginResult is LoginViewModel.LoginResult.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ClickableSignUpText(onSignUpClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = NoAccountTextColor, fontSize = 13.sp)) {
            append("Don’t have an account? ")
        }
        pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
        withStyle(
            style = SpanStyle(
                color = SignUpLinkColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Sign Up")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                .firstOrNull()?.let {
                    onSignUpClick()
                }
        },
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF152332)
@Composable
fun LoginScreenPreview() {
    SealnoteTheme {
        LoginScreen(
            onLoginSuccess = {},
            onGoogleSignInClick = {},
            onForgotPasswordClick = {},
            onSignUpClick = {}
        )
    }
}