package com.rimaro.musify.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.rimaro.musify.domain.repository.SpotifyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaybackManager @Inject constructor(
    val spotifyRepository: SpotifyRepository,
    val spotifyTokenManager: SpotifyTokenManager
) {
    init {
        Log.d("PlaybackManager", "PlaybackManager initialized")
    }

    private val _currentMediaItem = MutableLiveData<MediaItem?>()
    val currentMediaItem = _currentMediaItem

    private val _currentTrackFollowed = MutableLiveData<Boolean>()
    var currentTrackFollowed: LiveData<Boolean> = _currentTrackFollowed

    private val _playingPlaylistId = MutableLiveData<String>()
    var playingPlaylistId: LiveData<String> = _playingPlaylistId


    fun setPlayingPlaylistID(newId: String) {
        _playingPlaylistId.value = newId
    }

    fun getPlaybackListener(
        updateIsPlaying: (Boolean) -> Unit,
        updatePlayerState: (Int) -> Unit): Player.Listener {
        return PlayerListener(updateIsPlaying, updatePlayerState)
    }

    inner class PlayerListener(
        private val updateIsPlaying: (Boolean) -> Unit,
        private val updatePlayerState: (Int) -> Unit) : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = mediaItem
            CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
                val token = spotifyTokenManager.retrieveAccessToken()
                val trackFollowed = withContext(Dispatchers.IO) {
                    spotifyRepository.checkUserFollowsTracks(
                        "Bearer $token",
                        mediaItem?.mediaId.toString()
                    )
                }
                _currentTrackFollowed.value = trackFollowed.first()
            }

        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState(playbackState)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateIsPlaying(isPlaying)
        }
    }
}