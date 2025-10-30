package com.rimaro.musify.ui.fragments.player

import android.content.Context
import android.util.Log
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

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playbackState = MutableLiveData<Int>()
    val playbackState: LiveData<Int> get() = _playbackState

    init {
        val future = mediaControllerProvider.controllerFuture()

        future.addListener (
            {
                val controller = future.get()
                _mediaController = controller
                controller.addListener(playbackManager.getPlaybackListener(::setIsPlaying, ::setPlaybackState))
                setIsPlaying(controller.isPlaying)
                setPlaybackState(controller.playbackState)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    // ---------- PLAYLIST BUTTONS FUNCTIONS -----------------
    fun setIsPlaying(isPlaying: Boolean) { _isPlaying.value = isPlaying }
    fun setPlaybackState(playbackState: Int) { _playbackState.value = playbackState }

    fun togglePlayButton() {
        if(_mediaController.isPlaying) _mediaController.pause()
        else _mediaController.play()
    }

    fun skipToNext() = _mediaController.seekToNextMediaItem()
    fun skipToPrev() = _mediaController.seekToPreviousMediaItem()

    fun playbackPosition(): Long {
        return if(this::_mediaController.isInitialized) _mediaController.currentPosition
        else 0
    }
    fun playbackDuration(): Long {
        return if(this::_mediaController.isInitialized) _mediaController.duration
        else 0
    }

    fun seekTo(progress: Int) {
        Log.d("PlayerViewModel", "seekTo: $progress, playback position: ${_mediaController.currentPosition}")
        _mediaController.seekTo(progress.toLong())
    }
}