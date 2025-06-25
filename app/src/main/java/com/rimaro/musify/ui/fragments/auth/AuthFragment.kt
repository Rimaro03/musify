package com.rimaro.musify.ui.fragments.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.rimaro.musify.databinding.FragmentAuthBinding
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import androidx.core.content.edit

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var savedStateHandle: SavedStateHandle

    companion object {
        private const val CLIENT_ID = "7019f693da3b4af69de4573339b3445d"
        private const val REQUEST_CODE: Int = 1337
        private const val REDIRECT_URI: String = "musify://auth"

        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESFULL"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        binding.loginBtn.setOnClickListener {
            val builder = AuthorizationRequest.Builder(
                CLIENT_ID,
                AuthorizationResponse.Type.CODE,
                REDIRECT_URI
            )
            builder.setScopes(arrayOf("user-read-private", "user-library-read",
                "playlist-read-private", "playlist-read-collaborative", "playlist-modify-public", "playlist-modify-private",
                "user-follow-read", "user-top-read", "user-read-recently-played"))
            val request = builder.build()
            val intent = AuthorizationClient.createLoginActivityIntent(requireActivity(), request)

            loginLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)

        when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                // Handle successful login
                val code = response.code
                Log.d("SpotifyAuth", "Authorization code: $code")

                savedStateHandle[LOGIN_SUCCESSFUL] = true
                val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                prefs.edit { putString("auth_code", code) }

                findNavController().popBackStack()
            }

            AuthorizationResponse.Type.ERROR -> {
                Log.e("SpotifyAuth", "Error: ${response.error}")
            }

            else -> {
                Log.d("SpotifyAuth", "Login cancelled or unknown type")
            }
        }
    }
}