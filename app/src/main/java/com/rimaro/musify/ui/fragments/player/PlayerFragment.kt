package com.rimaro.musify.ui.fragments.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.rimaro.musify.databinding.FragmentPlayerBinding
import com.rimaro.musify.utils.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var playbackManager: PlaybackManager

    private val viewModel: PlayerViewModel by viewModels()

    private val updateProgressBarAction = Runnable { updateProgressBar() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playbackManager.currentMediaItem.observe(viewLifecycleOwner) {
            val currentTrackData = it?.mediaMetadata
            binding.playerTrackName.text = currentTrackData?.title
            binding.playerTrackArtist.text = currentTrackData?.artist
            Glide.with(this)
                .load(currentTrackData?.artworkUri)
                .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                .error(androidx.media3.session.R.drawable.media3_icon_artist)
                .into(binding.playerTrackImg)
        }

        // PLAYER BUTTON
        // TODO: also consider the "buffering" player state
        viewModel.isPlaying.observe(viewLifecycleOwner) {
            if(it) {
                binding.playerPlayBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_pause)
            } else {
                binding.playerPlayBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_play)
            }
        }
        binding.playerPlayBtn.setOnClickListener { viewModel.togglePlayButton()  }

        // SKIP NEXT/PREV
        binding.playerSkipNextBtn.setOnClickListener {
            viewModel.skipToNext()
        }
        // TODO: check if prev exists
        binding.playerSkipPrevBtn.setOnClickListener {
            viewModel.skipToPrev()
        }

        // FOLLOW BUTTON
        playbackManager.currentTrackFollowed.observe(viewLifecycleOwner) {
            if(it) {
                binding.playerAddFavBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_filled)
            } else {
                binding.playerAddFavBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_unfilled)
            }
        }

        // SEEKBAR
        var isUserSeeking = false
        binding.playerSeekbar.post(updateProgressBarAction)
        binding.playerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // do nothing
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.seekTo(it.progress)
                    isUserSeeking = false
                }
            }

        })

    }

    fun updateProgressBar() {
        val currentPosition = viewModel.playbackPosition()
        val duration = viewModel.playbackDuration()

        if(duration > 0) {
            val progress = (currentPosition.toFloat() / duration * binding.playerSeekbar.max).toInt()
            binding.playerSeekbar.progress = progress
        }

        binding.playerSeekbar.postDelayed(updateProgressBarAction, 500L)
    }
}