package com.rimaro.musify.ui.fragments.playlist

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.domain.model.PlaylistLocal
import com.rimaro.musify.domain.model.UserLocal
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.PlaybackManager
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.collections.first

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val spotifyTokenManager: SpotifyTokenManager,
    private val playbackManager: PlaybackManager,
    @ApplicationContext private val context: Context
): ViewModel() {

    private lateinit var _mediaController: MediaController
    
    private val _trackList: MutableLiveData<List<TrackObject>> = MutableLiveData()
    val trackList: LiveData<List<TrackObject>> = _trackList
    
    private val _urlJobs = mutableMapOf<String, Deferred<String?>>()
    private val _audioStreamURLs = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _selectedPlaylistId: MutableLiveData<String> = MutableLiveData()

    private val _playlistData: MutableLiveData<PlaylistLocal> = MutableLiveData()
    val playlistData: LiveData<PlaylistLocal> = _playlistData

    val playingTrackId: LiveData<String> = playbackManager.playingTrackId

    private val _playButtonState: MutableLiveData<@Player.State Int> = MutableLiveData()
    var playButtonState: LiveData<@Player.State Int> = _playButtonState

    private val _shuffleEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val shuffleEnabled: LiveData<Boolean> = _shuffleEnabled

    private val _playlistFollowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val playlistFollowed: LiveData<Boolean> = _playlistFollowed

    private val _tracksFollowed : MutableLiveData<MutableMap<String, Boolean>> = MutableLiveData()
    val tracksFollowed: LiveData<MutableMap<String, Boolean>> = _tracksFollowed

    fun connectToSession(sessionToken: SessionToken, playlistId: String) {
        val controllerFuture = MediaController.Builder(
            context,
            sessionToken
        ).buildAsync()

        controllerFuture.addListener (
            {
                val controller = controllerFuture.get()
                controller.addListener(playbackManager.getPlaybackListener(::callbackIsPlayingChange, ::callbackPlayerStateChange))
                _mediaController = controller
                setPlaylistId(playlistId)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun setPlaylistId(playlistId: String) {
        _selectedPlaylistId.value = playlistId
        getPlaylist(playlistId)
        viewModelScope.launch {
             _playButtonState.value = updatePlayButtonState(_mediaController.isPlaying, _mediaController.playbackState)
            _shuffleEnabled.value = _mediaController.shuffleModeEnabled == true
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
        // immediately display songs
        _trackList.value = tracks
        getFollowedTrack(tracks)

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

    // ---------- TRACK FUNCTIONS ---------------
    // TODO: same issue here with streaming.let
    @OptIn(ExperimentalCoroutinesApi::class)
    fun playTrack(track: TrackObject){
        playbackManager.setPlayingPlaylistID(_selectedPlaylistId.value!!)
        _mediaController.clearMediaItems()
        addTracksToQueue(startIndex = _trackList.value!!.indexOf(track))
        _mediaController.prepare()
        _mediaController.play()
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
                _mediaController.addMediaItem(mediaItem)
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
            _mediaController.addMediaItem(mediaItem)
        }
    }

    fun playCurrentPlaylist() {
        _mediaController.clearMediaItems()
        addTracksToQueue()
        _mediaController.prepare()
        _mediaController.play()
    }

    // ---------- PLAYLIST BUTTONS FUNCTIONS -----------------
    fun callbackIsPlayingChange(isPlaying: Boolean) {
        _playButtonState.value = updatePlayButtonState(isPlaying, _mediaController.playbackState)
    }

    // TODO: add buffering state, add loading icon on player button
    fun callbackPlayerStateChange(playerState: @Player.State Int) {
        _playButtonState.value = updatePlayButtonState(_mediaController.isPlaying, playerState)
    }

    fun updatePlayButtonState(playerIsPlaying: Boolean,  playerState: @Player.State Int): @Player.State Int {
        if(playerIsPlaying == true && (_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) ) {
            return Player.STATE_READY
        } else if(playerState == Player.STATE_BUFFERING) {
            return Player.STATE_BUFFERING
        }
        return Player.STATE_IDLE
    }

    fun togglePlayButton() {
        if(_mediaController.isPlaying == true) {
            if(_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) {
                _mediaController.pause()
            } else {
                playbackManager.playingPlaylistId = _selectedPlaylistId
                playCurrentPlaylist()
            }
        } else {
            if(_selectedPlaylistId.value == playbackManager.playingPlaylistId.value) {
                _mediaController.play()
            } else {
                playbackManager.playingPlaylistId = _selectedPlaylistId
                playCurrentPlaylist()
            }
        }
    }

    fun toggleShuffle() {
        val newShuffleModeEnabled = !_mediaController.shuffleModeEnabled
        _shuffleEnabled.value = newShuffleModeEnabled
        if(newShuffleModeEnabled) {
            if (_mediaController.mediaItemCount > 0)
                _mediaController.removeMediaItems(1, _mediaController.mediaItemCount)
            addTracksToQueue()
            _mediaController.prepare()
        }
        _mediaController.shuffleModeEnabled = newShuffleModeEnabled
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

    fun toggleFollowTrack(track: TrackObject) {
        val newIsTrackFollowed = !(_tracksFollowed.value!![track.id])!!
        _tracksFollowed.value = _tracksFollowed.value!!.apply {
            this[track.id] = newIsTrackFollowed
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val token = spotifyTokenManager.retrieveAccessToken()
                if (newIsTrackFollowed == true) {
                    try{
                        spotifyRepository.followTrack("Bearer $token", track.id)
                    } catch(e: HttpException) {
                        Log.d("PlaylistViewModel", "Error following track: ${track.name}: ${e.message}")
                    }
                } else {
                    spotifyRepository.unfollowTrack("Bearer $token", track.id)
                }
            }
        }
    }

    // TODO: when chunking the track list, maybe it's better to run the api call async for every chunk
    private fun getFollowedTrack(tracks: List<TrackObject>) {
        val followed = mutableMapOf<String, Boolean>()
        if(_selectedPlaylistId.value == "-1") {
            for(track in tracks) {
                followed[track.id] = true
            }
            _tracksFollowed.value = followed
        } else {
            viewModelScope.launch {
                _tracksFollowed.value = withContext(Dispatchers.IO) {
                    val token = spotifyTokenManager.retrieveAccessToken()
                    val dividedTracks = tracks.chunked(50)
                    for(currTracks in dividedTracks){
                        val ids = currTracks.joinToString(",") { it.id }
                        val trackFollowed =
                            spotifyRepository.checkUserFollowsTracks("Bearer $token", ids)
                        for ((index, track) in currTracks.withIndex()) {
                            followed[track.id] = trackFollowed[index]
                        }
                    }
                    followed
                }
            }
        }
    }
}