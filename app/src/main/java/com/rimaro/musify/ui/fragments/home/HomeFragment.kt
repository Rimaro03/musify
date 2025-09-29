package com.rimaro.musify.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rimaro.musify.R
import com.rimaro.musify.databinding.FragmentHomeBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.rimaro.musify.ui.fragments.auth.AuthFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()

        Log.d("HomeFragment", "onCreate: ${viewModel.checkAuthSaved()}")
        if(!viewModel.checkAuthSaved()) {
            navController.navigate(R.id.action_homeFragment_to_authFragment)
            return
        }

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(AuthFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry, Observer { success ->
                if(!success) {
                    navController.popBackStack(R.id.authFragment, true)
                }
            })

        viewModel.retrieveUserPlaylists()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<GridLayout>(R.id.pinned_playlist_grid)
        viewModel.userPlaylists.observe(viewLifecycleOwner) {
            val navigateAction = {
                val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment("-1")
                findNavController().navigate(action)
            }
            val card = LayoutInflater
                .from(requireContext())
                .inflate(R.layout.pinned_playlist_card, container, false)

            card.findViewById<TextView>(R.id.pinned_playlist_title)
                .text = requireContext().getString(R.string.liked_tracks)
            card.findViewById<ImageView>(R.id.pinned_playlist_icon)
                .setImageResource(androidx.media3.session.R.drawable.media3_icon_heart_filled)
            card.setOnClickListener { navigateAction() }
            container.addView(card)

            for(playlist in it.subList(0, 7)) {
                val navigateAction = {
                    val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(playlist.id)
                    findNavController().navigate(action)
                }

                val card = LayoutInflater
                    .from(requireContext())
                    .inflate(R.layout.pinned_playlist_card, container, false)

                Glide.with(requireContext())
                    .load(playlist.images?.first()?.url?.toUri())
                    .placeholder(androidx.media3.session.R.drawable.media3_icon_album)
                    .into(card.findViewById<ImageView>(R.id.pinned_playlist_icon))

                card.findViewById<TextView>(R.id.pinned_playlist_title).text = playlist.name
                card.setOnClickListener { navigateAction() }
                container.addView(card)
            }

            val navigateBtn = binding.navigateBtn
            navigateBtn.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}