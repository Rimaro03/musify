package com.rimaro.musify.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rimaro.musify.utils.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ACTION_ADD_FAV = "ACTION_ADD_FAV"
private const val ACTION_REM_FAV = "ACTION_ADD_FAV"

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val addFavAction = SessionCommand(ACTION_ADD_FAV, Bundle.EMPTY)
    private val remFavAction = SessionCommand(ACTION_REM_FAV, Bundle.EMPTY)

    @Inject
    lateinit var playbackManager: PlaybackManager

    val addFavButton = CommandButton.Builder(CommandButton.ICON_HEART_UNFILLED)
        .setDisplayName("Add to favorites")
        .setSessionCommand(addFavAction)
        .build()

    val remFavButton = CommandButton.Builder(CommandButton.ICON_HEART_FILLED)
        .setDisplayName("Remove from favorites")
        .setSessionCommand(remFavAction)
        .build()

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                player.removeMediaItem(0)
            }
        })

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(Callback())
            .setMediaButtonPreferences(
                if(playbackManager.currentTrackFollowed.value == true) ImmutableList.of(remFavButton)
                else ImmutableList.of(addFavButton)
            )
            .build()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession?  = mediaSession

    private inner class Callback : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(addFavAction)
                        .add(remFavAction)
                        .build(),
                )
                .build()
        }

        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if(customCommand.customAction == ACTION_ADD_FAV) {
                session.setMediaButtonPreferences(ImmutableList.of(remFavButton))
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        // TODO: future work, add playback resumption even after app closed or device rebooted
        /*override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val settable = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()

        }*/
    }
}