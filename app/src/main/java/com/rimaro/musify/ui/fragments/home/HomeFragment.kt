package com.rimaro.musify.ui.fragments.home

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rimaro.musify.R
import com.rimaro.musify.databinding.FragmentHomeBinding
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.MoreExecutors
import com.rimaro.musify.model.TrackObject
import com.rimaro.musify.service.PlaybackService
import com.rimaro.musify.ui.adapters.TrackAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(
            requireContext(),
            ComponentName(requireContext(), PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(
            requireContext(),
            sessionToken
        ).buildAsync()

        controllerFuture.addListener (
            {
                val controller = controllerFuture.get()
                viewModel.mediaController = controller
            },
            MoreExecutors.directExecutor()
        )
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

        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val authCode = prefs.getString("auth_code", null)
        if(authCode == null) {
            findNavController().navigate(R.id.authFragment)
            return
        }

        Log.d("HomeFragment", "auth code: $authCode")

        binding.logoutBtn.setOnClickListener {
            prefs.edit { remove("auth_code") }
            findNavController().navigate(R.id.authFragment)
        }

        viewModel.retrieveUserTopTracks()
        val trackRecyclerView = binding.trackRv
        trackRecyclerView.layoutManager = LinearLayoutManager(view.context)
        fun onTrackClicked(track: TrackObject) {
            viewModel.playTrack(track)
        }
        val trackAdapter = TrackAdapter(::onTrackClicked)
        trackRecyclerView.adapter = trackAdapter
        viewModel.userTopTracks.observe(viewLifecycleOwner) {
            trackAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}