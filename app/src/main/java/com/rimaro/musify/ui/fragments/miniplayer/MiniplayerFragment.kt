package com.rimaro.musify.ui.fragments.miniplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rimaro.musify.databinding.FragmentMiniplayerBinding
import com.rimaro.musify.utils.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MiniplayerFragment : Fragment() {

    @Inject
    lateinit var playbackManager: PlaybackManager
    private var _binding: FragmentMiniplayerBinding? = null
    private val binding get() = _binding!!

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

        playbackManager.playingTrackId.observe(viewLifecycleOwner) {
            if(it == null) {
                binding.miniplayerSongTitle.text = "Nothing playing"
                binding.miniplayerSongAuthor.visibility = View.GONE
            } else {
                binding.miniplayerSongTitle.text = "Beautiful song"
                binding.miniplayerSongAuthor.visibility = View.VISIBLE
                binding.miniplayerSongAuthor.text = "Author"
            }
        }
    }
}