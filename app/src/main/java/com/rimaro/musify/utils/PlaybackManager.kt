package com.rimaro.musify.utils

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

    fun getPlaybackListener( updatePlayButtonStatus: () -> Unit): Player.Listener {
        return PlayerListener(updatePlayButtonStatus)
    }

    inner class PlayerListener( private val updatePlayButtonStatus: () -> Unit ) : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _playingTrackId.value = mediaItem?.mediaId
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayButtonStatus()
        }
    }
}