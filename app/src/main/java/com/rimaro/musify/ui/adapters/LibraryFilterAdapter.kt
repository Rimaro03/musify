package com.rimaro.musify.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.R

enum class Category {
    ALBUMS,
    ARTISTS,
    PLAYLISTS,
}

class LibraryFilterAdapter (
    private val onFilterClick: (Category) -> Unit = {}
): RecyclerView.Adapter<LibraryFilterAdapter.LibraryFilterViewHolder>() {
    private val currentFilter = Category.PLAYLISTS

    inner class LibraryFilterViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val playlistFilter = itemView.findViewById<CardView>(R.id.library_playlist_filter)
        val albumFilter = itemView.findViewById<CardView>(R.id.library_album_filter)
        val artistFilter = itemView.findViewById<CardView>(R.id.library_artist_filter)

        fun bind() {
            if(currentFilter == Category.PLAYLISTS) {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast)))
            } else {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_secondaryContainer)))
            }
            playlistFilter.setOnClickListener {
                onFilterClick(Category.PLAYLISTS)
            }

            if(currentFilter == Category.PLAYLISTS) {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast)))
            } else {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_secondaryContainer)))
            }
            albumFilter.setOnClickListener {
                onFilterClick(Category.ALBUMS)
            }

            if(currentFilter == Category.PLAYLISTS) {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast)))
            } else {
                playlistFilter.setCardBackgroundColor(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_secondaryContainer)))
            }
            artistFilter.setOnClickListener {
                onFilterClick(Category.ARTISTS)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryFilterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.library_filtes, parent, false)
        return LibraryFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryFilterViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

}