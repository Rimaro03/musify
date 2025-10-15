package com.rimaro.musify.ui.fragments.miniplayer

import android.content.Context
import android.util.Log
import androidx.media3.session.MediaController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.rimaro.musify.di.AppModule
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.PlaybackManager
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MiniplayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val mediaControllerProvider: AppModule.MediaControllerProvider,
    private val spotifyTokenManager: SpotifyTokenManager,
    private val spotifyRepository: SpotifyRepository,
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

    fun playbackPosition(): Long {
        return if(this::_mediaController.isInitialized) _mediaController.currentPosition
        else 0
    }


    fun playbackDuration(): Long {
        return if(this::_mediaController.isInitialized) _mediaController.duration
        else 0
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

    fun toggleLikeButton() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val token = spotifyTokenManager.retrieveAccessToken()
                val track = playbackManager.currentMediaItem.value
                if (playbackManager.currentTrackFollowed.value == false) {
                    try{
                        spotifyRepository.followTrack("Bearer $token", track!!.mediaId)
                        playbackManager.followCurrentTrack()
                    } catch(e: HttpException) {
                        Log.d("PlaylistViewModel", "Error following track: ${track!!.mediaMetadata.title}: ${e.message}")
                    }
                } else {
                    spotifyRepository.unfollowTrack("Bearer $token", track!!.mediaId)
                    playbackManager.unfollowCurrentTrack()
                }
            }
        }
    }
}