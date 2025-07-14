package com.rimaro.musify.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.rimaro.musify.R
import dagger.hilt.android.qualifiers.ApplicationContext

class TrackSwipeCallback (
    private val onTrackSwiped: (Int) -> Unit,
    @ApplicationContext context: Context
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    private val swipeIcon = ContextCompat.getDrawable(context, R.drawable.queue_music_24px)
    private val swipeColor = ContextCompat.getColor(context, R.color.md_theme_tertiaryContainer_mediumContrast)
    private val background = ColorDrawable()

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // Not used for swipe
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if(direction == ItemTouchHelper.RIGHT) {
            onTrackSwiped(viewHolder.absoluteAdapterPosition)

            viewHolder.itemView.post {
                viewHolder.bindingAdapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)
            }
        }
    }

    // Optional: Customize background & icon (like Gmail)
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - (swipeIcon?.intrinsicHeight ?: 0)) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + (swipeIcon?.intrinsicHeight ?: 0)

        if(dX > 0) {
            background.color = swipeColor
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            background.draw(c)

            val iconLeft = itemView.left + iconMargin
            val iconRight = iconLeft + (swipeIcon?.intrinsicWidth ?: 0)
            swipeIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            swipeIcon?.draw(c)
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.2f
    }
}
