package com.rimaro.musify.ui.fragments.playlist

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.domain.model.PlaylistLocal
import com.rimaro.musify.domain.model.UserLocal
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
): ViewModel() {
    private val _trackList: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val trackList: LiveData<List<TrackObject>> = _trackList

    var mediaController: MediaController? = null

    private val _urlJobs = mutableMapOf<String, Deferred<String?>>()
    private val _audioStreamURLs = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _playlistId: MutableLiveData<String> = MutableLiveData()

    private val _playlistData: MutableLiveData<PlaylistLocal> = MutableLiveData()
    val playlistData: LiveData<PlaylistLocal> = _playlistData


    fun setPlaylistId(playlistId: String) {
        _playlistId.value = playlistId
        getPlaylist(playlistId)
    }

    // TODO: use the "next" response field to retrieve the next page of results when limit < total results (e.g. liked songs)
    fun getPlaylist(playlistId: String) = viewModelScope.launch {
        // moving API requests off the main thread with withContext(Dispatchers.IO) to the IO thread
        val token = withContext(Dispatchers.IO) {
            spotifyTokenManager.retrieveAccessToken()
        }
        var tracks: List<TrackObject>
        if(playlistId == "-1"){
            val playlist = withContext(Dispatchers.IO) {
                spotifyRepository.getUserSavedTracks("Bearer $token")
            }

            val playlistData = PlaylistLocal(
                name = "Liked Songs",
                imageUrl = null,
                owner = UserLocal(
                    displayName = "You",
                    iconUrl = null
                ),
                description = null
            )
            _playlistData.value = playlistData

            tracks = playlist.items
                .filter { it.track != null }
                .map { it.track!! }
        } else {
            val playlist = withContext(Dispatchers.IO) {
                spotifyRepository.getPlaylistById("Bearer $token", playlistId)
            }

            val playlistData = PlaylistLocal(
                name = playlist.name,
                imageUrl = playlist.images?.first()?.url,
                owner = UserLocal(
                    displayName = playlist.owner.display_name ?: "",
                    iconUrl = "" //TODO: to get iconUrl, make request to spotify API
                ),
                description = playlist.description
            )
            _playlistData.value = playlistData

            tracks = playlist.tracks.items
                .filter { it.track != null }
                .map { it.track!! }
        }
        // immediatly display songs
        _trackList.value = tracks

        // keeps child jobs cancellable with the parent
        coroutineScope {
            tracks.forEach { trackItem ->
                val track = trackItem
                // Skip if already downloading
                if (_urlJobs.containsKey(track.id)) return@forEach

                val job = async(Dispatchers.IO) {
                    spotifyRepository.getTrackAudioStreamURL(track)
                }
                _urlJobs[track.id] = job

                launch {
                    val audioStreamURL = job.await()
                    audioStreamURL?.let {
                        Log.d("PlaylistViewModel", "Audio URL: ${track.name} $audioStreamURL")
                        _audioStreamURLs.update { it + (track.id to audioStreamURL) }
                    }
                    _urlJobs.remove(track.id)
                }
            }
        }
    }

    fun isUrlReady(track: TrackObject): Boolean {
        return _audioStreamURLs.value.containsKey(track.id)
    }

    // TODO: same issue here with streaming.let
    @OptIn(ExperimentalCoroutinesApi::class)
    fun playTrack(track: TrackObject){
        viewModelScope.launch {
            try {
                if (!isUrlReady(track)) {
                    Log.d("PlaylistViewModel", "Waiting for URL...")
                    return@launch
                }
                val audioStreamURL = _audioStreamURLs.asStateFlow()
                    .filter { it.containsKey(track.id) }
                    .first()[track.id]

                val artists = track.artists.joinToString(", ") { it.name }
                val mediaItem = MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(audioStreamURL)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(artists)
                            .setTitle(track.name)
                            .setArtworkUri(track.album.images.first().url.toUri())
                            .build()
                    )
                    .build()
                mediaController?.addMediaItem(mediaItem)
                mediaController?.prepare()
                mediaController?.play()

            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: $e")
            }
        }
    }

    // TODO: handle different cases: player not playing, playing another playlist, playing a single song etc...
    fun togglePlaylistPlayButton() {}

    fun playCurrentPlaylist() {
        viewModelScope.launch {
            val tracks = _trackList.asFlow()
                .first { !it.isNullOrEmpty() }

            for(track in tracks) {
                val audioStreamURL = _audioStreamURLs.asStateFlow()
                    .filter { it.containsKey(track.id) }
                    .first()[track.id]

                val artists = track.artists.joinToString(", ") { it.name }
                val mediaItem = MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(audioStreamURL)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(artists)
                            .setTitle(track.name)
                            .setArtworkUri(track.album.images.first().url.toUri())
                            .build()
                    )
                    .build()
                mediaController?.addMediaItem(mediaItem)
                mediaController?.prepare()
                mediaController?.play()
            }
        }
    }

    fun toggleShuffle() {
        val newShuffleModeEnabled = !mediaController?.shuffleModeEnabled!!
        mediaController?.shuffleModeEnabled = newShuffleModeEnabled
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PlaylistViewModel", "onCleared")
    }
}