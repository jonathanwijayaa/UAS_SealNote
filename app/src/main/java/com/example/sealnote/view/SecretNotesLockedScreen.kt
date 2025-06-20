package com.example.sealnote.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sealnote.R
import com.example.sealnote.ui.theme.SealnoteTheme // Asumsi AppTheme Anda

// Definisi Warna dari XML
val LockedScreenBackground = Color(0xFF152332)
val LockedScreenTextColor = Color(0xFFFFFFFE)

@Composable
fun SecretNotesLockedScreen(
    onAuthenticate: () -> Unit // Callback untuk navigasi setelah autentikasi
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LockedScreenBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Lock Icon",
                    modifier = Modifier.size(35.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Notes Locked",
                    color = LockedScreenTextColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = onAuthenticate) { // Memanggil callback onAuthenticate
                    Text("Unlock Notes")
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun NotesLockedScreenPreviewWithOuterBackground() {
    SealnoteTheme {
        Box(Modifier.fillMaxSize().background(Color.White)) {
            SecretNotesLockedScreen(onAuthenticate = {}) // Lambda kosong untuk preview
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesLockedScreenPreview() {
    SealnoteTheme {
        SecretNotesLockedScreen(onAuthenticate = {}) // Lambda kosong untuk preview
    }
}
