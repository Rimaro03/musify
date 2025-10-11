package com.rimaro.musify.ui.fragments.player

import android.content.Context
import androidx.media3.session.MediaController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import com.rimaro.musify.di.AppModule
import com.rimaro.musify.utils.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val mediaControllerProvider: AppModule.MediaControllerProvider,
    @ApplicationContext private val context: Context
): ViewModel() {
    private lateinit var _mediaController: MediaController

    private val _playButtonState: MutableLiveData<@Player.State Int> = MutableLiveData()
    var playButtonState: LiveData<@Player.State Int> = _playButtonState

    init {
        val future = mediaControllerProvider.controllerFuture()

        future.addListener (
            {
                val controller = future.get()
                _mediaController = controller
                controller.addListener(playbackManager.getPlaybackListener(::callbackIsPlayingChange, ::callbackPlayerStateChange))
                updatePlayButtonState(controller.isPlaying, controller.playbackState)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun updatePlayButtonState(playerIsPlaying: Boolean,  playerState: @Player.State Int): @Player.State Int {
        if(playerIsPlaying) {
            return Player.STATE_READY
        } else if(playerState == Player.STATE_BUFFERING) {
            return Player.STATE_BUFFERING
        }
        return Player.STATE_IDLE
    }

    // ---------- PLAYLIST BUTTONS FUNCTIONS -----------------
    fun callbackIsPlayingChange(isPlaying: Boolean) {
        _playButtonState.value = updatePlayButtonState(isPlaying, _mediaController.playbackState)
    }

    // TODO: add buffering state, add loading icon on player button
    fun callbackPlayerStateChange(playerState: @Player.State Int) {
        _playButtonState.value = updatePlayButtonState(_mediaController.isPlaying, playerState)
    }

    fun togglePlayButton() {
        if(_mediaController.isPlaying) _mediaController.pause()
        else _mediaController.play()
    }
}