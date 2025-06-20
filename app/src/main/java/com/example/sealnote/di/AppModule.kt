// path: app/src/main/java/com/example/sealnote/di/AppModule.kt

package com.example.sealnote.di

import android.content.Context
import com.example.sealnote.data.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Modul ini akan hidup selama aplikasi berjalan
object AppModule {

    // Fungsi ini memberitahu Hilt: "Jika ada yang butuh UserPreferencesRepository,
    // jalankan fungsi ini untuk membuatnya".
    @Provides
    @Singleton // Pastikan hanya ada satu instance dari repository ini di seluruh aplikasi
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    // Anda bisa menambahkan @Provides lain untuk dependency lain di sini
    // misalnya untuk Room Database, Retrofit, dll.
}