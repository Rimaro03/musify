package com.rimaro.musify.ui.fragments.auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val spotifyTokenManager: SpotifyTokenManager,
    private val spotifyRepository: SpotifyRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    fun handleSuccessfulLogin(code: String) {
        runBlocking {
            Log.d("AuthViewModel", "code: $code")
            // set auth token
            val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            authPrefs.edit { putString("auth_code", code) }
        }
    }
}