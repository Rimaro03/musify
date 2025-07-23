package com.rimaro.musify.ui.fragments.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.databinding.FragmentLibraryBinding
import com.rimaro.musify.ui.adapters.PlaylistGridViewAdapter
import com.rimaro.musify.ui.adapters.PlaylistListViewAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.rimaro.musify.R

enum class LibraryViewMode {
    LIST,
    GRID
}

enum class LibrarySortMode {
    RECENT,
    ALPHABETICAL
}

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

        fun navigateAction(playlist: SimplifiedPlaylistObject) {
            val action = LibraryFragmentDirections.actionLibraryFragmentToPlaylistFragment(playlist.id)
            findNavController().navigate(action)
        }

        // change view mode
        val changeViewModeBtn = binding.libraryChangeViewBtn
        fun changeViewMode() {
            viewModel.changeViewMode()
        }
        changeViewModeBtn.setOnClickListener {
            changeViewMode()
        }

        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
        var adapter: ListAdapter<SimplifiedPlaylistObject, RecyclerView.ViewHolder> = PlaylistListViewAdapter(::navigateAction)
        trackRecyclerView.adapter = adapter
        viewModel.currentViewMode.observe(viewLifecycleOwner) {
            if(it == LibraryViewMode.LIST) {
                adapter = PlaylistListViewAdapter(::navigateAction)
                trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
                changeViewModeBtn.setImageResource(R.drawable.grid_view_24px)
            } else {
                adapter = PlaylistGridViewAdapter(::navigateAction)
                trackRecyclerView.layoutManager = GridLayoutManager(view.context, 3)
                changeViewModeBtn.setImageResource(R.drawable.list_24px)
            }
            trackRecyclerView.adapter = adapter
            viewModel.userPlaylists.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }

        //change sort mode
        val changeSortModeBtn = binding.libraryChangeSortBtn
        val sortText = binding.librarySortText
        fun changeSortMode() {
            viewModel.changeSortMode()
        }
        changeSortModeBtn.setOnClickListener {
            changeSortMode()
        }
        binding.libraryChangeSort.setOnClickListener {
            changeSortMode()
        }
        viewModel.currentSortMode.observe(viewLifecycleOwner) {
            if(it == LibrarySortMode.RECENT) {
                sortText.text = getString(R.string.order_recent)
            } else {
                sortText.text = getString(R.string.order_alphabet)
            }
        }

    }
}