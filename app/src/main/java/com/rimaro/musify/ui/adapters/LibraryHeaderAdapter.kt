package com.rimaro.musify.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.R

enum class LibraryViewMode {
    GRID,
    LIST
}

class LibraryHeaderAdapter (
    private val onViewButtonClick: () -> Unit = {}
): RecyclerView.Adapter<LibraryHeaderAdapter.LibraryHeaderViewHolder>() {
    private val currentViewMode = LibraryViewMode.LIST

    inner class LibraryHeaderViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val changeViewBtn = itemView.findViewById<ImageButton>(R.id.library_change_view_btn)

        fun bind() {
            if (currentViewMode == LibraryViewMode.GRID) {
                changeViewBtn.setImageResource(R.drawable.list_24px)
            } else {
                changeViewBtn.setImageResource(R.drawable.grid_view_24px)
            }
            changeViewBtn.setOnClickListener {
                onViewButtonClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryHeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.library_header, parent, false)
        return LibraryHeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryHeaderViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

}