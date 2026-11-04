package com.example.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.download.DownloadManager
import com.example.model.Album
import com.example.model.Artist
import com.example.model.DownloadStatus
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.model.UserProfile
import com.example.player.MelodyXPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
  HOME,
  SEARCH,
  EXPLORE,
  LIBRARY,
  PROFILE
}

class MelodyXViewModel(application: Application) : AndroidViewModel(application) {
  val repository = MusicRepository(application)
  val downloadManager = DownloadManager(application)
  val player = MelodyXPlayer(application)

  // Navigation
  private val _currentTab = MutableStateFlow(NavigationTab.HOME)
  val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

  // Selected Detail Views
  private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
  val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

  private val _selectedArtist = MutableStateFlow<Artist?>(null)
  val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

  private val _selectedCategory = MutableStateFlow<String?>(null)
  val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

  // Full Screen Player
  private val _isPlayerExpanded = MutableStateFlow(false)
  val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

  private val _isQueueSheetVisible = MutableStateFlow(false)
  val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

  // Modals & Dialogs
  private val _trackForPlaylist = MutableStateFlow<Track?>(null)
  val trackForPlaylist: StateFlow<Track?> = _trackForPlaylist.asStateFlow()

  private val _showCreatePlaylistDialog = MutableStateFlow(false)
  val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

  private val _showAdminDashboard = MutableStateFlow(false)
  val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  // Home Screen Language Filter Chip
  private val _homeLanguageFilter = MutableStateFlow("All")
  val homeLanguageFilter: StateFlow<String> = _homeLanguageFilter.asStateFlow()

  // Search State
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
  val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

  private val _searchFilterChip = MutableStateFlow("All")
  val searchFilterChip: StateFlow<String> = _searchFilterChip.asStateFlow()

  private var searchDebounceJob: Job? = null

