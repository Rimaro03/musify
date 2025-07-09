package com.rimaro.musify.ui.fragments.playlist

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.MoreExecutors
import com.rimaro.musify.databinding.FragmentPlaylistBinding
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.service.PlaybackService
import com.rimaro.musify.ui.PlaybackViewmodel
import com.rimaro.musify.ui.adapters.TrackAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    private val playbackViewModel: PlaybackViewmodel by activityViewModels()
    val args: PlaylistFragmentArgs by navArgs()

    override fun onStart() {
        super.onStart()

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
                controller.shuffleModeEnabled = false
                viewModel.mediaController = controller
                playbackViewModel.observePlayer(controller)
            },
            MoreExecutors.directExecutor()
        )
    }

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
        viewModel.setPlaylistId(args.playlistId)

        val trackRecyclerView = binding.trackRv
        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
        fun onTrackClicked(track: TrackObject) {
            viewModel.playTrack(track)
        }
        val trackAdapter = TrackAdapter(
            ::onTrackClicked,
            onAddFavClicked = {},
            onShuffleClicked = { viewModel.toggleShuffle() },
            onPlayTrackClicked = { viewModel.playCurrentPlaylist() }
        )
        trackRecyclerView.adapter = trackAdapter
        viewModel.trackList.observe(viewLifecycleOwner) {
            trackAdapter.submitList(it)
        }
        viewModel.playlistData.observe(viewLifecycleOwner) {
            trackAdapter.setPlaylistData(it)
        }
        playbackViewModel.currentTrackId.observe(viewLifecycleOwner) {
            trackAdapter.setCurrentTrackId(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}