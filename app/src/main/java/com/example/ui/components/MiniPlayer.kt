package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.Track
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXNeonPink
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun MiniPlayer(
  track: Track?,
  isPlaying: Boolean,
  isBuffering: Boolean,
  currentPositionMs: Int,
  durationMs: Int,
  onPlayPauseClick: () -> Unit,
  onNextClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  onExpandClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = track != null,
    enter = slideInVertically { it },
    exit = slideOutVertically { it },
    modifier = modifier
  ) {
    if (track == null) return@AnimatedVisibility

    val progress = if (durationMs > 0) {
      (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else 0f

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(MelodyXSurface, MelodyXSurfaceHighlight)
          )
        )
        .border(1.dp, MelodyXBorder, RoundedCornerShape(12.dp))
        .clickable(onClick = onExpandClick)
        .testTag("mini_player")
    ) {
      Column {
        // Slim progress line on top of the mini player
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp),
          color = MelodyXGreen,
          trackColor = Color.Transparent
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Album thumbnail
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MelodyXSurfaceHighlight),
            contentAlignment = Alignment.Center
          ) {
            if (track.artworkUrl.isNotEmpty()) {
              AsyncImage(
                model = track.artworkUrl,
                contentDescription = "${track.title} artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MelodyXTextMuted,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          // Song title & artist
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = track.title,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
              ),
              color = MelodyXTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = track.artist,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = MelodyXTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Favorite button
          IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("mini_fav_btn")
          ) {
            Icon(
              imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (track.isFavorite) MelodyXNeonPink else MelodyXTextMuted,
              modifier = Modifier.size(20.dp)
            )
          }

          // Play/Pause button
          IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(MelodyXGreen)
              .testTag("mini_play_pause_btn")
          ) {
            if (isBuffering) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.Black,
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Next track button
          IconButton(
            onClick = onNextClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("mini_next_btn")
          ) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next",
              tint = MelodyXTextPrimary,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}
