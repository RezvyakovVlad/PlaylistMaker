package com.example.playlistmaker.presentation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import kotlin.math.roundToInt

class TrackViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val ivArtwork: ImageView = itemView.findViewById(R.id.artwork_image)
    private val tvTrackName: TextView = itemView.findViewById(R.id.track_name)
    private val tvArtistName: TextView = itemView.findViewById(R.id.artist_name)
    private val tvTrackTime: TextView = itemView.findViewById(R.id.track_time)

    fun bind(track: Track) {
        tvTrackName.text = track.getSafeTrackName()
        tvArtistName.text = track.getSafeArtistName()
        tvTrackTime.text = track.getFormattedTrackTime()

        val cornerRadius = 8.dpToPx(itemView.context)

        val imageUrl = track.artworkUrl100
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(itemView)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .transform(RoundedCorners(cornerRadius))
                        .error(R.drawable.ic_placeholder)
                        .placeholder(R.drawable.ic_placeholder)
                )
                .into(ivArtwork)
        } else {
            ivArtwork.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).roundToInt()
    }
}