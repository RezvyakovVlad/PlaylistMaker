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
    private var handler: Handler? = null
    private var isPlaying = false
    private var currentPosition = 0
    private lateinit var track: Track

    companion object {
        private const val UPDATE_INTERVAL = 100L // Обновление каждые 100 мс
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        track = intent.getSerializableExtra("TRACK") as? Track ?: return

        initViews()
        setupToolbar()
        setupTrackData()
        setupClickListeners()
        initMediaPlayer()
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
        track.let {
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
            timeTrack.text = "00:00" // Начинаем с 00:00
            timeTrackInfo.text = trackTime

            albumTrackInfo.text = it.getSafeCollectionName()
            yearTrackInfo.text = it.getReleaseYear() ?: "Не указано"
            genreTrackInfo.text = it.getSafePrimaryGenreName()
            countryTrackInfo.text = it.getSafeCountry()
        }
    }

    private fun initMediaPlayer() {
        handler = Handler(Looper.getMainLooper())

        val previewUrl = track.getSafePreviewUrl()
        if (previewUrl.isNotEmpty()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(previewUrl)
                    setOnPreparedListener {
                        // Активируем кнопку воспроизведения после подготовки
                        playTrack.isEnabled = true
                    }
                    setOnCompletionListener {
                        // При завершении воспроизведения
                        onPlaybackCompleted()
                    }
                    prepareAsync() // Асинхронная подготовка
                }
            } catch (e: Exception) {
                e.printStackTrace()
                playTrack.isEnabled = false
            }
        } else {
            // Если нет previewUrl, отключаем кнопку воспроизведения
            playTrack.isEnabled = false
        }
    }

    private fun setupClickListeners() {
        playTrack.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }

        addTrack.setOnClickListener {
            // TODO: Implement add track functionality
        }

        favoriteTrack.setOnClickListener {
            // TODO: Implement favorite functionality
        }
    }

    private fun startPlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                // Если трек не играет, начинаем или продолжаем воспроизведение
                player.seekTo(currentPosition)
                player.start()
                isPlaying = true
                playTrack.setImageResource(R.drawable.ic_pause) // Меняем на иконку паузы
                startProgressUpdates()
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                currentPosition = player.currentPosition // Сохраняем позицию
                isPlaying = false
                playTrack.setImageResource(R.drawable.ic_play) // Меняем на иконку воспроизведения
                stopProgressUpdates()
            }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
        }
        isPlaying = false
        currentPosition = 0
        stopProgressUpdates()
        timeTrack.text = "00:00" // Сбрасываем отображение времени
    }

    private fun onPlaybackCompleted() {
        runOnUiThread {
            isPlaying = false
            currentPosition = 0
            playTrack.setImageResource(R.drawable.ic_play) // Меняем на иконку воспроизведения
            stopProgressUpdates()
            timeTrack.text = "00:00" // Сбрасываем отображение времени
        }
    }

    private fun startProgressUpdates() {
        handler?.post(object : Runnable {
            override fun run() {
                updateProgress()
                handler?.postDelayed(this, UPDATE_INTERVAL)
            }
        })
    }

    private fun stopProgressUpdates() {
        handler?.removeCallbacksAndMessages(null)
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val currentMs = player.currentPosition
                updateProgressText(currentMs)
            }
        }
    }

    private fun updateProgressText(milliseconds: Int) {
        val minutes = milliseconds / 1000 / 60
        val seconds = (milliseconds / 1000) % 60
        timeTrack.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback() // Приостанавливаем воспроизведение при уходе в фон
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        stopProgressUpdates()
        handler?.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBackPressed() {
        stopPlayback()
        super.onBackPressed()
    }
}