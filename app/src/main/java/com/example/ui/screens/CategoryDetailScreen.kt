package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXPurpleDeep
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun CategoryDetailScreen(
  categoryName: String,
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

  val matchingTracks = allTracks.filter {
    it.language.contains(categoryName, ignoreCase = true) ||
        it.genre.contains(categoryName, ignoreCase = true) ||
        categoryName.contains(it.language, ignoreCase = true) ||
        categoryName == "Top Charts"
  }.ifEmpty { allTracks.take(8) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .testTag("category_detail_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
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
        Column(modifier = Modifier.fillMaxWidth()) {
          IconButton(onClick = onBackClick) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MelodyXTextPrimary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = categoryName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary
          )

          Text(
            text = "Official ${categoryName} Collection • ${matchingTracks.size} tracks",
            style = MaterialTheme.typography.bodyMedium,
            color = MelodyXGreen,
            modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                if (matchingTracks.isNotEmpty()) onPlayAllClick(matchingTracks)
              },
              colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen),
              shape = CircleShape
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MelodyXDarkBg)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Play All", color = MelodyXDarkBg, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
              onClick = {
                if (matchingTracks.isNotEmpty()) onPlayAllClick(matchingTracks.shuffled())
              },
              shape = CircleShape
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, tint = MelodyXTextPrimary)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Shuffle", color = MelodyXTextPrimary)
            }
          }
        }
      }
    }

    itemsIndexed(matchingTracks) { index, track ->
      TrackListItem(
        track = track,
        isPlayingThisTrack = currentTrack?.id == track.id,
        onClick = { onTrackClick(track, matchingTracks) },
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
