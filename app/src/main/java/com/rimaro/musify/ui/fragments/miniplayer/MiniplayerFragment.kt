package com.rimaro.musify.ui.fragments.miniplayer

import MiniplayerDragListener
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rimaro.musify.databinding.FragmentMiniplayerBinding
import com.rimaro.musify.utils.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.rimaro.musify.R

@AndroidEntryPoint
class MiniplayerFragment : Fragment() {

    @Inject
    lateinit var playbackManager: PlaybackManager

    private var _binding: FragmentMiniplayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MiniplayerViewModel by viewModels()

    private val updateProgressBarAction = Runnable { updateProgressBar() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiniplayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.miniplayerLayout.setOnClickListener {
            findNavController().navigate(R.id.playerFragment)
        }
        binding.miniplayerSwipeableArea.setOnClickListener {
            findNavController().navigate(R.id.playerFragment)
        }

        playbackManager.currentMediaItem.observe(viewLifecycleOwner) {
            if(it == null) {
                //.miniplayerLayout.visibility = View.GONE
            } else {
                binding.miniplayerSongTitle.text = it.mediaMetadata.title
                binding.miniplayerSongAuthor.visibility = View.VISIBLE
                binding.miniplayerSongAuthor.text = it.mediaMetadata.artist
                Glide.with(this)
                    .load(it.mediaMetadata.artworkUri)
                    .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                    .error(androidx.media3.session.R.drawable.media3_icon_artist)
                    .into(binding.miniplayerSongImage)
                binding.miniplayerLayout.visibility = View.VISIBLE
            }
        }

        playbackManager.currentTrackFollowed.observe(viewLifecycleOwner) {
            if(it) {
                binding.miniplayerLikeBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_filled)
            } else {
                binding.miniplayerLikeBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_unfilled)
            }
        }

        viewModel.playButtonState.observe(viewLifecycleOwner) {
            if(it == Player.STATE_READY) {
                binding.miniplayerPlayBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_pause)
            } else {
                binding.miniplayerPlayBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_play)
            }
        }

        binding.miniplayerPlayBtn.setOnClickListener { viewModel.togglePlayButton() }
        binding.miniplayerProgressBar.post(updateProgressBarAction)

        binding.miniplayerLikeBtn.setOnClickListener {
            if(playbackManager.currentTrackFollowed.value == true) {
                binding.miniplayerLikeBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_unfilled)
            } else {
                binding.miniplayerLikeBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_filled)
            }
            viewModel.toggleLikeButton()
        }

        // swiping animation
        @SuppressLint("ClickableViewAccessibility")
        binding.miniplayerSwipeableArea.setOnTouchListener(
            MiniplayerDragListener(
                onSwipeLeft = {viewModel.skipToNext()},
                onSwipeRight = {viewModel.skipToPrevious()}
            )
        )


    }

    fun updateProgressBar() {
        val currentPosition = viewModel.playbackPosition()
        val duration = viewModel.playbackDuration()

        if(duration > 0) {
            val progress = (currentPosition.toFloat() / duration * binding.miniplayerProgressBar.max).toInt()
            binding.miniplayerProgressBar.progress = progress
        }

        binding.miniplayerProgressBar.postDelayed(updateProgressBarAction, 500L)
    }
}