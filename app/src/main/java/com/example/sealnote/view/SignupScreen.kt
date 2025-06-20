// path: app/src/main/java/com/example/sealnote/view/SignUpScreen.kt

package com.example.sealnote.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sealnote.R
import com.example.sealnote.ui.theme.*
import com.example.sealnote.viewmodel.SignUpViewModel

// --- Impor warna kustom dari Color.kt ---
import com.example.sealnote.ui.theme.AlreadyAccountTextColor
import com.example.sealnote.ui.theme.LoginLinkColor
import com.example.sealnote.ui.theme.SignUpButtonGradientEnd
import com.example.sealnote.ui.theme.SignUpButtonGradientStart
import com.example.sealnote.ui.theme.SignUpButtonTextColor
import com.example.sealnote.ui.theme.SignUpGoogleButtonBackground
import com.example.sealnote.ui.theme.SignUpGoogleButtonTextColor
import com.example.sealnote.ui.theme.SignUpInputFieldBackground
import com.example.sealnote.ui.theme.SignUpInputTextColor
import com.example.sealnote.ui.theme.SignUpInputTextHintColor
import com.example.sealnote.ui.theme.SignUpScreenBackground
import com.example.sealnote.ui.theme.SignUpSubtitleTextColor
import com.example.sealnote.ui.theme.SignUpTitleTextColor

@Composable
fun SignUpScreen(
    // Dapatkan instance ViewModel menggunakan Hilt
    viewModel: SignUpViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // --- TAMBAHAN ---
    // Observe/dengarkan `signUpResult` dari ViewModel sebagai sebuah State
    val signUpResult by viewModel.signUpResult.observeAsState()

    // --- TAMBAHAN ---
    // Gunakan LaunchedEffect untuk menangani side-effects seperti Toast & Navigasi
    // Ini hanya akan berjalan ketika nilai `signUpResult` berubah
    LaunchedEffect(signUpResult) {
        when (val result = signUpResult) {
            is SignUpViewModel.SignUpResult.Success -> {
                Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                onSignUpSuccess() // Panggil navigasi setelah sukses
            }
            is SignUpViewModel.SignUpResult.Error -> {
                // Tampilkan pesan error dari Firebase/ViewModel
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            // Tidak perlu melakukan apa-apa untuk Loading atau state null awal
            else -> {}
        }
    }

    // --- TAMBAHAN: Box untuk menampung UI dan Indikator Loading ---
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SignUpScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(84.dp))

                Text(
                    text = "Create Account",
                    color = SignUpTitleTextColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please fill the input below here",
                    color = SignUpSubtitleTextColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    hint = "full name",
                    leadingIconRes = R.drawable.ic_person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    hint = "youremail@gmail.com",
                    leadingIconRes = R.drawable.ic_email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    hint = "password",
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomOutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    hint = "confirm password",
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            // --- DIUBAH: Panggil fungsi ViewModel ---
                            viewModel.signUp(fullName, email, password, confirmPassword)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // --- DIUBAH: Panggil fungsi ViewModel ---
                        viewModel.signUp(fullName, email, password, confirmPassword)
                    },
                    // --- TAMBAHAN: Nonaktifkan tombol saat sedang loading ---
                    enabled = signUpResult !is SignUpViewModel.SignUpResult.Loading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(119.dp)
                        .height(40.dp)
                        .align(Alignment.End),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SignUpButtonGradientStart, SignUpButtonGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            color = SignUpButtonTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onGoogleSignInClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignUpGoogleButtonBackground,
                        contentColor = SignUpGoogleButtonTextColor
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
                            color = SignUpGoogleButtonTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ClickableLoginText(onLoginClick = onLoginClick)

                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // --- TAMBAHAN: Tampilkan Indikator Loading di tengah layar ---
        if (signUpResult is SignUpViewModel.SignUpResult.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    leadingIconRes: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = SignUpInputTextHintColor) },
        placeholder = { Text(hint, color = SignUpInputTextHintColor) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIconRes),
                contentDescription = "$label Icon",
                tint = SignUpInputTextHintColor
            )
        },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(color = SignUpInputTextColor, fontSize = 14.sp),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SignUpInputFieldBackground,
            unfocusedContainerColor = SignUpInputFieldBackground,
            disabledContainerColor = SignUpInputFieldBackground,
            cursorColor = SignUpInputTextColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
private fun ClickableLoginText(onLoginClick: () -> Unit) {
    val ANNOTATION_TAG = "login_link_tag"

    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = AlreadyAccountTextColor, fontSize = 13.sp)) {
            append("Already have an account? ")
        }
        pushStringAnnotation(tag = ANNOTATION_TAG, annotation = "login_action")
        withStyle(
            style = SpanStyle(
                color = LoginLinkColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Log In")
        }
        pop()
    }

    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = annotatedText,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        textLayoutResult?.let { layoutResult ->
                            val annotation = annotatedText.getStringAnnotations(
                                tag = ANNOTATION_TAG,
                                start = layoutResult.getOffsetForPosition(offset),
                                end = layoutResult.getOffsetForPosition(offset)
                            ).firstOrNull()

                            if (annotation?.tag == ANNOTATION_TAG) {
                                onLoginClick()
                            }
                        }
                    }
                )
            },
            onTextLayout = { layoutResult -> textLayoutResult = layoutResult },
            style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF152332)
@Composable
fun SignupScreenPreview() {
    MaterialTheme {
        // Panggilan preview disesuaikan dengan parameter baru
        SignUpScreen(
            onSignUpSuccess = {},
            onGoogleSignInClick = {},
            onLoginClick = {}
        )
    }
}