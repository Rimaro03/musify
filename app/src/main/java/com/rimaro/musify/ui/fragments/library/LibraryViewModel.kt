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
    private var _recentPlaylists: List<SimplifiedPlaylistObject> = listOf()
    private var _alphabetPlaylists: List<SimplifiedPlaylistObject> = listOf()

    private val _currentViewMode: MutableLiveData<LibraryViewMode> = MutableLiveData(LibraryViewMode.LIST)
    val currentViewMode: LiveData<LibraryViewMode> = _currentViewMode

    private val _currentSortMode: MutableLiveData<LibrarySortMode> = MutableLiveData(LibrarySortMode.RECENT)
    val currentSortMode: LiveData<LibrarySortMode> = _currentSortMode

    fun retrieveUserPlaylists() {
        viewModelScope.launch {
            val token = spotifyTokenManager.retrieveAccessToken()

            val userPlaylists = spotifyRepository.getUserPlaylists("Bearer $token")
            for(playlist in userPlaylists.items) {
                Log.d("HomeViewModel", "Playlist: ${playlist.name}")
            }
            _userPlaylists.value = userPlaylists.items
            _recentPlaylists = userPlaylists.items
            _alphabetPlaylists = userPlaylists.items.sortedBy { it.name }
        }
    }

    fun changeViewMode() {
        if(_currentViewMode.value == LibraryViewMode.LIST) {
            _currentViewMode.value = LibraryViewMode.GRID
        } else {
            _currentViewMode.value = LibraryViewMode.LIST
        }
    }

    fun changeSortMode() {
        if(_currentSortMode.value == LibrarySortMode.RECENT) {
            _currentSortMode.value = LibrarySortMode.ALPHABETICAL
            _userPlaylists.value = _alphabetPlaylists
        } else if(_currentSortMode.value == LibrarySortMode.ALPHABETICAL) {
            _currentSortMode.value = LibrarySortMode.RECENT
            _userPlaylists.value = _recentPlaylists
        }
    }
}