package com.rimaro.musify.ui.fragments.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rimaro.musify.MyDownloader
import com.rimaro.musify.model.TrackObject
import com.rimaro.musify.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager
): ViewModel() {
    private val _userTopTracks: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val userTopTracks: LiveData<List<TrackObject>> = _userTopTracks

    var mediaController: MediaController? = null

    fun retrieveUserTopTracks() {
        viewModelScope.launch {
            var token = spotifyTokenManager.retrieveAccessToken()
            Log.d("HomeViewModel", "token: $token")
            _userTopTracks.value = spotifyRepository.getUserTopTracks("Bearer $token").items
        }
    }

    fun playTrack(trackName: String){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    NewPipe.init(MyDownloader())

                    val youtube = ServiceList.YouTube
                    val searchExtractor = youtube.getSearchExtractor(trackName)
                    searchExtractor.fetchPage()

                    val videoURL = searchExtractor.initialPage.items.firstOrNull()?.url
                    Log.d("MainActivity", "${searchExtractor.initialPage.items}")

                    if (videoURL != null) {
                        Log.d("MainActivity", "Found video: $videoURL")

                        val streamInfo = StreamInfo.getInfo(NewPipe.getServiceByUrl(videoURL), videoURL)
                        val audioStream: AudioStream? = streamInfo.audioStreams.firstOrNull()

                        audioStream?.let {
                            launch(Dispatchers.Main) {
                                Log.d("HomeViewModel", "Audio URL: ${it.url}")

                                val url = it.url.toString()
                                val mediaItem = MediaItem.fromUri(url)
                                mediaController?.setMediaItem(mediaItem)
                                mediaController?.prepare()
                                mediaController?.play()
                            }
                        }
                    }
                    else {
                        Log.d("HomeViewModel", "No video found")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: ${e}")
            }
        }
    }
}