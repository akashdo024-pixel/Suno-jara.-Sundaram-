package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DownloadStatus
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXError
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXNeonPink
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXPurpleDeep
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun LibraryScreen(
  tracks: List<Track>,
  playlists: List<Playlist>,
  currentTrack: Track?,
  storageUsedBytes: Long,
  historyTracks: List<Track>,
  onCreatePlaylistClick: () -> Unit,
  onPlaylistClick: (Playlist) -> Unit,
  onDeletePlaylistClick: (String) -> Unit,
  onTrackClick: (Track, List<Track>) -> Unit,
  onFavoriteClick: (Track) -> Unit,
  onDownloadClick: (Track) -> Unit,
  onDeleteDownloadClick: (String) -> Unit,
  onClearAllDownloadsClick: () -> Unit,
  onClearHistoryClick: () -> Unit,
  onAddToPlaylistClick: (Track) -> Unit,
  onPlayNextClick: (Track) -> Unit,
  onAddToQueueClick: (Track) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("Playlists", "Liked Songs", "Downloads", "History")

  val likedTracks = tracks.filter { it.isFavorite }
  val downloadedTracks = tracks.filter { it.downloadStatus == DownloadStatus.DOWNLOADED }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .testTag("library_screen")
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Your Library",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
      ),
      color = MelodyXTextPrimary,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Sub-Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = MelodyXDarkBg,
      contentColor = MelodyXGreen,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
          color = MelodyXGreen,
          height = 3.dp
        )
      },
      divider = { }
    ) {
      tabs.forEachIndexed { index, title ->
        val isSelected = selectedTabIndex == index
        Tab(
          selected = isSelected,
          onClick = { selectedTabIndex = index },
          text = {
            Text(
              text = title,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (isSelected) MelodyXGreen else MelodyXTextSecondary
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    when (selectedTabIndex) {
      // 0: Playlists
      0 -> {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            // Create New Playlist Card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MelodyXSurfaceHighlight)
                .clickable(onClick = onCreatePlaylistClick)
                .padding(14.dp)
                .testTag("create_playlist_btn"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MelodyXGreen),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Add,
                  contentDescription = null,
                  tint = MelodyXDarkBg,
                  modifier = Modifier.size(28.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "Create Playlist",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MelodyXTextPrimary
                )
                Text(
                  text = "Build your custom collection",
                  style = MaterialTheme.typography.bodySmall,
                  color = MelodyXTextSecondary
                )
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
          }

          items(playlists) { pl ->
            PlaylistRowItem(
              playlist = pl,
              onClick = { onPlaylistClick(pl) },
              onDelete = if (pl.isCustom) {
                { onDeletePlaylistClick(pl.id) }
              } else null
            )
          }
        }
      }

      // 1: Liked Songs
      1 -> {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${likedTracks.size} Liked Songs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MelodyXTextPrimary
              )
              if (likedTracks.isNotEmpty()) {
                Button(
                  onClick = { onTrackClick(likedTracks.first(), likedTracks) },
                  colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MelodyXDarkBg)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Play All", color = MelodyXDarkBg, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          if (likedTracks.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 60.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MelodyXNeonPink.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "No liked songs yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MelodyXTextPrimary
                  )
                  Text(
                    text = "Tap the heart icon on any song to save it here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MelodyXTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
            }
          }

          items(likedTracks) { track ->
            TrackListItem(
              track = track,
              isPlayingThisTrack = currentTrack?.id == track.id,
              onClick = { onTrackClick(track, likedTracks) },
              onFavoriteClick = { onFavoriteClick(track) },
              onDownloadClick = { onDownloadClick(track) },
              onAddToPlaylistClick = { onAddToPlaylistClick(track) },
              onPlayNextClick = { onPlayNextClick(track) },
              onAddToQueueClick = { onAddToQueueClick(track) }
            )
          }
        }
      }

      // 2: Downloads (Offline Music Library)
      2 -> {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            // Storage Management Card
            val mb = storageUsedBytes / (1024.0 * 1024.0)
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
              colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Offline Storage Manager",
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MelodyXTextPrimary
                    )
                    Text(
                      text = "Authorized files stored locally for offline playback",
                      style = MaterialTheme.typography.bodySmall,
                      color = MelodyXTextSecondary
                    )
                  }
                  Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MelodyXGreen,
                    modifier = Modifier.size(24.dp)
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "%.1f MB used • %d tracks".format(mb, downloadedTracks.size),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MelodyXGreen
                  )

                  if (downloadedTracks.isNotEmpty()) {
                    TextButton(onClick = onClearAllDownloadsClick) {
                      Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MelodyXError, modifier = Modifier.size(18.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Clear All", color = MelodyXError, fontSize = 13.sp)
                    }
                  }
                }
              }
            }
          }

          if (downloadedTracks.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = MelodyXGreen.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "No offline downloads yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MelodyXTextPrimary
                  )
                  Text(
                    text = "Download authorized songs to listen without internet connection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MelodyXTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
            }
          }

          items(downloadedTracks) { track ->
            TrackListItem(
              track = track,
              isPlayingThisTrack = currentTrack?.id == track.id,
              onClick = { onTrackClick(track, downloadedTracks) },
              onFavoriteClick = { onFavoriteClick(track) },
              onDownloadClick = { onDeleteDownloadClick(track.id) },
              onAddToPlaylistClick = { onAddToPlaylistClick(track) },
              onPlayNextClick = { onPlayNextClick(track) },
              onAddToQueueClick = { onAddToQueueClick(track) }
            )
          }
        }
      }

      // 3: History
      3 -> {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Recently Played",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MelodyXTextPrimary
              )
              if (historyTracks.isNotEmpty()) {
                TextButton(onClick = onClearHistoryClick) {
                  Text("Clear History", color = MelodyXTextMuted, fontSize = 13.sp)
                }
              }
            }
          }

          if (historyTracks.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 60.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MelodyXTextMuted,
                    modifier = Modifier.size(56.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "No listening history yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MelodyXTextPrimary
                  )
                  Text(
                    text = "Songs you stream will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MelodyXTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
            }
          }

          items(historyTracks) { track ->
            TrackListItem(
              track = track,
              isPlayingThisTrack = currentTrack?.id == track.id,
              onClick = { onTrackClick(track, historyTracks) },
              onFavoriteClick = { onFavoriteClick(track) },
              onDownloadClick = { onDownloadClick(track) },
              onAddToPlaylistClick = { onAddToPlaylistClick(track) },
              onPlayNextClick = { onPlayNextClick(track) },
              onAddToQueueClick = { onAddToQueueClick(track) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PlaylistRowItem(
  playlist: Playlist,
  onClick: () -> Unit,
  onDelete: (() -> Unit)?
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MelodyXSurfaceHighlight),
      contentAlignment = Alignment.Center
    ) {
      if (playlist.coverUrl.isNotEmpty()) {
        AsyncImage(
          model = playlist.coverUrl,
          contentDescription = playlist.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          Icons.Default.QueueMusic,
          contentDescription = null,
          tint = MelodyXPurple,
          modifier = Modifier.size(28.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = playlist.name,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MelodyXTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = "${playlist.trackIds.size} songs • ${if (playlist.isCustom) "Custom Playlist" else "Curated"}",
        style = MaterialTheme.typography.bodySmall,
        color = MelodyXTextSecondary
      )
    }

    if (onDelete != null) {
      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MelodyXTextMuted)
      }
    }
  }
}
