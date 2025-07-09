package com.rimaro.musify.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaybackViewmodel @Inject constructor() : ViewModel() {
    private var _currentTrackId = MutableLiveData<String>()
    var currentTrackId: LiveData<String> = _currentTrackId

    fun observePlayer(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentTrackId.value = mediaItem?.mediaId
            }
        })
    }
}