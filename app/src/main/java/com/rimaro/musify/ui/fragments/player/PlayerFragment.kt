package com.rimaro.musify.ui.fragments.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
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
    }
}