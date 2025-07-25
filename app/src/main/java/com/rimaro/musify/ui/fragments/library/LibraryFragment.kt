package com.rimaro.musify.ui.fragments.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.databinding.FragmentLibraryBinding
import com.rimaro.musify.ui.adapters.PlaylistAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.rimaro.musify.R
import com.rimaro.musify.ui.adapters.AlbumAdapter
import com.rimaro.musify.ui.adapters.ArtistAdapter

enum class LibraryViewMode {
    LIST,
    GRID
}

enum class LibrarySortMode {
    RECENT,
    ALPHABETICAL
}

enum class FilterCategories {
    PLAYLIST,
    ALBUM,
    ARTIST,
    PODCAST
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

        fun configurationChange(category: FilterCategories, viewMode: LibraryViewMode) {
            when(category) {
                FilterCategories.PLAYLIST -> {
                    val adapter = if(viewMode == LibraryViewMode.LIST) {
                        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
                        PlaylistAdapter(::navigateAction, R.layout.library_playlist_list_card)
                    } else {
                        trackRecyclerView.layoutManager = GridLayoutManager(view.context, 3)
                        PlaylistAdapter(::navigateAction, R.layout.library_playlist_grid_card)
                    }
                    trackRecyclerView.adapter = adapter
                    viewModel.userPlaylists.observe(viewLifecycleOwner) {
                        adapter.submitList(it)
                    }

                }
                FilterCategories.ALBUM -> {
                    val adapter = if(viewMode == LibraryViewMode.LIST) {
                        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
                        AlbumAdapter({}, R.layout.library_album_list_card)
                    } else {
                        trackRecyclerView.layoutManager = GridLayoutManager(view.context, 3)
                        AlbumAdapter({}, R.layout.library_album_grid_card)
                    }
                    trackRecyclerView.adapter = adapter
                    viewModel.userAlbums.observe(viewLifecycleOwner) {
                        adapter.submitList(it)
                    }
                }
                FilterCategories.ARTIST -> {
                    val adapter = if(viewMode == LibraryViewMode.LIST) {
                        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
                        ArtistAdapter({}, R.layout.library_artist_list_card)
                    } else {
                        trackRecyclerView.layoutManager = GridLayoutManager(view.context, 3)
                        ArtistAdapter({}, R.layout.library_artist_grid_card)
                    }
                    trackRecyclerView.adapter = adapter
                    viewModel.userArtists.observe(viewLifecycleOwner) {
                        adapter.submitList(it)
                    }
                }
                else -> {}
            }
        }

        // change view mode
        val changeViewModeBtn = binding.includedHeader.libraryChangeViewBtn
        changeViewModeBtn.setOnClickListener {
            viewModel.changeViewMode()
        }

        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
        trackRecyclerView.adapter = PlaylistAdapter(::navigateAction, R.layout.library_playlist_list_card)
        viewModel.currentViewMode.observe(viewLifecycleOwner) { newViewMode ->
            configurationChange(viewModel.selectedCategory.value!!, newViewMode)
        }

        //change sort mode
        val changeSortModeBtn = binding.includedHeader.libraryChangeSortBtn
        val sortText = binding.includedHeader.librarySortText
        changeSortModeBtn.setOnClickListener {
            viewModel.changeSortMode()
        }
        binding.includedHeader.libraryChangeSort.setOnClickListener {
            viewModel.changeSortMode()
        }
        viewModel.currentSortMode.observe(viewLifecycleOwner) {
            if(it == LibrarySortMode.RECENT) {
                sortText.text = getString(R.string.order_recent)
            } else {
                sortText.text = getString(R.string.order_alphabet)
            }
        }

        // category filters
        val filterButtons = listOf(
            binding.includedFilters.libraryPlaylistFilter,
            binding.includedFilters.libraryAlbumFilter,
            binding.includedFilters.libraryArtistFilter,
            binding.includedFilters.libraryPodcastFilter
        )

        filterButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.changeCategory(FilterCategories.entries[index])
            }
        }

        val defaultColor = filterButtons[1].cardBackgroundColor
        viewModel.selectedCategory.observe(viewLifecycleOwner) {
            for((index, button) in filterButtons.withIndex()){
                if (it.ordinal == index) {
                    button.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_tertiaryContainer_mediumContrast
                        )
                    )
                } else {
                    button.setCardBackgroundColor(
                        defaultColor
                    )
                }
            }
        }

        // change category
        viewModel.selectedCategory.observe(viewLifecycleOwner) { currentCategory ->
            configurationChange(currentCategory, viewModel.currentViewMode.value!!)
        }
    }
}