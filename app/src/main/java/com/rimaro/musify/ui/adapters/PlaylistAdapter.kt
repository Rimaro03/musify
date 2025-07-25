package com.rimaro.musify.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.R

class PlaylistAdapter (
    private val onPlaylistClick: (SimplifiedPlaylistObject) -> Unit = {},
    private val viewId: Int
): ListAdapter<SimplifiedPlaylistObject, RecyclerView.ViewHolder>(PlaylistDiffCallback()) {
    inner class PlaylistViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val playlistName = itemView.findViewById<TextView>(R.id.playlist_name)
        val playlistDesc = itemView.findViewById<TextView>(R.id.playlist_desc)
        val playlistImg = itemView.findViewById<ImageView>(R.id.library_playlist_icon)

        fun bind(playlist: SimplifiedPlaylistObject) {
            itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }

            playlistName.text = playlist.name
            val desc = "${playlist.owner.display_name} • ${playlist.tracks.total} tracks"
            playlistDesc.text = desc
            Glide.with(itemView.context)
                .load(playlist.images?.first()?.url?.toUri())
                .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                .error(androidx.media3.session.R.drawable.media3_icon_artist)
                .into(playlistImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewId, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlaylistViewHolder).bind(getItem(position))
    }

}