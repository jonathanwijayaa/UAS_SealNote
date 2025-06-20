package com.example.sealnote.view

// path: app/src/main/java/com/example/sealnote/viewmodel/MainViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.ThemeOption
import com.example.sealnote.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // StateFlow yang akan diamati oleh UI untuk mengetahui tema saat ini
    val themeOption: StateFlow<ThemeOption> = preferencesRepository.themeOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeOption.SYSTEM // Nilai awal
        )
}