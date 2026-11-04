package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXPurpleDeep
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun PlaylistDetailScreen(
  playlist: Playlist,
  allTracks: List<Track>,
  currentTrack: Track?,
  onBackClick: () -> Unit,
  onPlayAllClick: (List<Track>) -> Unit,
  onTrackClick: (Track, List<Track>) -> Unit,
  onFavoriteClick: (Track) -> Unit,
  onDownloadClick: (Track) -> Unit,
  onAddToPlaylistClick: (Track) -> Unit,
  onPlayNextClick: (Track) -> Unit,
  onAddToQueueClick: (Track) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler {
    onBackClick()
  }

  val playlistTracks = allTracks.filter { playlist.trackIds.contains(it.id) }
  val totalDurationSeconds = playlistTracks.sumOf { it.durationSeconds }
  val totalMinutes = totalDurationSeconds / 60

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .testTag("playlist_detail_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    // Header Hero with Blurred Gradient
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(MelodyXPurpleDeep.copy(alpha = 0.5f), MelodyXDarkBg)
            )
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Back button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
          ) {
            IconButton(
              onClick = onBackClick,
              modifier = Modifier.size(40.dp)
            ) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MelodyXTextPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Cover Image
          Box(
            modifier = Modifier
              .size(160.dp)
              .aspectRatio(1f)
              .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = MelodyXPurple)
              .clip(RoundedCornerShape(16.dp))
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
                modifier = Modifier.size(64.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          if (playlist.description.isNotEmpty()) {
            Text(
              text = playlist.description,
              style = MaterialTheme.typography.bodySmall,
              color = MelodyXTextSecondary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
            )
          }

          Text(
            text = "${playlistTracks.size} songs • $totalMinutes mins",
            style = MaterialTheme.typography.labelMedium,
            color = MelodyXGreen,
            modifier = Modifier.padding(top = 6.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Play & Shuffle Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                if (playlistTracks.isNotEmpty()) onPlayAllClick(playlistTracks)
              },
              colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen),
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MelodyXDarkBg)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Play All", color = MelodyXDarkBg, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
              onClick = {
                if (playlistTracks.isNotEmpty()) {
                  val shuffled = playlistTracks.shuffled()
                  onPlayAllClick(shuffled)
                }
              },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, tint = MelodyXTextPrimary)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Shuffle", color = MelodyXTextPrimary)
            }
          }
        }
      }
    }

    if (playlistTracks.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No songs added to this playlist yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MelodyXTextSecondary
          )
        }
      }
    }

    itemsIndexed(playlistTracks) { index, track ->
      TrackListItem(
        track = track,
        isPlayingThisTrack = currentTrack?.id == track.id,
        onClick = { onTrackClick(track, playlistTracks) },
        onFavoriteClick = { onFavoriteClick(track) },
        onDownloadClick = { onDownloadClick(track) },
        onAddToPlaylistClick = { onAddToPlaylistClick(track) },
        onPlayNextClick = { onPlayNextClick(track) },
        onAddToQueueClick = { onAddToQueueClick(track) },
        modifier = Modifier.padding(horizontal = 8.dp)
      )
    }
  }
}
