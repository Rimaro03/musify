package com.rimaro.musify.domain.repository

import com.rimaro.musify.data.local.dao.AudioStreamUrlDAO
import com.rimaro.musify.data.local.entity.AudioStreamUrl
import com.rimaro.musify.data.remote.retrofit.SpotifyApiService
import com.rimaro.musify.data.remote.retrofit.SpotifyAuthService
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.utils.NewPipeHelper
import org.schabi.newpipe.extractor.stream.AudioStream
import javax.inject.Inject

class SpotifyRepository @Inject constructor(
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyApiService: SpotifyApiService,
    private val newPipeHelper: NewPipeHelper,
    private val audioStreamUrlDAO: AudioStreamUrlDAO
) {
    suspend fun getTrackAudioStreamURL(track: TrackObject): String? {
        // first check the database if track audio stream is already present, otherwise fetch it
        val audioStreamUrl = audioStreamUrlDAO.getAudioStreamUrl(track.id)
        if (audioStreamUrl != null && audioStreamUrl.streamUrl != null) {
            return audioStreamUrl.streamUrl
        }

        // ruffly takes 2-2.5 seconds for video url and audio stream, totalling 4s of waiting
        val videoUrl = newPipeHelper.getVideoUrl(track.name)
        var audioStream: AudioStream? = null
        videoUrl?.let {
            audioStream = newPipeHelper.getAudioStream(it)
        }
        val url = audioStream?.content?.toString()
        audioStreamUrlDAO.insert(
            AudioStreamUrl(
                trackId = track.id,
                streamUrl = url
            )
        )
        return url
    }

    // auth requests
    suspend fun getSpotifyToken(code: String, redirectUri: String, authorization: String) =
        spotifyAuthService.getToken(code = code, redirectUri = redirectUri, authorization = authorization)

    suspend fun refreshToken(refreshToken: String, clientId: String, authorization: String) =
        spotifyAuthService.refreshToken(refreshToken = refreshToken, clientId = clientId, authorization = authorization)

    // api requests
    suspend fun getUserProfile(authorization: String) =
        spotifyApiService.getUserProfile(authorization = authorization)

    suspend fun getUserTopTracks(authorization: String) =
        spotifyApiService.getUserTopTrack(authorization = authorization)

    suspend fun getUserSavedTracks(authorization: String) =
        spotifyApiService.getUserSavedTracks(authorization = authorization)

    suspend fun getUserPlaylists(authorization: String) =
        spotifyApiService.getUserPlaylists(authorization = authorization)

    suspend fun getPlaylistById(authorization: String, playlistId: String) =
        spotifyApiService.getPlaylist(authorization = authorization, playlistId = playlistId)

    suspend fun checkUserFollowsPlaylist(authorization: String, playlistId: String) =
        spotifyApiService.checkUserFollowsPlaylist(authorization = authorization, playlistId = playlistId)

    suspend fun followPlaylist(authorization: String, playlistId: String) =
        spotifyApiService.followPlaylist(authorization = authorization, playlistId = playlistId)

    suspend fun unfollowPlaylist(authorization: String, playlistId: String) =
        spotifyApiService.unfollowPlaylist(authorization = authorization, playlistId = playlistId)

    suspend fun checkUserFollowsTracks(authorization: String, trackIds: String) =
        spotifyApiService.checkUserFollowTracks(authorization = authorization, trackIds = trackIds)

    suspend fun followTrack(authorization: String, trackIds: String) =
        spotifyApiService.followTrack(authorization = authorization, trackIds = trackIds)

    suspend fun unfollowTrack(authorization: String, trackIds: String) =
        spotifyApiService.unfollowTrack(authorization = authorization, trackIds = trackIds)

}