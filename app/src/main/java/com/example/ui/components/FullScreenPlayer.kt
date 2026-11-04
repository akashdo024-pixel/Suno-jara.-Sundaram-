package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DownloadStatus
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXDarkBg
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
fun FullScreenPlayer(
  track: Track?,
  isPlaying: Boolean,
  isBuffering: Boolean,
  currentPositionMs: Int,
  durationMs: Int,
  isShuffle: Boolean,
  repeatMode: RepeatMode,
  volume: Float,
  onCollapseClick: () -> Unit,
  onPlayPauseClick: () -> Unit,
  onNextClick: () -> Unit,
  onPreviousClick: () -> Unit,
  onSeekTo: (Int) -> Unit,
  onToggleShuffle: () -> Unit,
  onCycleRepeat: () -> Unit,
  onFavoriteClick: () -> Unit,
  onDownloadClick: () -> Unit,
  onVolumeChange: (Float) -> Unit,
  onOpenQueueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler {
    onCollapseClick()
  }

  if (track == null) return

  var sliderPosition by remember { mutableFloatStateOf(0f) }
  var isUserSeeking by remember { mutableStateOf(false) }

  val effectivePosition = if (isUserSeeking) {
    sliderPosition
  } else {
    currentPositionMs.toFloat()
  }

  val maxDuration = if (durationMs > 0) durationMs.toFloat() else 180000f

  // Subtle continuous vinyl / ambient rotation animation when playing
  val infiniteTransition = rememberInfiniteTransition(label = "player_animation")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 24000, easing = FastOutSlowInEasing),
      repeatMode = AnimRepeatMode.Restart
    ),
    label = "vinyl_rotate"
  )

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MelodyXDarkBg
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MelodyXPurpleDeep.copy(alpha = 0.45f),
              MelodyXDarkBg,
              MelodyXDarkBg
            )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = onCollapseClick,
            modifier = Modifier
              .size(44.dp)
              .testTag("player_collapse_btn")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Collapse Player",
              tint = MelodyXTextPrimary,
              modifier = Modifier.size(32.dp)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "PLAYING FROM SUNO JARA",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              ),
              color = MelodyXTextMuted
            )
            Text(
              text = track.album.ifEmpty { "Trending Music" },
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MelodyXTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          IconButton(
            onClick = onOpenQueueClick,
            modifier = Modifier.size(44.dp)
          ) {
            Icon(
              imageVector = Icons.Default.QueueMusic,
              contentDescription = "Queue",
              tint = MelodyXTextPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        var showEmbeddedVideo by remember(track.id) {
          mutableStateOf(track.isYouTubeSource && track.youtubeVideoId != null)
        }

        if (track.youtubeVideoId != null) {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(MelodyXSurfaceHighlight)
              .padding(3.dp),
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (!showEmbeddedVideo) MelodyXGreen else Color.Transparent)
                .clickable { showEmbeddedVideo = false }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = "Audio",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (!showEmbeddedVideo) Color.Black else MelodyXTextSecondary
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (showEmbeddedVideo) Color(0xFFFF0000) else Color.Transparent)
                .clickable { showEmbeddedVideo = true }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.SmartDisplay,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "YouTube Video",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        if (showEmbeddedVideo && track.youtubeVideoId != null) {
          YouTubeEmbeddedPlayer(
            track = track,
            onClose = { showEmbeddedVideo = false },
            modifier = Modifier.padding(horizontal = 4.dp)
          )
        } else {
          // Center Artwork Card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .aspectRatio(1f)
              .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MelodyXPurple
              )
              .clip(RoundedCornerShape(20.dp))
              .background(MelodyXSurfaceHighlight),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = track.artworkUrl,
              contentDescription = "${track.title} cover",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Track Information Row with Like & Download Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = track.title,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
              ),
              color = MelodyXTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${track.artist} • ${track.genre}",
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
              color = MelodyXTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Download action
          IconButton(
            onClick = onDownloadClick,
            modifier = Modifier
              .size(44.dp)
              .testTag("fullscreen_download_btn")
          ) {
            when (track.downloadStatus) {
              DownloadStatus.DOWNLOADING -> {
                CircularProgressIndicator(
                  progress = { track.downloadProgress },
                  modifier = Modifier.size(24.dp),
                  color = MelodyXGreen,
                  strokeWidth = 2.5.dp
                )
              }
              DownloadStatus.DOWNLOADED -> {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Downloaded offline",
                  tint = MelodyXGreen,
                  modifier = Modifier.size(26.dp)
                )
              }
              else -> {
                Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = "Download for offline listening",
                  tint = MelodyXTextPrimary,
                  modifier = Modifier.size(26.dp)
                )
              }
            }
          }

          // Favorite Heart Button
          IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
              .size(44.dp)
              .testTag("fullscreen_fav_btn")
          ) {
            Icon(
              imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = if (track.isFavorite) "Unlike" else "Like",
              tint = if (track.isFavorite) MelodyXNeonPink else MelodyXTextMuted,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // License & Audio Stream Badge
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MelodyXSurfaceHighlight)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (track.downloadStatus == DownloadStatus.DOWNLOADED) "OFFLINE STORAGE (LOCAL)" else "AUTHORIZED STREAM • 320 KBPS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              ),
              color = if (track.downloadStatus == DownloadStatus.DOWNLOADED) MelodyXGreen else MelodyXPurple
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = "Licensed Preview/Master",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MelodyXTextMuted
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seek Bar Slider & Timestamps
        Column(modifier = Modifier.fillMaxWidth()) {
          Slider(
            value = effectivePosition,
            onValueChange = {
              isUserSeeking = true
              sliderPosition = it
            },
            onValueChangeFinished = {
              onSeekTo(sliderPosition.toInt())
              isUserSeeking = false
            },
            valueRange = 0f..maxDuration,
            colors = SliderDefaults.colors(
              thumbColor = MelodyXTextPrimary,
              activeTrackColor = MelodyXGreen,
              inactiveTrackColor = MelodyXBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("player_seek_slider")
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = formatTimeMs(effectivePosition.toInt()),
              style = MaterialTheme.typography.labelSmall,
              color = MelodyXTextMuted
            )
            Text(
              text = formatTimeMs(maxDuration.toInt()),
              style = MaterialTheme.typography.labelSmall,
              color = MelodyXTextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Player Controls Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Shuffle
          IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier.size(44.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Shuffle,
              contentDescription = "Shuffle",
              tint = if (isShuffle) MelodyXGreen else MelodyXTextMuted,
              modifier = Modifier.size(24.dp)
            )
          }

          // Previous
          IconButton(
            onClick = onPreviousClick,
            modifier = Modifier
              .size(52.dp)
              .testTag("player_prev_btn")
          ) {
            Icon(
              imageVector = Icons.Default.SkipPrevious,
              contentDescription = "Previous Track",
              tint = MelodyXTextPrimary,
              modifier = Modifier.size(36.dp)
            )
          }

          // Play / Pause (Big Neon Circle)
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(MelodyXGreen, MelodyXGreen)
                )
              )
              .shadow(12.dp, CircleShape, spotColor = MelodyXGreen)
              .clickable(onClick = onPlayPauseClick)
              .testTag("player_main_play_pause_btn"),
            contentAlignment = Alignment.Center
          ) {
            if (isBuffering) {
              CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color.Black,
                strokeWidth = 3.dp
              )
            } else {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(38.dp)
              )
            }
          }

          // Next
          IconButton(
            onClick = onNextClick,
            modifier = Modifier
              .size(52.dp)
              .testTag("player_next_btn")
          ) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next Track",
              tint = MelodyXTextPrimary,
              modifier = Modifier.size(36.dp)
            )
          }

          // Repeat Mode
          IconButton(
            onClick = onCycleRepeat,
            modifier = Modifier.size(44.dp)
          ) {
            val (icon, tint) = when (repeatMode) {
              RepeatMode.OFF -> Pair(Icons.Default.Repeat, MelodyXTextMuted)
              RepeatMode.ALL -> Pair(Icons.Default.Repeat, MelodyXGreen)
              RepeatMode.ONE -> Pair(Icons.Default.RepeatOne, MelodyXGreen)
            }
            Icon(
              imageVector = icon,
              contentDescription = "Repeat",
              tint = tint,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume Slider Control
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.VolumeDown,
            contentDescription = "Volume down",
            tint = MelodyXTextMuted,
            modifier = Modifier.size(20.dp)
          )
          Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
              thumbColor = MelodyXTextPrimary,
              activeTrackColor = MelodyXPurple,
              inactiveTrackColor = MelodyXBorder
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 8.dp)
          )
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume up",
            tint = MelodyXTextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

private fun formatTimeMs(millis: Int): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return "%d:%02d".format(minutes, seconds)
}
