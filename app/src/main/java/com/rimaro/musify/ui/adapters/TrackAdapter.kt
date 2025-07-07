package com.rimaro.musify.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.domain.model.TrackObject
import com.rimaro.musify.R

class TrackAdapter(
    val onTrackClicked: (TrackObject) -> Unit
) : ListAdapter<TrackObject, TrackAdapter.TrackViewHolder>(DiffCallback()) {

    inner class TrackViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val trackName = itemView.findViewById<TextView>(R.id.track_name)

        fun bind(track: TrackObject, onTrackClicked: (TrackObject) -> Unit) {
            trackName.text = track.name
            itemView.setOnClickListener {
                onTrackClicked(track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onTrackClicked)
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