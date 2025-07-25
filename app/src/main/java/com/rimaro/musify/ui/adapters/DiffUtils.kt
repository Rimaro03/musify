package com.rimaro.musify.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.rimaro.musify.data.remote.model.Album
import com.rimaro.musify.data.remote.model.ArtistObject
import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.data.remote.model.TrackObject

class TrackDiffCallback: DiffUtil.ItemCallback<TrackObject>() {
    override fun areItemsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem == newItem
    }
}

class PlaylistDiffCallback: DiffUtil.ItemCallback<SimplifiedPlaylistObject>() {
    override fun areItemsTheSame(oldItem: SimplifiedPlaylistObject, newItem: SimplifiedPlaylistObject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SimplifiedPlaylistObject, newItem: SimplifiedPlaylistObject): Boolean {
        return oldItem == newItem
    }
}

class AlbumDiffCallback: DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem == newItem
    }
}

class ArtistDiffCallback: DiffUtil.ItemCallback<ArtistObject>() {
    override fun areItemsTheSame(oldItem: ArtistObject, newItem: ArtistObject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ArtistObject, newItem: ArtistObject): Boolean {
        return oldItem == newItem
    }
}