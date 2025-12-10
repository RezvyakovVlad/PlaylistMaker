package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class MediaPlayerManager {

    private var mediaPlayer: MediaPlayer? = null
    private var handler: Handler? = null
    private var isPrepared = false
    private var onProgressUpdate: ((Int) -> Unit)? = null

    fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                isPrepared = true
                onPrepared()
            }
            setOnCompletionListener {
                stopProgressUpdates()
                onCompletion()
            }
            setOnErrorListener { _, what, extra ->
                onError("MediaPlayer error: what=$what, extra=$extra")
                true
            }
            prepareAsync()
        }

        handler = Handler(Looper.getMainLooper())
    }

    fun play() {
        mediaPlayer?.takeIf { isPrepared && !it.isPlaying }?.let { player ->
            player.start()
            startProgressUpdates()
        }
    }

    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.let { player ->
            player.pause()
            stopProgressUpdates()
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            isPrepared = false
            stopProgressUpdates()
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun isPrepared(): Boolean {
        return isPrepared
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun setOnProgressUpdateListener(listener: (Int) -> Unit) {
        onProgressUpdate = listener
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        handler?.post(object : Runnable {
            override fun run() {
                onProgressUpdate?.invoke(getCurrentPosition())
                handler?.postDelayed(this, 100L)
            }
        })
    }

    private fun stopProgressUpdates() {
        handler?.removeCallbacksAndMessages(null)
    }

    fun release() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        handler = null
        isPrepared = false
    }
}