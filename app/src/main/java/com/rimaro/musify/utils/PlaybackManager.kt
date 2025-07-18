package com.rimaro.musify.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

class PlaybackManager {
    private val _playingTrackId = MutableLiveData<String>()
    var playingTrackId: LiveData<String> = _playingTrackId

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
            _playingTrackId.value = mediaItem?.mediaId
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState(playbackState)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateIsPlaying(isPlaying)
        }
    }
}