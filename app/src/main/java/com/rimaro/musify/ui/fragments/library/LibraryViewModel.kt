package com.rimaro.musify.ui.fragments.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimaro.musify.data.remote.model.Album
import com.rimaro.musify.data.remote.model.ArtistObject
import com.rimaro.musify.data.remote.model.ExternalUrls
import com.rimaro.musify.data.remote.model.PlaylistResponse
import com.rimaro.musify.data.remote.model.SavedAlbumObject
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.data.remote.model.Tracks
import com.rimaro.musify.data.remote.model.UserObject
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
    // library content: playlist, album, artists, podcasts
    private val _userPlaylists: MutableLiveData<List<SimplifiedPlaylistObject>> = MutableLiveData()
    val userPlaylists: LiveData<List<SimplifiedPlaylistObject>> = _userPlaylists
    private var _recentPlaylists: List<SimplifiedPlaylistObject> = listOf()
    private var _alphabetPlaylists: List<SimplifiedPlaylistObject> = listOf()

    private val _userAlbums: MutableLiveData<List<Album>> = MutableLiveData()
    val userAlbums: LiveData<List<Album>> = _userAlbums
    private var _recentAlbums: List<Album> = listOf()
    private var _alphabetAlbums: List<Album> = listOf()

    private val _userArtists: MutableLiveData<List<ArtistObject>> = MutableLiveData()
    val userArtists: LiveData<List<ArtistObject>> = _userArtists
    private var _recentArtists: List<ArtistObject> = listOf()
    private var _alphabetArtists: List<ArtistObject> = listOf()

    private val _currentViewMode: MutableLiveData<LibraryViewMode> = MutableLiveData(LibraryViewMode.LIST)
    val currentViewMode: LiveData<LibraryViewMode> = _currentViewMode

    private val _currentSortMode: MutableLiveData<LibrarySortMode> = MutableLiveData(LibrarySortMode.RECENT)
    val currentSortMode: LiveData<LibrarySortMode> = _currentSortMode

    private val _selectedCategory: MutableLiveData<FilterCategories> = MutableLiveData(FilterCategories.PLAYLIST)
    val selectedCategory: LiveData<FilterCategories> = _selectedCategory

    fun retrieveUserPlaylists() {
        viewModelScope.launch {
            val token = spotifyTokenManager.retrieveAccessToken()

            val userPlaylists = spotifyRepository.getUserPlaylists("Bearer $token")
            val savedTracksPlaylist = SimplifiedPlaylistObject(
                collaborative = false,
                description = "Liked songs",
                external_url = null,
                href = "",
                id = "-1",
                images = listOf(),
                name = "Liked songs",
                owner = UserObject(
                    display_name = "You",
                    external_urls = ExternalUrls(
                        spotify = ""
                    ),
                    href = "",
                    id = "",
                    type = "",
                    uri = ""
                ),
                public = false,
                snapshot_id = "",
                tracks = Tracks(
                    href = "",
                    total = 0
                ),
                type = "",
                uri = ""
            )
            _userPlaylists.value = listOf(savedTracksPlaylist) + userPlaylists.items
            _recentPlaylists = listOf(savedTracksPlaylist) + userPlaylists.items
            _alphabetPlaylists = listOf(savedTracksPlaylist) + userPlaylists.items.sortedBy { it.name }

            val userAlbums = spotifyRepository.getUserSavedAlbums("Bearer $token").items.map { it.album }
            _userAlbums.value = userAlbums
            _recentAlbums = userAlbums
            _alphabetAlbums = userAlbums.sortedBy { it.name }

            val userArtists = spotifyRepository.getUserFollowingArtists("Bearer $token").artists
            _userArtists.value = userArtists.items
            _recentArtists = userArtists.items
            _alphabetArtists = userArtists.items.sortedBy { it.name }
        }
    }

    fun addUserPlaylist(newPlaylist: PlaylistResponse) {
        val newSimplifiedPlaylist = SimplifiedPlaylistObject(
            collaborative = newPlaylist.collaborative,
            description = newPlaylist.description,
            external_url = newPlaylist.external_url,
            href = newPlaylist.href,
            id = newPlaylist.id,
            images = newPlaylist.images,
            name = newPlaylist.name,
            owner = newPlaylist.owner,
            public = newPlaylist.public,
            snapshot_id = newPlaylist.snapshot_id,
            tracks = Tracks(
                href = newPlaylist.tracks.href,
                total = newPlaylist.tracks.total
            ),
            type = newPlaylist.type,
            uri = newPlaylist.uri
        )
        val newPlaylists = _userPlaylists.value.orEmpty() + newSimplifiedPlaylist

        _recentPlaylists = newPlaylists.toList()
        _alphabetPlaylists = newPlaylists.sortedBy { it.name }
        _userPlaylists.value = if(_currentViewMode.value == LibraryViewMode.LIST) {
            _recentPlaylists
        } else {
            _alphabetPlaylists
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
            _userAlbums.value = _alphabetAlbums
            _userArtists.value = _alphabetArtists
        } else if(_currentSortMode.value == LibrarySortMode.ALPHABETICAL) {
            _currentSortMode.value = LibrarySortMode.RECENT
            _userPlaylists.value = _recentPlaylists
            _userAlbums.value = _recentAlbums
            _userArtists.value = _recentArtists
        }
    }

    fun changeCategory(category: FilterCategories) {
        _selectedCategory.value = category
    }
}