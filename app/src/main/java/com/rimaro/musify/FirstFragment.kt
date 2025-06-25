package com.rimaro.musify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.navigation.fragment.findNavController
import com.rimaro.musify.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var notificationManager: NotificationManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                "media",
                "media",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Media"
                setAllowBubbles(true)
            }
        )

        val query = "save your tears"
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                NewPipe.init(MyDownloader())

                val youtube = ServiceList.YouTube
                val searchExtractor = youtube.getSearchExtractor(query)
                searchExtractor.fetchPage()

                val videoURL = searchExtractor.initialPage.items.firstOrNull()?.url
                Log.d("MainActivity", "${searchExtractor.initialPage.items}")

                if (videoURL != null) {
                    Log.d("MainActivity", "Found video: $videoURL")

                    val streamInfo = StreamInfo.getInfo(NewPipe.getServiceByUrl(videoURL), videoURL)
                    val audioStream: AudioStream? = streamInfo.audioStreams.firstOrNull()

                    audioStream?.let {
                        launch(Dispatchers.Main) {
                            Log.d("MainActivity", "Audio URL: ${it.url}")

                            playAudio(it.url.toString())
                        }
                    }
                }
                else {
                    Log.d("MainActivity", "No video found")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error: ${e.message}")
            }
        }

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class) private fun playAudio(url: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        mediaSession = MediaSession.Builder(requireContext(), player).build()
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        val notification = NotificationCompat.Builder(requireContext(), "media")
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            // Add media control buttons that invoke intents in your media service
            // Apply the media style template.
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
                    .setShowActionsInCompactView(1 /* #1: pause button \*/))

        notificationManager.notify(1, notification.build())
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) {
            player.release()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}