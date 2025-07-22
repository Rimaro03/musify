package com.rimaro.musify.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.imageview.ShapeableImageView
import com.rimaro.musify.data.remote.model.TrackObject
import com.rimaro.musify.R
import com.rimaro.musify.domain.model.PlaylistLocal

//TODO: future idea, divide the header item in playlist metadata and action buttons
class TrackAdapter(
    val onTrackClicked: (TrackObject) -> Unit,
    val onTrackFavClicked: (TrackObject) -> Unit,
    val onAddFavClicked: () -> Unit,
    val onShuffleClicked: () -> Unit,
    val onPlayButtonClicked: () -> Unit
) : ListAdapter<TrackObject, RecyclerView.ViewHolder>(TrackDiffCallback()) {
    private var _playlistData: PlaylistLocal? = null
    private var playButtonState: @Player.State Int = Player.STATE_IDLE
    private var shuffleModeEnabled = false
    private var currentTrackId: String? = null
    private var followingPlaylist = false
    private var followedTracks: Map<String, Boolean> = mapOf()

    fun setPlaylistData(data: PlaylistLocal) {
        _playlistData = data
        notifyItemChanged(0)
    }

    fun setCurrentTrackId(id: String?) {
        currentTrackId = id
        notifyItemRangeChanged(0, currentList.size)
    }

    fun setPlayButtonState(newState: @Player.State Int) {
        playButtonState = newState
        notifyItemChanged(0)
    }

    fun setShuffleMode(enabled: Boolean) {
        shuffleModeEnabled = enabled
        notifyItemChanged(0)
    }

    fun setPlaylistFollowed(followed: Boolean) {
        followingPlaylist = followed
        notifyItemChanged(0)
    }

    fun setFollowedTracks(followed: Map<String, Boolean>) {
        followedTracks = followed
        notifyItemRangeChanged(0, currentList.size)
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
        val playBtn = itemView.findViewById<ShapeableImageView>(R.id.play_track_bnt)
        val loadingSpinner = itemView.findViewById<View>(R.id.loading_spinner)

        fun bind() {
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
            if(followingPlaylist) {
                addFavBtn.setImageResource(R.drawable.check_circle_24px)
                addFavBtn.imageTintList = ColorStateList
                    .valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.md_theme_tertiaryContainer_mediumContrast
                        )
                    )
            }
            else{
                addFavBtn.setImageResource(R.drawable.add_circle_24px)
                addFavBtn.imageTintList = null
            }

            shufflePlaylistBtn.setOnClickListener {
                onShuffleClicked()
            }
            if(shuffleModeEnabled)
                shufflePlaylistBtn.imageTintList = ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast))
            else
                shufflePlaylistBtn.imageTintList = null

            playBtn.setOnClickListener {
                onPlayButtonClicked()
                it.animateClick()
            }
            if(playButtonState == Player.STATE_READY) {
                playBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_pause)
                loadingSpinner.visibility = View.GONE
            }
            else if(playButtonState == Player.STATE_BUFFERING) {
                playBtn.setImageResource(0)
                playBtn.setImageDrawable(null)
                loadingSpinner.visibility = View.VISIBLE
            }
            else {
                playBtn.setImageResource(androidx.media3.session.R.drawable.media3_icon_play)
                loadingSpinner.visibility = View.GONE
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
        private val followTrackBtn = itemView.findViewById<ImageButton>(R.id.follow_track_btn)

        fun bind(track: TrackObject) {
            trackName.text = track.name
            if(track.id == currentTrackId) {
                trackName.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_theme_tertiaryContainer_mediumContrast))
            } else {
                trackName.setTextColor(-1)
            }

            Glide.with(itemView.context)
                .load(track.album.images[0].url)
                .placeholder(androidx.media3.session.R.drawable.media3_icon_album)
                .error(androidx.media3.session.R.drawable.media3_icon_album)
                .transform(RoundedCorners(15))
                .into(trackImg)

            trackArtists.text = track.artists.joinToString(", ") { it.name }

            clickToPlay.setOnClickListener {
                onTrackClicked(track)
            }

            followTrackBtn.setOnClickListener {
                onTrackFavClicked(track)
            }
            if(followedTracks[track.id] == true) {
                followTrackBtn.setImageResource(R.drawable.check_circle_24px)
                followTrackBtn.imageTintList = ColorStateList
                    .valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.md_theme_tertiaryContainer_mediumContrast
                        )
                    )
            } else {
                followTrackBtn.setImageResource(R.drawable.add_circle_24px)
                followTrackBtn.imageTintList = null
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
            holder.bind()
        } else if(holder is TrackViewHolder){
            holder.bind(getItem(position - 1))
        }
    }

    override fun getItemCount(): Int = currentList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }
}

class TrackDiffCallback: DiffUtil.ItemCallback<TrackObject>() {
    override fun areItemsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackObject, newItem: TrackObject): Boolean {
        return oldItem == newItem
    }
}