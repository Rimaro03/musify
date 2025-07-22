package com.rimaro.musify.ui.fragments.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager
) : ViewModel() {
    private val _userPlaylists: MutableLiveData<List<SimplifiedPlaylistObject>> = MutableLiveData()
    val userPlaylists: LiveData<List<SimplifiedPlaylistObject>> = _userPlaylists

    fun retrieveUserPlaylists() {
        viewModelScope.launch {
            val token = spotifyTokenManager.retrieveAccessToken()

            val userPlaylists = spotifyRepository.getUserPlaylists("Bearer $token")
            for(playlist in userPlaylists.items) {
                Log.d("HomeViewModel", "Playlist: ${playlist.name}")
            }
            _userPlaylists.value = userPlaylists.items
        }
    }
}