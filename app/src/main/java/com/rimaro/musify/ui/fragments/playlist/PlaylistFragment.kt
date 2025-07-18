package com.rimaro.musify.ui.fragments.playlist

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.rimaro.musify.databinding.FragmentPlaylistBinding
import com.rimaro.musify.service.PlaybackService
import com.rimaro.musify.ui.adapters.TrackAdapter
import com.rimaro.musify.utils.TrackSwipeCallback
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
        val token = SessionToken(
            requireContext(),
            ComponentName(requireContext(), PlaybackService::class.java)
        )
        viewModel.connectToSession(token, args.playlistId)

        val trackRecyclerView = binding.trackRv
        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)

        // handles track swipe gesture
        fun handleTrackSwipe(position: Int) {
            trackRecyclerView.post {
                viewModel.enqueueTrack(position - 1)
            }
        }

        val itemTouchHelper = ItemTouchHelper(TrackSwipeCallback(::handleTrackSwipe, requireContext()))
        itemTouchHelper.attachToRecyclerView(trackRecyclerView)

        val trackAdapter = TrackAdapter (
            onTrackClicked = { viewModel.playTrack(it) },
            onTrackFavClicked = { viewModel.toggleFollowTrack(it) },
            onAddFavClicked = { viewModel.toggleFollowPlaylist() },
            onShuffleClicked = { viewModel.toggleShuffle() },
            onPlayButtonClicked = { viewModel.togglePlayButton() }
        )
        trackRecyclerView.adapter = trackAdapter

        // TODO: before loading playlist UI, wait for all these LiveData to fetch (short loading screen)
        viewModel.trackList.observe(viewLifecycleOwner) {
            trackAdapter.submitList(it)
        }
        viewModel.playlistData.observe(viewLifecycleOwner) {
            trackAdapter.setPlaylistData(it)
        }
        viewModel.playingTrackId.observe(viewLifecycleOwner) {
            trackAdapter.setCurrentTrackId(it)
        }
        viewModel.playButtonState.observe(viewLifecycleOwner) {
            trackAdapter.setPlayButtonState(it)
        }
        viewModel.shuffleEnabled.observe(viewLifecycleOwner) {
            trackAdapter.setShuffleMode(it)
        }
        viewModel.playlistFollowed.observe(viewLifecycleOwner) {
            trackAdapter.setPlaylistFollowed(it)
        }
        viewModel.tracksFollowed.observe(viewLifecycleOwner) {
            trackAdapter.setFollowedTracks(it)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}