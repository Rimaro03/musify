package com.rimaro.musify.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.rimaro.musify.R
import com.rimaro.musify.data.remote.model.CreatePlaylistRequestBody
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.ui.fragments.library.LibraryViewModel
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PlaylistCreationDialog : DialogFragment() {
    @Inject
    lateinit var repository: SpotifyRepository
    @Inject
    lateinit var spotifyTokenManager: SpotifyTokenManager

    private val libraryViewModel: LibraryViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = layoutInflater.inflate(R.layout.playlist_creation_dialog, null)

            view.findViewById<MaterialButton>(R.id.playlist_create_btn).setOnClickListener {
                val name = view.findViewById<EditText>(R.id.playlist_name_et).text.toString()
                val description = view.findViewById<EditText>(R.id.playlist_desc_et).text.toString()

                val request = CreatePlaylistRequestBody(
                    name = name,
                    description = description,
                    public = true
                )
                lifecycleScope.launch {
                    val newPlaylist = withContext(Dispatchers.IO) {
                        val token = spotifyTokenManager.retrieveAccessToken()
                        val userId = repository.getUserProfile(authorization = "Bearer $token").id
                        repository.createPlaylist(
                            authorization = "Bearer $token",
                            userId = userId,
                            request = request
                        )
                    }
                    libraryViewModel.addUserPlaylist(newPlaylist)
                    dismiss()
                }
            }

            view.findViewById<MaterialButton>(R.id.playlist_cancel_btn).setOnClickListener {
                dismiss()
            }

            AlertDialog.Builder(it)
                .setView(view)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}