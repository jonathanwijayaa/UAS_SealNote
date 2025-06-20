// path: app/src/main/java/com/example/sealnote/data/UserPreferencesRepository.kt

package com.example.sealnote.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val THEME_OPTION_KEY = stringPreferencesKey("theme_option")

@Singleton
// --- HAPUS @Inject constructor DAN @ApplicationContext DARI SINI ---
class UserPreferencesRepository @Inject constructor(private val context: Context) {
    // ... sisa kodenya sama ...
    val themeOption: Flow<ThemeOption> = context.dataStore.data
        .map { preferences ->
            val optionName = preferences[THEME_OPTION_KEY] ?: ThemeOption.SYSTEM.name
            ThemeOption.valueOf(optionName)
        }

    suspend fun saveThemeOption(themeOption: ThemeOption) {
        context.dataStore.edit { settings ->
            settings[THEME_OPTION_KEY] = themeOption.name
        }
    }
}