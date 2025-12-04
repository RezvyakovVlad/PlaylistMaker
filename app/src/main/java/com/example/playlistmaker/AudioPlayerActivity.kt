package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale

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

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var playbackPosition = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateProgressRunnable: Runnable
    private lateinit var currentTrack: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        initViews()
        setupToolbar()
        setupTrackData()
        setupClickListeners()
        setupMediaPlayer()
        setupProgressUpdater()
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
            stopPlayback()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTrackData() {
        val track = intent.getSerializableExtra("TRACK") as? Track
        track?.let {
            currentTrack = it

            if (!it.artworkUrl100.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.getCoverArtwork())
                    .placeholder(R.drawable.ic_placeholder_312)
                    .error(R.drawable.ic_placeholder_312)
                    .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
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

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                startPlayback()
            }
            setOnCompletionListener {
                playbackCompleted()
            }
        }
    }

    private fun setupProgressUpdater() {
        updateProgressRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        updateProgress(currentPosition)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun setupClickListeners() {
        playTrack.setOnClickListener {
            togglePlayback()
        }

        addTrack.setOnClickListener {
            // Логика добавления трека в плейлист
        }

        favoriteTrack.setOnClickListener {
            // Логика добавления в избранное
        }
    }

    private fun togglePlayback() {
        if (isPlaying) {
            pausePlayback()
        } else {
            startOrResumePlayback()
        }
    }

    private fun startOrResumePlayback() {
        val previewUrl = currentTrack.getSafePreviewUrl()
        if (previewUrl.isEmpty()) return

        mediaPlayer?.let { player ->
            try {
                if (playbackPosition > 0) {
                    player.seekTo(playbackPosition)
                    player.start()
                    startProgressUpdates()
                    updatePlayButton(true)
                } else {
                    player.reset()
                    player.setDataSource(previewUrl)
                    player.prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPlayback() {
        mediaPlayer?.start()
        startProgressUpdates()
        updatePlayButton(true)
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        playbackPosition = mediaPlayer?.currentPosition ?: 0
        stopProgressUpdates()
        updatePlayButton(false)
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        playbackPosition = 0
        stopProgressUpdates()
        updateProgress(0)
        updatePlayButton(false)
    }

    private fun playbackCompleted() {
        playbackPosition = 0
        stopProgressUpdates()
        updateProgress(0)
        updatePlayButton(false)
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable)
        handler.post(updateProgressRunnable)
        isPlaying = true
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable)
        isPlaying = false
    }

    private fun updateProgress(position: Int) {
        val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(position)
        timeTrackInfo.text = formattedTime
    }

    private fun updatePlayButton(playing: Boolean) {
        if (playing) {
            playTrack.setImageResource(R.drawable.ic_pause)
        } else {
            playTrack.setImageResource(R.drawable.ic_play)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateProgressRunnable)
    }
}