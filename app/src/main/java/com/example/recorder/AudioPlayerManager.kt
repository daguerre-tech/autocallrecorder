package com.example.recorder

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val currentRecordingId: Long? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playbackSpeed: Float = 1.0f
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(recordingId: Long, filePath: String) {
        val current = _playbackState.value

        // If same track is already loaded and paused, resume it
        if (current.currentRecordingId == recordingId && mediaPlayer != null) {
            if (!current.isPlaying) {
                mediaPlayer?.start()
                _playbackState.value = current.copy(isPlaying = true)
                startProgressTracker()
            }
            return
        }

        // Otherwise stop previous and load new
        stop()

        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayerManager", "Audio file does not exist: $filePath")
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0
                    )
                    progressJob?.cancel()
                }
            }

            mediaPlayer = player
            applySpeed(current.playbackSpeed)
            player.start()

            _playbackState.value = PlaybackState(
                currentRecordingId = recordingId,
                isPlaying = true,
                currentPositionMs = 0,
                durationMs = player.duration.coerceAtLeast(1000),
                playbackSpeed = current.playbackSpeed
            )

            startProgressTracker()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error playing audio file", e)
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        progressJob?.cancel()
    }

    fun togglePlayPause(recordingId: Long, filePath: String) {
        val current = _playbackState.value
        if (current.currentRecordingId == recordingId && current.isPlaying) {
            pause()
        } else {
            play(recordingId, filePath)
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
        val currentSpeed = _playbackState.value.playbackSpeed
        val nextIndex = (speeds.indexOf(currentSpeed) + 1) % speeds.size
        val nextSpeed = speeds[nextIndex]

        applySpeed(nextSpeed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = nextSpeed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply { this.speed = speed }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = PlaybackState(playbackSpeed = _playbackState.value.playbackSpeed)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }
                val dur = try { mediaPlayer?.duration ?: 0 } catch (_: Exception) { 0 }
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = pos,
                    durationMs = if (dur > 0) dur else _playbackState.value.durationMs
                )
                delay(100)
            }
        }
    }
}
