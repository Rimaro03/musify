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
import com.rimaro.musify.data.remote.model.Album

class AlbumAdapter (
    private val onPlaylistClick: (Album) -> Unit = {},
    private val viewId: Int
): ListAdapter<Album, RecyclerView.ViewHolder>(AlbumDiffCallback()) {
    inner class AlbumViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val albumName = itemView.findViewById<TextView>(R.id.album_name)
        val albumDesc = itemView.findViewById<TextView>(R.id.album_desc)
        val albumImg = itemView.findViewById<ImageView>(R.id.library_album_icon)

        fun bind(album: Album) {
            itemView.setOnClickListener {
                onPlaylistClick(album)
            }

            albumName.text = album.name
            val desc = album.artists.first().name
            albumDesc.text = desc
            Glide.with(itemView.context)
                .load(album.images.first().url.toUri())
                .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                .error(androidx.media3.session.R.drawable.media3_icon_artist)
                .into(albumImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewId, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AlbumViewHolder).bind(getItem(position))
    }

}