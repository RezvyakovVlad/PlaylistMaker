package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var placeholderTrack: ImageView
    private lateinit var trackName: TextView
    private lateinit var trackSinger: TextView
    private lateinit var addTrack: ImageButton
    private lateinit var playTrack: ImageButton
    private lateinit var favoriteTrack: ImageButton
    private lateinit var timeTrack: TextView
    private lateinit var timeTrackInfo: TextView
    private lateinit var albumTrackInfo: TextView
    private lateinit var yearTrackInfo: TextView
    private lateinit var genreTrackInfo: TextView
    private lateinit var countryTrackInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        initViews()
        setupToolbar()
        setupTrackData()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.back_button)
        placeholderTrack = findViewById(R.id.placeholder_track)
        trackName = findViewById(R.id.track_name)
        trackSinger = findViewById(R.id.track_singer)
        addTrack = findViewById(R.id.add_track)
        playTrack = findViewById(R.id.play_track)
        favoriteTrack = findViewById(R.id.favorite_track)
        timeTrack = findViewById(R.id.time_track)
        timeTrackInfo = findViewById(R.id.time_track_info)
        albumTrackInfo = findViewById(R.id.album_track_info)
        yearTrackInfo = findViewById(R.id.year_track_info)
        genreTrackInfo = findViewById(R.id.genre_track_info)
        countryTrackInfo = findViewById(R.id.country_track_info)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTrackData() {
        val track = intent.getSerializableExtra("TRACK") as? Track
        track?.let {
            if (!it.artworkUrl100.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.getCoverArtwork())
                    .placeholder(R.drawable.ic_placeholder_312)
                    .error(R.drawable.ic_placeholder_312)
                    .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius))) // Используйте dimension ресурс
                    .into(placeholderTrack)
            } else {
                placeholderTrack.setImageResource(R.drawable.ic_placeholder_312)
            }

            trackName.text = it.getSafeTrackName()
            trackSinger.text = it.getSafeArtistName()

            val trackTime = it.getFormattedTrackTime()
            timeTrack.text = trackTime
            timeTrackInfo.text = trackTime

            albumTrackInfo.text = it.getSafeCollectionName()
            yearTrackInfo.text = it.getReleaseYear() ?: "Не указано"
            genreTrackInfo.text = it.getSafePrimaryGenreName()
            countryTrackInfo.text = it.getSafeCountry()
        }
    }

    private fun setupClickListeners() {
        playTrack.setOnClickListener {
        }

        addTrack.setOnClickListener {
        }

        favoriteTrack.setOnClickListener {
        }
    }
}