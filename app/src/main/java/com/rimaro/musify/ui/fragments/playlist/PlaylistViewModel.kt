package com.rimaro.musify.ui.fragments.playlist

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.domain.model.PlaylistLocal
import com.rimaro.musify.domain.model.UserLocal
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.PlaybackManager
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.first

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
    private val playbackManager: PlaybackManager
): ViewModel() {
    enum class PlayButtonState {
        PLAYING,
        STOPPED
    }

    private val _trackList: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val trackList: LiveData<List<TrackObject>> = _trackList

    private var _mediaController: MediaController? = null

    fun setMediaController(mediaController: MediaController) {
        _mediaController = mediaController
        playbackManager.observePlayer(mediaController, ::updatePlayButtonStatus)
    }

    private val _urlJobs = mutableMapOf<String, Deferred<String?>>()
    private val _audioStreamURLs = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _selectedPlaylistId: MutableLiveData<String> = MutableLiveData()

    private val _playlistData: MutableLiveData<PlaylistLocal> = MutableLiveData()
    val playlistData: LiveData<PlaylistLocal> = _playlistData

    val playingTrackId: LiveData<String> = playbackManager.playingTrackId

    private val _playButtonStatus: MutableLiveData<PlayButtonState> = MutableLiveData()
    var playButtonStatus: LiveData<PlayButtonState> = _playButtonStatus

    private val _shuffleEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val shuffleEnabled: LiveData<Boolean> = _shuffleEnabled

    private val _playlistFollowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val playlistFollowed: LiveData<Boolean> = _playlistFollowed

    fun setPlaylistId(playlistId: String) {
        _selectedPlaylistId.value = playlistId
        getPlaylist(playlistId)
        initPlaylist()
    }

    private fun initPlaylist() {
        viewModelScope.launch {
            _playButtonStatus.value = getPlayButtonStatus()
            _shuffleEnabled.value = _mediaController?.shuffleModeEnabled == true
            _playlistFollowed.value = checkIfPlaylistIsFollowed()
        }
    }

    // TODO: use the "next" response field to retrieve the next page of results when limit < total results (e.g. liked songs)
    fun getPlaylist(playlistId: String) = viewModelScope.launch {
        // moving API requests off the main thread with withContext(Dispatchers.IO) to the IO thread
        val token = withContext(Dispatchers.IO) {
            spotifyTokenManager.retrieveAccessToken()
        }
        var tracks: List<TrackObject>
        if(playlistId == "-1"){
            val playlist = withContext(Dispatchers.IO) {
                spotifyRepository.getUserSavedTracks("Bearer $token")
            }

            val playlistData = PlaylistLocal(
                name = "Liked Songs",
                imageUrl = null,
                owner = UserLocal(
                    displayName = "You",
                    iconUrl = null
                ),
                description = null
            )
            _playlistData.value = playlistData

            tracks = playlist.items
                .filter { it.track != null }
                .map { it.track!! }
        } else {
            val playlist = withContext(Dispatchers.IO) {
                spotifyRepository.getPlaylistById("Bearer $token", playlistId)
            }

            val playlistData = PlaylistLocal(
                name = playlist.name,
                imageUrl = playlist.images?.first()?.url,
                owner = UserLocal(
                    displayName = playlist.owner.display_name ?: "",
                    iconUrl = "" //TODO: to get iconUrl, make request to spotify API
                ),
                description = playlist.description
            )
            _playlistData.value = playlistData

            tracks = playlist.tracks.items
                .filter { it.track != null }
                .map { it.track!! }
        }
        // immediatly display songs
        _trackList.value = tracks

        // keeps child jobs cancellable with the parent
        coroutineScope {
            tracks.forEach { trackItem ->
                val track = trackItem
                // Skip if already downloading
                if (_urlJobs.containsKey(track.id)) return@forEach

                val job = async(Dispatchers.IO) {
                    spotifyRepository.getTrackAudioStreamURL(track)
                }
                _urlJobs[track.id] = job

                launch {
                    val audioStreamURL = job.await()
                    audioStreamURL?.let {
                        Log.d("PlaylistViewModel", "Audio URL: ${track.name} $audioStreamURL")
                        _audioStreamURLs.update { it + (track.id to audioStreamURL) }
                    }
                    _urlJobs.remove(track.id)
                }
            }
        }
    }

    // TODO: same issue here with streaming.let
    @OptIn(ExperimentalCoroutinesApi::class)
    fun playTrack(track: TrackObject){
        playbackManager.setPlayingPlaylistID(_selectedPlaylistId.value!!)
        _mediaController?.clearMediaItems()
        addTracksToQueue(startIndex = _trackList.value!!.indexOf(track))
        _mediaController?.prepare()
        _mediaController?.play()
    }

    fun togglePlayButton() {
        if(_mediaController?.isPlaying == true) {
            if(_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) {
                _mediaController?.pause()
                _playButtonStatus.value = PlayButtonState.STOPPED
            } else {
                playbackManager.playingPlaylistId = _selectedPlaylistId
                _playButtonStatus.value = PlayButtonState.PLAYING
                playCurrentPlaylist()
            }
        } else {
            _playButtonStatus.value = PlayButtonState.PLAYING
            if(_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) {
                _mediaController?.play()
            } else {
                playbackManager.playingPlaylistId = _selectedPlaylistId
                playCurrentPlaylist()
            }
        }
    }

    fun playCurrentPlaylist() {
        _mediaController?.clearMediaItems()
        addTracksToQueue()
        _mediaController?.prepare()
        _mediaController?.play()
    }

    // insert all the tracks from startIndex to end into the queue
    fun addTracksToQueue(startIndex: Int = 0) {
        viewModelScope.launch {
            val tracks = _trackList.asFlow()
                .first { !it.isNullOrEmpty() }
                .subList(startIndex, _trackList.value!!.size)

            for(track in tracks) {
                val audioStreamURL = _audioStreamURLs.asStateFlow()
                    .filter { it.containsKey(track.id) }
                    .first()[track.id]

                val artists = track.artists.joinToString(", ") { it.name }
                val mediaItem = MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(audioStreamURL)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(artists)
                            .setTitle(track.name)
                            .setArtworkUri(track.album.images.first().url.toUri())
                            .build()
                    )
                    .build()
                _mediaController?.addMediaItem(mediaItem)
            }
        }
    }

    fun enqueueTrack(position: Int) {
        val track = _trackList.value!![position]
        Log.d("PlaylistViewModel", "Enqueuing track: ${track.name}")
        viewModelScope.launch {
            val audioStreamURL = _audioStreamURLs.asStateFlow()
                .filter { it.containsKey(track.id) }
                .first()[track.id]

            val artists = track.artists.joinToString(", ") { it.name }
            val mediaItem = MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(audioStreamURL)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(artists)
                        .setTitle(track.name)
                        .setArtworkUri(track.album.images.first().url.toUri())
                        .build()
                )
                .build()
            _mediaController?.addMediaItem(mediaItem)
        }
    }

    fun getPlayButtonStatus(): PlayButtonState {
        return if(_mediaController?.isPlaying == true) {
            if(_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) {
                PlayButtonState.PLAYING
            } else {
                PlayButtonState.STOPPED
            }
        } else {
            PlayButtonState.STOPPED
        }
    }

    fun updatePlayButtonStatus() {
        _playButtonStatus.value = getPlayButtonStatus()
    }

    fun toggleShuffle() {
        val newShuffleModeEnabled = !_mediaController?.shuffleModeEnabled!!
        _shuffleEnabled.value = newShuffleModeEnabled
        if(newShuffleModeEnabled) {
            if (_mediaController?.mediaItemCount!! > 0)
                _mediaController?.removeMediaItems(1, _mediaController?.mediaItemCount!!)
            addTracksToQueue()
            _mediaController?.prepare()
        }
        _mediaController?.shuffleModeEnabled = newShuffleModeEnabled
    }

    // TODO: need a way to advice the home pinned playlist if a new playlist is unfollowed/followed
    fun toggleFollowPlaylist() {
        val newIsPlaylistFollowed = !_playlistFollowed.value!!
        _playlistFollowed.value = newIsPlaylistFollowed
        if(newIsPlaylistFollowed) {
            viewModelScope.launch {
                val token = spotifyTokenManager.retrieveAccessToken()
                spotifyRepository.followPlaylist("Bearer $token", _selectedPlaylistId.value!!)
            }
        } else {
            viewModelScope.launch {
                val token = spotifyTokenManager.retrieveAccessToken()
                spotifyRepository.unfollowPlaylist("Bearer $token", _selectedPlaylistId.value!!)
            }
        }
    }

    private suspend fun checkIfPlaylistIsFollowed(): Boolean {
        if(_selectedPlaylistId.value!! == "-1") return true
        return withContext(Dispatchers.IO) {
            val token = spotifyTokenManager.retrieveAccessToken()
            spotifyRepository.checkUserFollowsPlaylist("Bearer $token", _selectedPlaylistId.value!!)[0]
        }
    }
}