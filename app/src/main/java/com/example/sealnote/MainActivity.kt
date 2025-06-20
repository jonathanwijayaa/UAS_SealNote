package com.example.sealnote

import android.os.Bundle
// 1. Ganti import ini
// import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity // <-- Gunakan import ini
import com.example.sealnote.ui.theme.SealnoteTheme
import com.example.sealnote.view.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// 2. Ganti kelas dasar dari ComponentActivity menjadi FragmentActivity
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SealnoteTheme {
                AppNavigation()
            }
        }
    }
}