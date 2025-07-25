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
import com.rimaro.musify.R
import com.rimaro.musify.data.remote.model.ArtistObject

class ArtistAdapter (
    private val onPlaylistClick: (ArtistObject) -> Unit = {},
    private val viewId: Int
): ListAdapter<ArtistObject, RecyclerView.ViewHolder>(ArtistDiffCallback()) {
    inner class ArtistViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val artistName = itemView.findViewById<TextView>(R.id.artist_name)
        val artistImg = itemView.findViewById<ImageView>(R.id.library_artist_icon)

        fun bind(artist: ArtistObject) {
            itemView.setOnClickListener {
                onPlaylistClick(artist)
            }

            artistName.text = artist.name
            Glide.with(itemView.context)
                .load(artist.images.first().url.toUri())
                .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                .error(androidx.media3.session.R.drawable.media3_icon_artist)
                .into(artistImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewId, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ArtistViewHolder).bind(getItem(position))
    }

}