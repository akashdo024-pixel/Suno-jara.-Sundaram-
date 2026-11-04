package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.RepeatMode
import com.example.model.Track
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

class MelodyXPlayer(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.Main + Job())
  private var mediaPlayer: MediaPlayer? = null
  private var positionUpdateJob: Job? = null

  private val _currentTrack = MutableStateFlow<Track?>(null)
  val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _isBuffering = MutableStateFlow(false)
  val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

  private val _currentPositionMs = MutableStateFlow(0)
  val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

  private val _durationMs = MutableStateFlow(0)
  val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

  private val _queue = MutableStateFlow<List<Track>>(emptyList())
  val queue: StateFlow<List<Track>> = _queue.asStateFlow()

  private val _currentIndex = MutableStateFlow(0)
  val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

  private val _isShuffle = MutableStateFlow(false)
  val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

  private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
  val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

  private val _volume = MutableStateFlow(0.85f)
  val volume: StateFlow<Float> = _volume.asStateFlow()

  var onTrackPlayed: ((Track) -> Unit)? = null

  init {
    initMediaPlayer()
  }

  private fun initMediaPlayer() {
    mediaPlayer?.release()
    mediaPlayer = MediaPlayer().apply {
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      )
      setOnPreparedListener { mp ->
        _isBuffering.value = false
        _durationMs.value = mp.duration
        mp.start()
        _isPlaying.value = true
        startPositionTracking()
      }
      setOnCompletionListener {
        handleTrackCompletion()
      }
      setOnErrorListener { _, what, extra ->
        Log.e("MelodyXPlayer", "MediaPlayer error: what=$what, extra=$extra")
        _isBuffering.value = false
        _isPlaying.value = false
        true
      }
      setOnBufferingUpdateListener { _, percent ->
        // Could track network buffer percent
      }
    }
  }

  fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
    if (tracks.isEmpty()) return
    _queue.value = tracks
    val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
    _currentIndex.value = safeIndex
    playCurrentTrackInternal()
  }

  fun playTrack(track: Track, newQueue: List<Track>? = null) {
    val q = newQueue ?: if (_queue.value.contains(track)) _queue.value else (_queue.value + track)
    val idx = q.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }
    _queue.value = q
    _currentIndex.value = idx
    playCurrentTrackInternal()
  }

  fun togglePlayPause() {
    val mp = mediaPlayer ?: return
    if (_isPlaying.value) {
      mp.pause()
      _isPlaying.value = false
      stopPositionTracking()
    } else {
      if (_currentTrack.value == null && _queue.value.isNotEmpty()) {
        playCurrentTrackInternal()
      } else {
        mp.start()
        _isPlaying.value = true
        startPositionTracking()
      }
    }
  }

  fun seekTo(positionMs: Int) {
    mediaPlayer?.let { mp ->
      val safePos = positionMs.coerceIn(0, _durationMs.value)
      mp.seekTo(safePos)
      _currentPositionMs.value = safePos
    }
  }

  fun next() {
    val q = _queue.value
    if (q.isEmpty()) return

    if (_isShuffle.value && q.size > 1) {
      var nextIdx = (q.indices).random()
      if (nextIdx == _currentIndex.value) {
        nextIdx = (nextIdx + 1) % q.size
      }
      _currentIndex.value = nextIdx
    } else {
      val nextIdx = _currentIndex.value + 1
      if (nextIdx < q.size) {
        _currentIndex.value = nextIdx
      } else if (_repeatMode.value == RepeatMode.ALL) {
        _currentIndex.value = 0
      } else {
        // Stop at end of queue
        return
      }
    }
    playCurrentTrackInternal()
  }

  fun previous() {
    // If more than 3 seconds in, seek to beginning first like Spotify
    if (_currentPositionMs.value > 3000) {
      seekTo(0)
      return
    }

    val q = _queue.value
    if (q.isEmpty()) return
    val prevIdx = _currentIndex.value - 1
    if (prevIdx >= 0) {
      _currentIndex.value = prevIdx
    } else if (_repeatMode.value == RepeatMode.ALL) {
      _currentIndex.value = q.lastIndex
    } else {
      seekTo(0)
      return
    }
    playCurrentTrackInternal()
  }

  fun toggleShuffle() {
    _isShuffle.value = !_isShuffle.value
  }

  fun cycleRepeatMode() {
    _repeatMode.value = when (_repeatMode.value) {
      RepeatMode.OFF -> RepeatMode.ALL
      RepeatMode.ALL -> RepeatMode.ONE
      RepeatMode.ONE -> RepeatMode.OFF
    }
  }

  fun setVolume(vol: Float) {
    val clamped = vol.coerceIn(0f, 1f)
    _volume.value = clamped
    mediaPlayer?.setVolume(clamped, clamped)
  }

  fun addToQueue(track: Track) {
    _queue.value = _queue.value + track
    if (_currentTrack.value == null) {
      playTrack(track)
    }
  }

  fun playNextInQueue(track: Track) {
    val currentQ = _queue.value.toMutableList()
    val insertAt = (_currentIndex.value + 1).coerceAtMost(currentQ.size)
    currentQ.add(insertAt, track)
    _queue.value = currentQ
  }

  fun removeFromQueue(index: Int) {
    val currentQ = _queue.value.toMutableList()
    if (index in currentQ.indices) {
      currentQ.removeAt(index)
      _queue.value = currentQ
      if (index < _currentIndex.value) {
        _currentIndex.value = _currentIndex.value - 1
      }
    }
  }

  fun clearQueue() {
    val current = _currentTrack.value
    if (current != null) {
      _queue.value = listOf(current)
      _currentIndex.value = 0
    } else {
      _queue.value = emptyList()
      _currentIndex.value = 0
    }
  }

  private fun handleTrackCompletion() {
    when (_repeatMode.value) {
      RepeatMode.ONE -> {
        seekTo(0)
        mediaPlayer?.start()
        _isPlaying.value = true
        startPositionTracking()
      }
      RepeatMode.ALL -> next()
      RepeatMode.OFF -> {
        if (_currentIndex.value < _queue.value.lastIndex) {
          next()
        } else {
          _isPlaying.value = false
          stopPositionTracking()
        }
      }
    }
  }

  private fun playCurrentTrackInternal() {
    val q = _queue.value
    val idx = _currentIndex.value
    if (idx !in q.indices) return

    val track = q[idx]
    _currentTrack.value = track
    _isBuffering.value = true
    _currentPositionMs.value = 0
    _durationMs.value = track.durationSeconds * 1000

    stopPositionTracking()

    try {
      mediaPlayer?.reset()
      val audioSource = track.localFilePath?.let { path ->
        val file = File(path)
        if (file.exists() && file.length() > 0) {
          path
        } else {
          track.audioUrl
        }
      } ?: track.audioUrl

      if (audioSource.startsWith("http://") || audioSource.startsWith("https://")) {
        mediaPlayer?.setDataSource(audioSource)
        mediaPlayer?.prepareAsync()
      } else {
        val file = File(audioSource)
        if (file.exists()) {
          mediaPlayer?.setDataSource(context, Uri.fromFile(file))
          mediaPlayer?.prepare()
          _isBuffering.value = false
          _durationMs.value = mediaPlayer?.duration ?: (track.durationSeconds * 1000)
          mediaPlayer?.start()
          _isPlaying.value = true
          startPositionTracking()
        } else {
          // Fallback to streaming URL if local file is missing
          mediaPlayer?.setDataSource(track.audioUrl)
          mediaPlayer?.prepareAsync()
        }
      }

      mediaPlayer?.setVolume(_volume.value, _volume.value)
      onTrackPlayed?.invoke(track)
    } catch (e: Exception) {
      Log.e("MelodyXPlayer", "Error preparing audio source for ${track.title}", e)
      _isBuffering.value = false
      _isPlaying.value = false
    }
  }

  private fun startPositionTracking() {
    stopPositionTracking()
    positionUpdateJob = scope.launch {
      while (isActive && _isPlaying.value) {
        mediaPlayer?.let { mp ->
          try {
            if (mp.isPlaying) {
              _currentPositionMs.value = mp.currentPosition
              val dur = mp.duration
              if (dur > 0) _durationMs.value = dur
            }
          } catch (e: Exception) {
            // Player may be in transition
          }
        }
        delay(250)
      }
    }
  }

  private fun stopPositionTracking() {
    positionUpdateJob?.cancel()
    positionUpdateJob = null
  }

  fun release() {
    stopPositionTracking()
    mediaPlayer?.release()
    mediaPlayer = null
  }
}
