package com.rimaro.musify.ui.fragments.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.databinding.FragmentLibraryBinding
import com.rimaro.musify.ui.adapters.PlaylistAdapter
import com.rimaro.musify.ui.fragments.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.retrieveUserPlaylists()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trackRecyclerView = binding.playlistRv
        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)

        fun navigateAction(playlist: SimplifiedPlaylistObject) {
            val action = LibraryFragmentDirections.actionLibraryFragmentToPlaylistFragment(playlist.id)
            findNavController().navigate(action)
        }

        val playlistAdapter = PlaylistAdapter(::navigateAction)
        trackRecyclerView.adapter = playlistAdapter

        viewModel.userPlaylists.observe(viewLifecycleOwner) {
            playlistAdapter.submitList(it)
        }
    }
}