  // Combined tracks with download status and favorites
  val tracks: StateFlow<List<Track>> = combine(
    repository.allTracks,
    repository.favoriteTrackIds,
    downloadManager.downloadStatusMap,
    downloadManager.downloadProgressMap
  ) { allTracks, favs, downloadStatus, progressMap ->
    allTracks.map { track ->
      val status = downloadStatus[track.id] ?: DownloadStatus.NOT_DOWNLOADED
      val localFile = if (status == DownloadStatus.DOWNLOADED) {
        downloadManager.getLocalFile(track.id)?.absolutePath
      } else null
      val fileSizeBytes = if (status == DownloadStatus.DOWNLOADED) {
        downloadManager.getLocalFile(track.id)?.length() ?: 0L
      } else 0L

      track.copy(
        isFavorite = favs.contains(track.id),
        downloadStatus = status,
        downloadProgress = progressMap[track.id] ?: 0f,
        localFilePath = localFile,
        fileSizeBytes = fileSizeBytes
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val playlists: StateFlow<List<Playlist>> = repository.playlists
  val artists: StateFlow<List<Artist>> = repository.artists
  val albums: StateFlow<List<Album>> = repository.albums
  val userProfile: StateFlow<UserProfile> = repository.userProfile
  val storageUsedBytes: StateFlow<Long> = downloadManager.totalStorageUsedBytes

  // YouTube Data API flows
  val trendingYouTubeTracks: StateFlow<List<Track>> = repository.trendingYouTubeTracks
  val recentReleaseTracks: StateFlow<List<Track>> = repository.recentReleaseTracks
  val youTubeApiErrorMessage: StateFlow<String?> = repository.youTubeApiErrorMessage
  val isYouTubeQuotaExceeded: StateFlow<Boolean> = repository.isYouTubeQuotaExceeded

  // Active YouTube embedded playback state
  private val _activeYouTubeTrack = MutableStateFlow<Track?>(null)
  val activeYouTubeTrack: StateFlow<Track?> = _activeYouTubeTrack.asStateFlow()

  // Playback states forwarded from player
  val currentTrack: StateFlow<Track?> = player.currentTrack
  val isPlaying: StateFlow<Boolean> = player.isPlaying
  val isBuffering: StateFlow<Boolean> = player.isBuffering
  val currentPositionMs: StateFlow<Int> = player.currentPositionMs
  val durationMs: StateFlow<Int> = player.durationMs
  val queue: StateFlow<List<Track>> = player.queue
  val isShuffle: StateFlow<Boolean> = player.isShuffle
  val repeatMode: StateFlow<RepeatMode> = player.repeatMode
  val volume: StateFlow<Float> = player.volume

  init {
    player.onTrackPlayed = { track ->
      repository.recordHistory(track)
    }
    // Fetch initial trending and recent releases from YouTube Data API v3
    refreshYouTubeMusic()
  }

  fun refreshYouTubeMusic() {
    viewModelScope.launch {
      repository.fetchYouTubeTrending()
      repository.fetchYouTubeRecentReleases(homeLanguageFilter.value)
    }
  }

  fun playYouTubeTrack(track: Track) {
    // When playing YouTube official embedded video, pause local audio player to prevent audio clash
    if (player.isPlaying.value) {
      player.togglePlayPause()
    }
    _activeYouTubeTrack.value = track
    repository.recordHistory(track)
  }

  fun closeYouTubePlayer() {
    _activeYouTubeTrack.value = null
  }

  fun selectTab(tab: NavigationTab) {
    _currentTab.value = tab
    // Close detail panels when navigating main tabs
    _selectedPlaylist.value = null
    _selectedArtist.value = null
    _selectedCategory.value = null
  }

  fun openPlaylist(playlist: Playlist) {
    _selectedPlaylist.value = playlist
  }

  fun closePlaylistDetail() {
    _selectedPlaylist.value = null
  }

  fun openArtist(artist: Artist) {
    _selectedArtist.value = artist
  }

  fun closeArtistDetail() {
    _selectedArtist.value = null
  }

  fun openCategory(categoryName: String) {
    _selectedCategory.value = categoryName
  }

  fun closeCategoryDetail() {
    _selectedCategory.value = null
  }

  fun setPlayerExpanded(expanded: Boolean) {
    _isPlayerExpanded.value = expanded
  }

  fun setQueueSheetVisible(visible: Boolean) {
    _isQueueSheetVisible.value = visible
  }

  fun setHomeLanguageFilter(lang: String) {
    _homeLanguageFilter.value = lang
  }

  fun setTrackForPlaylist(track: Track?) {
    _trackForPlaylist.value = track
  }

  fun setShowCreatePlaylistDialog(show: Boolean) {
    _showCreatePlaylistDialog.value = show
  }

  fun setShowAdminDashboard(show: Boolean) {
    _showAdminDashboard.value = show
  }

  fun setShowAuthDialog(show: Boolean) {
    _showAuthDialog.value = show
  }

  // --- Playback Controls ---
  fun playTrack(track: Track, contextQueue: List<Track>? = null) {
    player.playTrack(track, contextQueue)
  }

  fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
    player.playQueue(tracks, startIndex)
  }

  fun togglePlayPause() = player.togglePlayPause()
  fun next() = player.next()
  fun previous() = player.previous()
  fun seekTo(posMs: Int) = player.seekTo(posMs)
  fun toggleShuffle() = player.toggleShuffle()
  fun cycleRepeatMode() = player.cycleRepeatMode()
  fun setVolume(vol: Float) = player.setVolume(vol)
  fun addToQueue(track: Track) = player.addToQueue(track)
  fun playNextInQueue(track: Track) = player.playNextInQueue(track)
  fun removeFromQueue(index: Int) = player.removeFromQueue(index)
  fun clearQueue() = player.clearQueue()

  // --- Favorite Toggle ---
  fun toggleFavorite(track: Track) {
    val isFav = repository.toggleFavorite(track.id)
    val msg = if (isFav) "Added to Liked Songs" else "Removed from Liked Songs"
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }

  // --- Downloads ---
  fun toggleDownload(track: Track) {
    if (downloadManager.isTrackDownloaded(track.id)) {
      downloadManager.deleteDownload(track.id)
      Toast.makeText(getApplication(), "Removed from offline storage", Toast.LENGTH_SHORT).show()
    } else {
      if (!track.isAuthorizedDownload) {
        Toast.makeText(
          getApplication(),
          "Offline download is not licensed by copyright holder",
          Toast.LENGTH_LONG
        ).show()
        return
      }
      Toast.makeText(getApplication(), "Starting authorized download...", Toast.LENGTH_SHORT).show()
      downloadManager.downloadTrack(
        track = track,
        onSuccess = {
          Toast.makeText(getApplication(), "Downloaded: ${track.title}", Toast.LENGTH_SHORT).show()
        },
        onError = { err ->
          Toast.makeText(getApplication(), "Download error: $err", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }

  fun deleteDownload(trackId: String) {
    downloadManager.deleteDownload(trackId)
    Toast.makeText(getApplication(), "Track removed from downloads", Toast.LENGTH_SHORT).show()
  }

  fun clearAllDownloads() {
    val freed = downloadManager.clearAllDownloads()
    val mb = freed / (1024.0 * 1024.0)
    Toast.makeText(
      getApplication(),
      "Cleared %.1f MB of downloaded audio".format(mb),
      Toast.LENGTH_SHORT
    ).show()
  }

  // --- Search ---
  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
    searchDebounceJob?.cancel()

    if (query.isBlank()) {
      _searchResults.value = emptyList()
      _isSearching.value = false
      return
    }

    searchDebounceJob = viewModelScope.launch {
      delay(350) // Debounce network calls
      _isSearching.value = true
      val languageFilter = _searchFilterChip.value.takeIf { it != "All" && it != "Songs" && it != "Artists" }
      val results = repository.searchYouTubeMusic(
        query = query,
        language = languageFilter
      )
      _searchResults.value = results
      _isSearching.value = false
    }
  }

  fun setSearchFilterChip(chip: String) {
    _searchFilterChip.value = chip
    if (_searchQuery.value.isNotBlank()) {
      onSearchQueryChanged(_searchQuery.value)
    }
  }

  // --- Playlist Actions ---
  fun createPlaylist(name: String, description: String = ""): Playlist {
    val pl = repository.createPlaylist(name, description)
    Toast.makeText(getApplication(), "Created playlist: ${pl.name}", Toast.LENGTH_SHORT).show()
    return pl
  }

  fun addTrackToPlaylist(playlistId: String, trackId: String) {
    repository.addTrackToPlaylist(playlistId, trackId)
    Toast.makeText(getApplication(), "Added to playlist", Toast.LENGTH_SHORT).show()
  }

  fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
    repository.removeTrackFromPlaylist(playlistId, trackId)
    Toast.makeText(getApplication(), "Removed from playlist", Toast.LENGTH_SHORT).show()
    // Refresh selected playlist if open
    _selectedPlaylist.value?.let { current ->
      if (current.id == playlistId) {
        _selectedPlaylist.value = current.copy(trackIds = current.trackIds - trackId)
      }
    }
  }

  fun deletePlaylist(playlistId: String) {
    repository.deletePlaylist(playlistId)
    if (_selectedPlaylist.value?.id == playlistId) {
      _selectedPlaylist.value = null
    }
    Toast.makeText(getApplication(), "Playlist deleted", Toast.LENGTH_SHORT).show()
  }

  // --- Profile & Admin ---
  fun updateProfile(name: String, email: String, quality: String, offlineOnly: Boolean) {
    repository.updateUserProfile(name, email, quality, offlineOnly)
    Toast.makeText(getApplication(), "Profile updated", Toast.LENGTH_SHORT).show()
  }

  fun addAdminTrack(track: Track) {
    repository.addCustomTrack(track)
    Toast.makeText(getApplication(), "Track added to catalog", Toast.LENGTH_SHORT).show()
  }

  fun clearListeningHistory() {
    repository.clearHistory()
    Toast.makeText(getApplication(), "Listening history cleared", Toast.LENGTH_SHORT).show()
  }

  override fun onCleared() {
    super.onCleared()
    player.release()
  }
}
