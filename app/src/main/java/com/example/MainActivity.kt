package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.YouTubeEmbeddedPlayer
import com.example.ui.screens.AdminDashboardDialog
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.AuthProfileDialog
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTheme
import com.example.viewmodel.MelodyXViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MelodyXTheme {
        MelodyXApp()
      }
    }
  }
}

@Composable
fun MelodyXApp(viewModel: MelodyXViewModel = viewModel()) {
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
  val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

  val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
  val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsStateWithLifecycle()
  val trackForPlaylist by viewModel.trackForPlaylist.collectAsStateWithLifecycle()
  val showAdminDashboard by viewModel.showAdminDashboard.collectAsStateWithLifecycle()
  val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()

  val tracks by viewModel.tracks.collectAsStateWithLifecycle()
  val playlists by viewModel.playlists.collectAsStateWithLifecycle()
  val artists by viewModel.artists.collectAsStateWithLifecycle()
  val albums by viewModel.albums.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val storageUsedBytes by viewModel.storageUsedBytes.collectAsStateWithLifecycle()

  // Player state
  val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
  val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
  val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
  val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
  val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
  val queue by viewModel.queue.collectAsStateWithLifecycle()
  val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
  val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
  val volume by viewModel.volume.collectAsStateWithLifecycle()

  // Home & Search state
  val homeLanguageFilter by viewModel.homeLanguageFilter.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
  val searchFilterChip by viewModel.searchFilterChip.collectAsStateWithLifecycle()

  // YouTube states
  val trendingYouTubeTracks by viewModel.trendingYouTubeTracks.collectAsStateWithLifecycle()
  val recentReleaseTracks by viewModel.recentReleaseTracks.collectAsStateWithLifecycle()
  val youTubeApiErrorMessage by viewModel.youTubeApiErrorMessage.collectAsStateWithLifecycle()
  val isYouTubeQuotaExceeded by viewModel.isYouTubeQuotaExceeded.collectAsStateWithLifecycle()
  val activeYouTubeTrack by viewModel.activeYouTubeTrack.collectAsStateWithLifecycle()

  val historyTracks = tracks.filter { track ->
    viewModel.repository.historyTrackIds.value.contains(track.id)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MelodyXDarkBg,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          // Persistent Mini Player above bottom bar
          MiniPlayer(
            track = currentTrack,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.next() },
            onFavoriteClick = { currentTrack?.let { viewModel.toggleFavorite(it) } },
            onExpandClick = { viewModel.setPlayerExpanded(true) }
          )

          // 5-Tab Spotify-inspired Navigation Bar
          NavigationBar(
            containerColor = MelodyXSurface,
            contentColor = MelodyXTextPrimary,
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("bottom_nav_bar")
          ) {
            NavigationBarItem(
              selected = currentTab == NavigationTab.HOME && selectedPlaylist == null && selectedArtist == null && selectedCategory == null,
              onClick = { viewModel.selectTab(NavigationTab.HOME) },
              icon = {
                Icon(
                  if (currentTab == NavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                  contentDescription = "Home"
                )
              },
              label = { Text("Home", fontSize = 11.sp) },
              colors = navBarItemColors(),
              modifier = Modifier.testTag("tab_home")
            )
            NavigationBarItem(
              selected = currentTab == NavigationTab.SEARCH,
              onClick = { viewModel.selectTab(NavigationTab.SEARCH) },
              icon = {
                Icon(
                  if (currentTab == NavigationTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                  contentDescription = "Search"
                )
              },
              label = { Text("Search", fontSize = 11.sp) },
              colors = navBarItemColors(),
              modifier = Modifier.testTag("tab_search")
            )
            NavigationBarItem(
              selected = currentTab == NavigationTab.EXPLORE,
              onClick = { viewModel.selectTab(NavigationTab.EXPLORE) },
              icon = {
                Icon(
                  if (currentTab == NavigationTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                  contentDescription = "Explore"
                )
              },
              label = { Text("Explore", fontSize = 11.sp) },
              colors = navBarItemColors(),
              modifier = Modifier.testTag("tab_explore")
            )
            NavigationBarItem(
              selected = currentTab == NavigationTab.LIBRARY,
              onClick = { viewModel.selectTab(NavigationTab.LIBRARY) },
              icon = {
                Icon(
                  if (currentTab == NavigationTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                  contentDescription = "Library"
                )
              },
              label = { Text("Library", fontSize = 11.sp) },
              colors = navBarItemColors(),
              modifier = Modifier.testTag("tab_library")
            )
            NavigationBarItem(
              selected = currentTab == NavigationTab.PROFILE,
              onClick = { viewModel.selectTab(NavigationTab.PROFILE) },
              icon = {
                Icon(
                  if (currentTab == NavigationTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                  contentDescription = "Profile"
                )
              },
              label = { Text("Profile", fontSize = 11.sp) },
              colors = navBarItemColors(),
              modifier = Modifier.testTag("tab_profile")
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when {
          selectedPlaylist != null -> {
            PlaylistDetailScreen(
              playlist = selectedPlaylist!!,
              allTracks = tracks,
              currentTrack = currentTrack,
              onBackClick = { viewModel.closePlaylistDetail() },
              onPlayAllClick = { trackList -> viewModel.playQueue(trackList, 0) },
              onTrackClick = { track, list -> viewModel.playTrack(track, list) },
              onFavoriteClick = { viewModel.toggleFavorite(it) },
              onDownloadClick = { viewModel.toggleDownload(it) },
              onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
              onPlayNextClick = { viewModel.playNextInQueue(it) },
              onAddToQueueClick = { viewModel.addToQueue(it) }
            )
          }
          selectedArtist != null -> {
            ArtistDetailScreen(
              artist = selectedArtist!!,
              allTracks = tracks,
              currentTrack = currentTrack,
              onBackClick = { viewModel.closeArtistDetail() },
              onPlayAllClick = { trackList -> viewModel.playQueue(trackList, 0) },
              onTrackClick = { track, list -> viewModel.playTrack(track, list) },
              onFavoriteClick = { viewModel.toggleFavorite(it) },
              onDownloadClick = { viewModel.toggleDownload(it) },
              onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
              onPlayNextClick = { viewModel.playNextInQueue(it) },
              onAddToQueueClick = { viewModel.addToQueue(it) }
            )
          }
          selectedCategory != null -> {
            CategoryDetailScreen(
              categoryName = selectedCategory!!,
              allTracks = tracks,
              currentTrack = currentTrack,
              onBackClick = { viewModel.closeCategoryDetail() },
              onPlayAllClick = { trackList -> viewModel.playQueue(trackList, 0) },
              onTrackClick = { track, list -> viewModel.playTrack(track, list) },
              onFavoriteClick = { viewModel.toggleFavorite(it) },
              onDownloadClick = { viewModel.toggleDownload(it) },
              onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
              onPlayNextClick = { viewModel.playNextInQueue(it) },
              onAddToQueueClick = { viewModel.addToQueue(it) }
            )
          }
          else -> {
            when (currentTab) {
              NavigationTab.HOME -> {
                HomeScreen(
                  tracks = tracks,
                  playlists = playlists,
                  artists = artists,
                  albums = albums,
                  currentTrack = currentTrack,
                  isPlaying = isPlaying,
                  selectedLanguage = homeLanguageFilter,
                  onLanguageSelected = { viewModel.setHomeLanguageFilter(it) },
                  youTubeTrendingTracks = trendingYouTubeTracks,
                  youTubeRecentReleases = recentReleaseTracks,
                  isYouTubeQuotaExceeded = isYouTubeQuotaExceeded,
                  youTubeErrorMessage = youTubeApiErrorMessage,
                  onYouTubePlayClick = { track -> viewModel.playYouTubeTrack(track) },
                  onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                  onPlaylistClick = { viewModel.openPlaylist(it) },
                  onArtistClick = { viewModel.openArtist(it) },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onDownloadClick = { viewModel.toggleDownload(it) },
                  onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
                  onPlayNextClick = { viewModel.playNextInQueue(it) },
                  onAddToQueueClick = { viewModel.addToQueue(it) },
                  onOpenAdminClick = { viewModel.setShowAdminDashboard(true) }
                )
              }
              NavigationTab.SEARCH -> {
                SearchScreen(
                  query = searchQuery,
                  isSearching = isSearching,
                  searchResults = searchResults,
                  catalogTracks = tracks,
                  currentTrack = currentTrack,
                  selectedFilter = searchFilterChip,
                  onQueryChange = { viewModel.onSearchQueryChanged(it) },
                  onFilterChange = { viewModel.setSearchFilterChip(it) },
                  onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onDownloadClick = { viewModel.toggleDownload(it) },
                  onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
                  onPlayNextClick = { viewModel.playNextInQueue(it) },
                  onAddToQueueClick = { viewModel.addToQueue(it) },
                  onWatchVideoClick = { track -> viewModel.playYouTubeTrack(track) },
                  isQuotaExceeded = isYouTubeQuotaExceeded,
                  errorMessage = youTubeApiErrorMessage
                )
              }
              NavigationTab.EXPLORE -> {
                ExploreScreen(
                  tracks = tracks,
                  onCategorySelected = { viewModel.openCategory(it) },
                  onPlaylistClick = { viewModel.openPlaylist(it) }
                )
              }
              NavigationTab.LIBRARY -> {
                LibraryScreen(
                  tracks = tracks,
                  playlists = playlists,
                  currentTrack = currentTrack,
                  storageUsedBytes = storageUsedBytes,
                  historyTracks = historyTracks,
                  onCreatePlaylistClick = { viewModel.setShowCreatePlaylistDialog(true) },
                  onPlaylistClick = { viewModel.openPlaylist(it) },
                  onDeletePlaylistClick = { viewModel.deletePlaylist(it) },
                  onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onDownloadClick = { viewModel.toggleDownload(it) },
                  onDeleteDownloadClick = { viewModel.deleteDownload(it) },
                  onClearAllDownloadsClick = { viewModel.clearAllDownloads() },
                  onClearHistoryClick = { viewModel.clearListeningHistory() },
                  onAddToPlaylistClick = { viewModel.setTrackForPlaylist(it) },
                  onPlayNextClick = { viewModel.playNextInQueue(it) },
                  onAddToQueueClick = { viewModel.addToQueue(it) }
                )
              }
              NavigationTab.PROFILE -> {
                ProfileScreen(
                  userProfile = userProfile,
                  likedSongsCount = tracks.count { it.isFavorite },
                  downloadedTracksCount = tracks.count { it.downloadStatus == com.example.model.DownloadStatus.DOWNLOADED },
                  onOpenAdminClick = { viewModel.setShowAdminDashboard(true) },
                  onOpenAuthDialog = { viewModel.setShowAuthDialog(true) },
                  onToggleOfflineMode = {
                    viewModel.updateProfile(userProfile.name, userProfile.email, userProfile.streamQuality, it)
                  },
                  onChangeQuality = {
                    viewModel.updateProfile(userProfile.name, userProfile.email, it, userProfile.offlineOnly)
                  }
                )
              }
            }
          }
        }
      }
    }

    // Full Screen Player Modal with Slide-In Animation
    AnimatedVisibility(
      visible = isPlayerExpanded,
      enter = slideInVertically(initialOffsetY = { it }),
      exit = slideOutVertically(targetOffsetY = { it }),
      modifier = Modifier.fillMaxSize()
    ) {
      FullScreenPlayer(
        track = currentTrack,
        isPlaying = isPlaying,
        isBuffering = isBuffering,
        currentPositionMs = currentPositionMs,
        durationMs = durationMs,
        isShuffle = isShuffle,
        repeatMode = repeatMode,
        volume = volume,
        onCollapseClick = { viewModel.setPlayerExpanded(false) },
        onPlayPauseClick = { viewModel.togglePlayPause() },
        onNextClick = { viewModel.next() },
        onPreviousClick = { viewModel.previous() },
        onSeekTo = { viewModel.seekTo(it) },
        onToggleShuffle = { viewModel.toggleShuffle() },
        onCycleRepeat = { viewModel.cycleRepeatMode() },
        onFavoriteClick = { currentTrack?.let { viewModel.toggleFavorite(it) } },
        onDownloadClick = { currentTrack?.let { viewModel.toggleDownload(it) } },
        onVolumeChange = { viewModel.setVolume(it) },
        onOpenQueueClick = { viewModel.setQueueSheetVisible(true) }
      )
    }

    // Queue Bottom Sheet
    if (isQueueSheetVisible) {
      QueueBottomSheet(
        queue = queue,
        currentTrack = currentTrack,
        onDismiss = { viewModel.setQueueSheetVisible(false) },
        onTrackClick = { viewModel.playTrack(it) },
        onRemoveTrack = { viewModel.removeFromQueue(it) },
        onClearQueue = { viewModel.clearQueue() }
      )
    }

    // Add to Playlist Dialog
    if (trackForPlaylist != null) {
      AddToPlaylistDialog(
        track = trackForPlaylist,
        playlists = playlists,
        onDismiss = { viewModel.setTrackForPlaylist(null) },
        onSelectPlaylist = { pl ->
          viewModel.addTrackToPlaylist(pl.id, trackForPlaylist!!.id)
          viewModel.setTrackForPlaylist(null)
        },
        onCreateNewPlaylist = { name ->
          val newPl = viewModel.createPlaylist(name)
          viewModel.addTrackToPlaylist(newPl.id, trackForPlaylist!!.id)
          viewModel.setTrackForPlaylist(null)
        }
      )
    }

    // Create Playlist Dialog from Library
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    if (showCreatePlaylistDialog) {
      AddToPlaylistDialog(
        track = tracks.firstOrNull(),
        playlists = playlists,
        onDismiss = { viewModel.setShowCreatePlaylistDialog(false) },
        onSelectPlaylist = { viewModel.setShowCreatePlaylistDialog(false) },
        onCreateNewPlaylist = { name ->
          viewModel.createPlaylist(name)
          viewModel.setShowCreatePlaylistDialog(false)
        }
      )
    }

    // Admin Dashboard Dialog
    if (showAdminDashboard) {
      AdminDashboardDialog(
        totalTracks = tracks.size,
        playlists = playlists,
        storageUsedBytes = storageUsedBytes,
        onDismiss = { viewModel.setShowAdminDashboard(false) },
        onAddTrack = { viewModel.addAdminTrack(it) }
      )
    }

    // Profile & Auth Dialog
    if (showAuthDialog) {
      AuthProfileDialog(
        currentProfile = userProfile,
        onDismiss = { viewModel.setShowAuthDialog(false) },
        onSaveProfile = { name, email ->
          viewModel.updateProfile(name, email, userProfile.streamQuality, userProfile.offlineOnly)
        }
      )
    }

    // Official YouTube Embedded Video Player Modal
    if (activeYouTubeTrack != null) {
      androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.closeYouTubePlayer() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          YouTubeEmbeddedPlayer(
            track = activeYouTubeTrack!!,
            onClose = { viewModel.closeYouTubePlayer() },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
  selectedIconColor = MelodyXGreen,
  selectedTextColor = MelodyXGreen,
  unselectedIconColor = MelodyXTextMuted,
  unselectedTextColor = MelodyXTextMuted,
  indicatorColor = MelodyXSurfaceHighlight
)
