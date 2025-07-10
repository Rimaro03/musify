package com.rimaro.musify.ui.fragments.playlist

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.MoreExecutors
import com.rimaro.musify.databinding.FragmentPlaylistBinding
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.service.PlaybackService
import com.rimaro.musify.ui.adapters.TrackAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    val args: PlaylistFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val controllerFuture = MediaController.Builder(
            requireContext(),
            SessionToken(
                requireContext(),
                ComponentName(requireContext(), PlaybackService::class.java)
            )
        ).buildAsync()

        controllerFuture.addListener (
            {
                val controller = controllerFuture.get()
                viewModel.setMediaController(controller)
                viewModel.setPlaylistId(args.playlistId)
            },
            MoreExecutors.directExecutor()
        )

        val trackRecyclerView = binding.trackRv
        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)

        fun onTrackClicked(track: TrackObject) = viewModel.playTrack(track)
        val trackAdapter = TrackAdapter(
            ::onTrackClicked,
            onAddFavClicked = {},
            onShuffleClicked = { viewModel.toggleShuffle() },
            onPlayButtonClicked = { viewModel.togglePlayButton() }
        )
        trackRecyclerView.adapter = trackAdapter
        viewModel.trackList.observe(viewLifecycleOwner) {
            trackAdapter.submitList(it)
        }
        viewModel.playlistData.observe(viewLifecycleOwner) {
            trackAdapter.setPlaylistData(it)
        }
        viewModel.playingTrackId.observe(viewLifecycleOwner) {
            trackAdapter.setCurrentTrackId(it)
        }
        viewModel.playButtonStatus.observe(viewLifecycleOwner) {
            Log.d("PlaylistFragment", "Status: $it")
            trackAdapter.setPlayButtonState(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}