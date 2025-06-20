// path: app/src/main/java/com/example/sealnote/viewmodel/SettingsViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.ThemeOption
import com.example.sealnote.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 1. Variabel 'themeOption' sekarang ada, sebagai StateFlow
    // Ini akan membaca data dari DataStore melalui repository.
    val themeOption: StateFlow<ThemeOption> = preferencesRepository.themeOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeOption.SYSTEM // Nilai awal sebelum dataStore selesai membaca
        )

    // 2. Fungsi 'onThemeOptionSelected' sekarang ada
    // Ini akan menyimpan pilihan baru pengguna ke DataStore.
    fun onThemeOptionSelected(newThemeOption: ThemeOption) {
        viewModelScope.launch {
            preferencesRepository.saveThemeOption(newThemeOption)
        }
    }
}