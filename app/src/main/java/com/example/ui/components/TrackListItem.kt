package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DownloadStatus
import com.example.model.Track
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXNeonPink
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun TrackListItem(
  track: Track,
  isPlayingThisTrack: Boolean = false,
  onClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  onDownloadClick: () -> Unit,
  onAddToPlaylistClick: () -> Unit,
  onPlayNextClick: () -> Unit,
  onAddToQueueClick: () -> Unit,
  onWatchVideoClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("track_item_${track.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Artwork thumbnail
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(MelodyXSurfaceHighlight),
      contentAlignment = Alignment.Center
    ) {
      if (track.artworkUrl.isNotEmpty()) {
        AsyncImage(
          model = track.artworkUrl,
          contentDescription = "${track.title} artwork",
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(48.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Default.MusicNote,
          contentDescription = null,
          tint = MelodyXTextMuted,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Title and Artist
    Column(
      modifier = Modifier
        .weight(1f)
    ) {
      Text(
        text = track.title,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontWeight = if (isPlayingThisTrack) FontWeight.Bold else FontWeight.Medium,
          fontSize = 15.sp
        ),
        color = if (isPlayingThisTrack) MelodyXGreen else MelodyXTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
      ) {
        if (track.downloadStatus == DownloadStatus.DOWNLOADED) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Downloaded offline",
            tint = MelodyXGreen,
            modifier = Modifier
              .size(13.dp)
              .padding(end = 4.dp)
          )
        }
        if (track.isYouTubeSource || track.youtubeVideoId != null) {
          Box(
            modifier = Modifier
              .padding(end = 5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFFFF0000))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text("YT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
        }
        Text(
          text = "${track.artist} • ${track.channelTitle ?: track.language}",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = MelodyXTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Download indicator / button
    when (track.downloadStatus) {
      DownloadStatus.DOWNLOADING -> {
        Box(
          modifier = Modifier.size(36.dp),
          contentAlignment = Alignment.Center
        ) {
          val animatedProgress by animateFloatAsState(
            targetValue = track.downloadProgress,
            label = "dl_progress"
          )
          CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(18.dp),
            color = MelodyXGreen,
            strokeWidth = 2.dp
          )
        }
      }
      DownloadStatus.DOWNLOADED -> {
        IconButton(
          onClick = onDownloadClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("download_status_${track.id}")
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Downloaded",
            tint = MelodyXGreen,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      else -> {
        // Not downloaded
      }
    }

    // Favorite button
    IconButton(
      onClick = onFavoriteClick,
      modifier = Modifier
        .size(36.dp)
        .testTag("fav_button_${track.id}")
    ) {
      Icon(
        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = if (track.isFavorite) "Unlike" else "Like",
        tint = if (track.isFavorite) MelodyXNeonPink else MelodyXTextMuted,
        modifier = Modifier.size(20.dp)
      )
    }

    // Overflow menu
    Box {
      IconButton(
        onClick = { showMenu = true },
        modifier = Modifier
          .size(36.dp)
          .testTag("menu_button_${track.id}")
      ) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "More options",
          tint = MelodyXTextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }

      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.background(MelodyXSurfaceHighlight)
      ) {
        DropdownMenuItem(
          text = { Text("Play Next", color = MelodyXTextPrimary) },
          onClick = {
            showMenu = false
            onPlayNextClick()
          }
        )
        DropdownMenuItem(
          text = { Text("Add to Queue", color = MelodyXTextPrimary) },
          onClick = {
            showMenu = false
            onAddToQueueClick()
          }
        )
        DropdownMenuItem(
          text = { Text("Add to Playlist", color = MelodyXTextPrimary) },
          onClick = {
            showMenu = false
            onAddToPlaylistClick()
          }
        )
        if (track.youtubeVideoId != null && onWatchVideoClick != null) {
          DropdownMenuItem(
            text = { Text("Watch Official Video", color = Color(0xFFFF4E4E), fontWeight = FontWeight.SemiBold) },
            onClick = {
              showMenu = false
              onWatchVideoClick()
            }
          )
        }
        DropdownMenuItem(
          text = {
            Text(
              if (track.downloadStatus == DownloadStatus.DOWNLOADED) "Remove from Downloads" else "Download Offline",
              color = MelodyXTextPrimary
            )
          },
          onClick = {
            showMenu = false
            onDownloadClick()
          }
        )
      }
    }
  }
}
