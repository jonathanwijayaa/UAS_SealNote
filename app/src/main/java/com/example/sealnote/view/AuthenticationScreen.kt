// path: app/src/main/java/com/example/sealnote/view/AuthenticationScreen.kt

package com.example.sealnote.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sealnote.R
import com.example.sealnote.ui.theme.SealnoteTheme
import com.example.sealnote.viewmodel.AuthViewModel
import com.example.sealnote.viewmodel.AuthenticatorType
import com.example.sealnote.viewmodel.BiometricAuthState

val AuthScreenBackground = Color(0xFF0A0F1E)
val AuthCardBackgroundColor = Color(0xFF10182C)
val AuthTextColor = Color.White
val AuthTabLayoutBackgroundColor = Color(0xFF0D1326)
val AuthSelectedTabBrush = Brush.horizontalGradient(listOf(Color(0xFF7B5DFF), Color(0xFF5D7FFF)))
val AuthUnselectedTabColor = Color.Transparent

@Composable
fun AuthenticationRoute(
    onUsePinClick: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Hoist state tab ke sini
    var selectedTabIndex by remember { mutableStateOf(1) }
    val currentAuthType = if (selectedTabIndex == 0) AuthenticatorType.FINGERPRINT else AuthenticatorType.FACE

    // Cek ketersediaan untuk setiap tipe
    val isBiometricAvailable = remember(currentAuthType) {
        viewModel.canAuthenticate(context, currentAuthType)
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is BiometricAuthState.Success -> {
                Toast.makeText(context, "Authentication Succeeded!", Toast.LENGTH_SHORT).show()
                onAuthSuccess()
                viewModel.resetAuthState()
            }
            is BiometricAuthState.Error -> {
                if (state.message.contains("Cancel", ignoreCase = true).not()) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    AuthenticationScreen(
        onUsePinClick = onUsePinClick,
        isBiometricAvailable = isBiometricAvailable,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index -> selectedTabIndex = index },
        onAuthClick = {
            // Kirim tipe autentikasi yang dipilih ke ViewModel
            viewModel.startAuthentication(context as FragmentActivity, currentAuthType)
        }
    )
}

@Composable
fun AuthenticationScreen(
    onUsePinClick: () -> Unit,
    onAuthClick: () -> Unit,
    isBiometricAvailable: Boolean,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit // Terima callback untuk mengubah tab
) {
    val currentAuthMethod = if (selectedTabIndex == 0) AuthMethod.Fingerprint else AuthMethod.Face

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AuthScreenBackground
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .width(332.dp)
                    .height(305.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuthCardBackgroundColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthCustomTabLayout(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = onTabSelected // Gunakan callback
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Unlock to open secret notes",
                        color = AuthTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Image(
                        painter = painterResource(id = currentAuthMethod.iconRes),
                        contentDescription = currentAuthMethod.contentDescription,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable(enabled = isBiometricAvailable) {
                                onAuthClick()
                            }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = if (isBiometricAvailable) currentAuthMethod.authText else "This biometric type is not available or not set up.",
                        color = AuthTextColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = "Use PIN",
                        color = AuthTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUsePinClick() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthCustomTabLayout(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Fingerprint", "Face")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(AuthTabLayoutBackgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val tabBackgroundModifier = if (isSelected) {
                Modifier.background(AuthSelectedTabBrush, shape = RoundedCornerShape(6.dp))
            } else {
                Modifier.background(AuthUnselectedTabColor, shape = RoundedCornerShape(6.dp))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(tabBackgroundModifier)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, color = AuthTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// === Teks instruksi disesuaikan kembali agar lebih spesifik ===
private enum class AuthMethod(val iconRes: Int, val authText: String, val contentDescription: String) {
    Fingerprint(
        iconRes = R.drawable.ic_finger,
        authText = "Place your finger on the sensor to authenticate.",
        contentDescription = "Fingerprint Icon"
    ),
    Face(
        iconRes = R.drawable.ic_face_id,
        authText = "Position your face in the frame to authenticate.",
        contentDescription = "Face Icon"
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0F1E)
@Composable
fun AuthenticationScreenPreview() {
    SealnoteTheme {
        AuthenticationScreen(onUsePinClick = {}, onAuthClick = {}, isBiometricAvailable = true, selectedTabIndex = 1, onTabSelected = {})
    }
}