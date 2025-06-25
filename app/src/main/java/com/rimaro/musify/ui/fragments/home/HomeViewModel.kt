package com.rimaro.musify.ui.fragments.home

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.rimaro.musify.model.TrackObject
import com.rimaro.musify.repository.SpotifyRepository
import com.rimaro.musify.utils.NewPipeHelper
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.AudioStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
    private val newPipeHelper: NewPipeHelper
): ViewModel() {
    private val _userTopTracks: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val userTopTracks: LiveData<List<TrackObject>> = _userTopTracks

    var mediaController: MediaController? = null

    private val urlJobs = mutableMapOf<String, Deferred<AudioStream?>>()

    fun retrieveUserTopTracks() {
        viewModelScope.launch {
            var token = spotifyTokenManager.retrieveAccessToken()
            val topTracks = spotifyRepository.getUserTopTracks("Bearer $token").items
            // immediatly display songs
            _userTopTracks.value = topTracks

            // update song url as soon as available
            topTracks.forEach { track ->
                if (urlJobs.containsKey(track.id)) return@launch

                val job = viewModelScope.async(Dispatchers.IO) {
                    val videoUrl = newPipeHelper.getVideoUrl(track.name)
                    if (videoUrl == null) {
                        Log.d("HomeViewModel", "No video found for song ${track.name}")
                        return@async null
                    }
                    Log.d("HomeViewModel", "Track ready: ${track.name}")
                    newPipeHelper.getAudioStream(videoUrl)
                }
                urlJobs[track.id] = job
            }
        }
    }

    fun isUrlReady(track: TrackObject): Boolean {
        return urlJobs[track.id]?.isCompleted == true
    }

    // TODO: same issue here with streaming.let
    @OptIn(ExperimentalCoroutinesApi::class)
    fun playTrack(track: TrackObject){
        viewModelScope.launch {
            try {
                val job = urlJobs[track.id] ?: error("No job found for song ${track.name}")
                var audioStream = if(job.isCompleted) {
                    job.getCompleted()
                } else job.await()
                if (audioStream == null) {
                    Log.d("HomeViewModel", "No audio stream found for song ${track.name}")
                    return@launch
                }
                Log.d("HomeViewModel", "Audio URL: ${audioStream.content}")

                val url = audioStream.content.toString()
                val artists = track.artists.joinToString(", ") { it.name }
                val mediaItem = MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(url)
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
                Log.e("HomeViewModel", "Error: $e")
            }
        }
    }
}