package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Track
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXError
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
  queue: List<Track>,
  currentTrack: Track?,
  onDismiss: () -> Unit,
  onTrackClick: (Track) -> Unit,
  onRemoveTrack: (Int) -> Unit,
  onClearQueue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MelodyXSurface,
    contentColor = MelodyXTextPrimary,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Playback Queue",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary
          )
          Text(
            text = "${queue.size} songs in queue",
            style = MaterialTheme.typography.bodySmall,
            color = MelodyXTextSecondary
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (queue.size > 1) {
            TextButton(onClick = onClearQueue) {
              Text("Clear Queue", color = MelodyXError, fontSize = 13.sp)
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MelodyXTextPrimary)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp)
      ) {
        // Now Playing Section
        if (currentTrack != null) {
          item {
            Text(
              text = "NOW PLAYING",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
              ),
              color = MelodyXGreen,
              modifier = Modifier.padding(vertical = 8.dp)
            )
            QueueItemRow(
              track = currentTrack,
              isCurrent = true,
              onClick = { },
              onRemove = null
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "UP NEXT",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
              ),
              color = MelodyXTextMuted,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }
        }

        // Rest of the Queue
        itemsIndexed(queue) { index, track ->
          if (track.id != currentTrack?.id) {
            QueueItemRow(
              track = track,
              isCurrent = false,
              onClick = { onTrackClick(track) },
              onRemove = { onRemoveTrack(index) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QueueItemRow(
  track: Track,
  isCurrent: Boolean,
  onClick: () -> Unit,
  onRemove: (() -> Unit)?
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(if (isCurrent) MelodyXSurfaceHighlight else MelodyXDarkBg.copy(alpha = 0.5f))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(MelodyXSurfaceHighlight),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = track.artworkUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
      if (isCurrent) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MelodyXDarkBg.copy(alpha = 0.6f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Equalizer,
            contentDescription = null,
            tint = MelodyXGreen,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = track.title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
        ),
        color = if (isCurrent) MelodyXGreen else MelodyXTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = track.artist,
        style = MaterialTheme.typography.bodySmall,
        color = MelodyXTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    if (onRemove != null) {
      IconButton(
        onClick = onRemove,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Remove from queue",
          tint = MelodyXTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
