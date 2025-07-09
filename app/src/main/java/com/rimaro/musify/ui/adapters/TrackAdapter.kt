package com.rimaro.musify.ui.adapters

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.imageview.ShapeableImageView
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.R
import com.rimaro.musify.domain.model.PlaylistLocal

class TrackAdapter(
    val onTrackClicked: (TrackObject) -> Unit,
    val onAddFavClicked: () -> Unit,
    val onShuffleClicked: () -> Unit,
    val onPlayTrackClicked: () -> Unit
) : ListAdapter<TrackObject, RecyclerView.ViewHolder>(DiffCallback()) {
    private var _playlistData: PlaylistLocal? = null
    private var playerIsPlaying = false
    private var shuffleModeEnabled = false
    private var currentTrackId: String? = null

    fun setPlaylistData(data: PlaylistLocal) {
        _playlistData = data
        notifyItemChanged(0)
    }

    fun setCurrentTrackId(id: String?) {
        currentTrackId?.let {
            notifyItemChanged(getTrackIndexById(it))
        }
        val updateIndex = if(id == null) {
            getTrackIndexById(currentTrackId!!)
        } else {
            getTrackIndexById(id)
        }
        currentTrackId = id
        if(updateIndex != -1) {
            notifyItemChanged(updateIndex)
        } else {
            notifyItemRangeChanged(1, currentList.size)
        }
    }

    fun View.animateClick() {
        this.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    inner class HeaderViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val playlistImage = itemView.findViewById<ImageView>(R.id.playlist_image_iv)
        val playlistTitle = itemView.findViewById<TextView>(R.id.playlist_title_tv)
        val playlistAuthor = itemView.findViewById<TextView>(R.id.playlist_author_name_tv)
        val playlistAuthorIcon = itemView.findViewById<ImageView>(R.id.playlist_author_icon_siv)

        val addFavBtn = itemView.findViewById<ImageButton>(R.id.add_fav_btn)
        val shufflePlaylistBtn = itemView.findViewById<ImageButton>(R.id.shuffle_playlist_btn)
        val playTrackBtn = itemView.findViewById<ShapeableImageView>(R.id.play_track_bnt)

        fun bind(onAddFavClicked: () -> Unit,
                 onShuffleClicked: () -> Unit,
                 onPlayTrackClicked: () -> Unit) {
            playlistAuthor.text = _playlistData?.owner?.displayName ?: "Owner not available"
            playlistTitle.text = _playlistData?.name ?: "Playlist name not available"
            Glide.with(itemView.context)
                .load(_playlistData?.imageUrl)
                .placeholder(androidx.media3.session.R.drawable.media3_icon_album)
                .error(androidx.media3.session.R.drawable.media3_icon_album)
                .into(playlistImage)
            Glide.with(itemView.context)
                .load(_playlistData?.owner?.iconUrl)
                .placeholder(androidx.media3.session.R.drawable.media3_icon_artist)
                .error(androidx.media3.session.R.drawable.media3_icon_artist)
                .into(playlistAuthorIcon)

            addFavBtn.setOnClickListener {
                onAddFavClicked()
            }
            shufflePlaylistBtn.setOnClickListener {
                onShuffleClicked()
                shuffleModeEnabled = !shuffleModeEnabled
                if(shuffleModeEnabled)
                    shufflePlaylistBtn.imageTintList = ColorStateList
                        .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast))
                else
                    shufflePlaylistBtn.imageTintList = null
            }
            playTrackBtn.setOnClickListener {
                onPlayTrackClicked()
                it.animateClick()
                playerIsPlaying = !playerIsPlaying
                if(playerIsPlaying)
                    playTrackBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_pause)
                else
                    playTrackBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_play)
            }
        }
    }

    inner class TrackViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val trackName = itemView.findViewById<TextView>(R.id.track_name)
        private val trackImg = itemView.findViewById<ImageView>(R.id.track_icon)
        private val trackArtists = itemView.findViewById<TextView>(R.id.track_artists)
        private val clickToPlay = itemView.findViewById<LinearLayout>(R.id.click_to_play_zone)

        fun bind(track: TrackObject, onTrackClicked: (TrackObject) -> Unit) {
            trackName.text = track.name
            val defaultColor = trackName.currentTextColor
            if(track.id == currentTrackId) {
                trackName.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast))
            } else {
                trackName.setTextColor(defaultColor)
            }
            trackArtists.text = track.artists.joinToString(", ") { it.name }
            Glide.with(itemView.context)
                .load(track.album.images[0].url)
                .placeholder(androidx.media3.session.R.drawable.media3_icon_album)
                .error(androidx.media3.session.R.drawable.media3_icon_album)
                .transform(RoundedCorners(15))
                .into(trackImg)
            clickToPlay.setOnClickListener {
                onTrackClicked(track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if(viewType == VIEW_TYPE_HEADER) {
            val view = inflater.inflate(R.layout.playlist_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.track_item, parent, false)
            TrackViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is HeaderViewHolder) {
            holder.bind(onAddFavClicked, onShuffleClicked, onPlayTrackClicked)
        } else if(holder is TrackViewHolder){
            holder.bind(getItem(position - 1), onTrackClicked)
        }
    }

    override fun getItemCount(): Int = currentList.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    fun getTrackIndexById(id: String): Int {
        for (i in 0 until currentList.size) {
            Log.d("TrackAdapter", "comparing $id with ${currentList[i].id}")
            if (currentList[i].id == id) {
                return i + 1
            }
        }
        return -1
    }
}

class DiffCallback: DiffUtil.ItemCallback<TrackObject>() {
    override fun areItemsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem == newItem
    }
}