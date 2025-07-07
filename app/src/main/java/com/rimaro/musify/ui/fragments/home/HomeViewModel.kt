package com.rimaro.musify.ui.fragments.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimaro.musify.domain.model.SimplifiedPlaylistObject
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
    private val prefs: SharedPreferences,
): ViewModel() {
    private val _userPlaylists: MutableLiveData<List<SimplifiedPlaylistObject>> = MutableLiveData()
    val userPlaylists: LiveData<List<SimplifiedPlaylistObject>> = _userPlaylists

    fun checkAuthSaved() : Boolean {
        val authCode = prefs.getString("auth_code", null)
        return ( authCode != null && authCode != "" )
    }

    fun retrieveUserPlaylists() {
        viewModelScope.launch {
            val token = spotifyTokenManager.retrieveAccessToken()
            val userProfile = spotifyRepository.getUserProfile("Bearer $token")
            val userId = userProfile.id

            val userPlaylists = spotifyRepository.getUserPlaylists("Bearer $token", userId)
            for(playlist in userPlaylists.items) {
                Log.d("HomeViewModel", "Playlist: ${playlist.name}")
            }
            _userPlaylists.value = userPlaylists.items
        }
    }
}