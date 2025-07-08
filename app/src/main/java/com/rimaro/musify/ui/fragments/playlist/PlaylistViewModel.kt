package com.rimaro.musify.ui.fragments.playlist

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.rimaro.musify.domain.model.TrackObject
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.NewPipeHelper
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
): ViewModel() {
    private val _userTopTracks: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val userTopTracks: LiveData<List<TrackObject>> = _userTopTracks

    var mediaController: MediaController? = null

    private val _urlJobs = mutableMapOf<String, Deferred<String?>>()
    private val _audioStreamURLs = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _playlistId: MutableLiveData<String> = MutableLiveData()

    fun setPlaylistId(playlistId: String) {
        _playlistId.value = playlistId
        retrieveUserTopTracks(playlistId)
    }

    fun retrieveUserTopTracks(playlistId: String) = viewModelScope.launch {
        // moving API requests off the main thread with withContext(Dispatchers.IO) to the IO thread
        val token = withContext(Dispatchers.IO) {
            spotifyTokenManager.retrieveAccessToken()
        }
        val tracks = withContext(Dispatchers.IO) {
            spotifyRepository
                .getPlaylistById("Bearer $token", playlistId)
                .tracks.items.filter { it.track != null }
        }
        // immediatly display songs
        _userTopTracks.value = tracks.map { it.track!! }

        // keeps child jobs cancellable with the parent
        coroutineScope {
            tracks.forEach { trackItem ->
                val track = trackItem.track!!
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
                val audioStreamURL = _audioStreamURLs.value[track.id] ?: error("No audio stream found for ${track.name}")

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
                mediaController?.setMediaItem(mediaItem)
                mediaController?.prepare()
                mediaController?.play()

            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: $e")
            }
        }
    }
}