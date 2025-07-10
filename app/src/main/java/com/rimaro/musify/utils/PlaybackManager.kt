package com.rimaro.musify.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController

class PlaybackManager {
    private val _playingTrackId = MutableLiveData<String>()
    var playingTrackId: LiveData<String> = _playingTrackId

    private val _playingPlaylistId = MutableLiveData<String>()
    var playingPlaylistId: LiveData<String> = _playingPlaylistId

    fun observePlayer(controller: MediaController, onPlayingStateChange: () -> Unit) {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _playingTrackId.value = mediaItem?.mediaId
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlayingStateChange()
            }
        })
    }
}