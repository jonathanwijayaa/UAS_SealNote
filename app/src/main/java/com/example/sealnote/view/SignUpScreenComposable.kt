package com.example.sealnote.view // Atau package UI Anda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
// REMOVED: import androidx.compose.foundation.clickable // No longer directly used for link
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.sp // <-- REMOVED THE TRAILING '>' CHARACTER HERE

// --- CORRECTED IMPORTS FOR POINTER INPUT / GESTURES ---
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures // Correct import for detectTapGestures
// REMOVED: import androidx.compose.ui.gesture.tap // This was the problematic old import
// REMOVED: import androidx.compose.ui.input.pointer.PointerEventPass // Not directly used by detectTapGestures
// REMOVED: import androidx.compose.ui.input.pointer.PointerInputChange // Not directly used by detectTapGestures
// REMOVED: import androidx.compose.ui.input.pointer.changedToUp // Not directly used by detectTapGestures
// --- END CORRECTED IMPORTS ---

import com.example.sealnote.R // Pastikan path ini benar

// --- START: Impor warna kustom dari Color.kt ---
import com.example.sealnote.ui.theme.SignUpScreenBackground
import com.example.sealnote.ui.theme.SignUpTitleTextColor
import com.example.sealnote.ui.theme.SignUpSubtitleTextColor
import com.example.sealnote.ui.theme.SignUpInputFieldBackground
import com.example.sealnote.ui.theme.SignUpInputTextHintColor
import com.example.sealnote.ui.theme.SignUpInputTextColor
import com.example.sealnote.ui.theme.SignUpButtonGradientStart
import com.example.sealnote.ui.theme.SignUpButtonGradientEnd
import com.example.sealnote.ui.theme.SignUpButtonTextColor
import com.example.sealnote.ui.theme.SignUpGoogleButtonBackground
import com.example.sealnote.ui.theme.SignUpGoogleButtonTextColor
import com.example.sealnote.ui.theme.AlreadyAccountTextColor
import com.example.sealnote.ui.theme.LoginLinkColor
// --- END: Impor warna kustom ---

@Composable
fun SignUpScreenComposable(
    onSignUpClick: (fullName: String, email: String, password: String, confirmPassword: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SignUpScreenBackground // Menggunakan warna dari Color.kt
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp), // Sesuai guideline di XML
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(84.dp)) // marginTop untuk titleText

            Text(
                text = "Create Account",
                color = SignUpTitleTextColor, // Menggunakan warna dari Color.kt
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please fill the input below here",
                color = SignUpSubtitleTextColor, // Menggunakan warna dari Color.kt
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Input
            CustomOutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                hint = "full name",
                leadingIconRes = R.drawable.ic_person, // Ganti dengan resource Anda
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                hint = "youremail@gmail.com",
                leadingIconRes = R.drawable.ic_email, // Ganti dengan resource Anda
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                hint = "password",
                leadingIconRes = R.drawable.ic_lock, // Ganti dengan resource Anda
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input
            CustomOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                hint = "confirm password",
                leadingIconRes = R.drawable.ic_lock, // Ganti dengan resource Anda
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSignUpClick(fullName, email, password, confirmPassword)
                    }
                ),
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSignUpClick(fullName, email, password, confirmPassword)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(119.dp)
                    .height(40.dp)
                    .align(Alignment.End), // Sesuai XML (bias 1.0)
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(SignUpButtonGradientStart, SignUpButtonGradientEnd) // Menggunakan warna dari Color.kt
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        color = SignUpButtonTextColor, // Menggunakan warna dari Color.kt
                        fontSize = 16.sp,
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
                    containerColor = SignUpGoogleButtonBackground, // Menggunakan warna dari Color.kt
                    contentColor = SignUpGoogleButtonTextColor // Menggunakan warna dari Color.kt
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // XML 243dp, ini membuatnya responsif
                    .height(50.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google), // Ganti dengan resource Anda
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        color = SignUpGoogleButtonTextColor // Menggunakan warna dari Color.kt
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Mendorong teks berikutnya ke bawah

            ClickableLoginText(onLoginClick = onLoginClick)

            Spacer(modifier = Modifier.height(60.dp)) // marginBottom="100dp" kira-kira, sesuaikan
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
        label = { Text(label, color = SignUpInputTextHintColor) }, // Menggunakan warna dari Color.kt
        placeholder = { Text(hint, color = SignUpInputTextHintColor) }, // Menggunakan warna dari Color.kt
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIconRes),
                contentDescription = "$label Icon",
                tint = SignUpInputTextHintColor // Menggunakan warna dari Color.kt
            )
        },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None, // Pastikan VisualTransformation diimpor
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(color = SignUpInputTextColor, fontSize = 14.sp), // Menggunakan warna dari Color.kt
        shape = RoundedCornerShape(8.dp), // Bentuk dari areainput
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SignUpInputFieldBackground, // Menggunakan warna dari Color.kt
            unfocusedContainerColor = SignUpInputFieldBackground, // Menggunakan warna dari Color.kt
            disabledContainerColor = SignUpInputFieldBackground, // Menggunakan warna dari Color.kt
            cursorColor = SignUpInputTextColor, // Menggunakan warna dari Color.kt
            focusedIndicatorColor = Color.Transparent, // Sembunyikan garis bawah
            unfocusedIndicatorColor = Color.Transparent, // Sembunyikan garis bawah
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
                textDecoration = TextDecoration.Underline // Opsional
            )
        ) {
            append("Log In")
        }
        pop() // Pop the annotation
    }

    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Use Text composable with pointerInput for click detection
        Text(
            text = annotatedText,
            modifier = Modifier.pointerInput(Unit) { // Use Unit as key if lambda doesn't depend on external state
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
            onTextLayout = { layoutResult -> textLayoutResult = layoutResult }, // Simpan TextLayoutResult
            style = LocalTextStyle.current.copy(textAlign = TextAlign.Center) // Gaya teks
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF152332)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreenComposable(
            onSignUpClick = { _, _, _, _ -> },
            onGoogleSignInClick = {},
            onLoginClick = {}
        )
    }
}