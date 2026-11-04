package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.TrackListItem
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
import java.util.Calendar

@Composable
fun HomeScreen(
  tracks: List<Track>,
  playlists: List<Playlist>,
  artists: List<Artist>,
  albums: List<Album>,
  currentTrack: Track?,
  isPlaying: Boolean,
  selectedLanguage: String,
  onLanguageSelected: (String) -> Unit,
  youTubeTrendingTracks: List<Track> = emptyList(),
  youTubeRecentReleases: List<Track> = emptyList(),
  isYouTubeQuotaExceeded: Boolean = false,
  youTubeErrorMessage: String? = null,
  onYouTubePlayClick: ((Track) -> Unit)? = null,
  onTrackClick: (Track, List<Track>) -> Unit,
  onPlaylistClick: (Playlist) -> Unit,
  onArtistClick: (Artist) -> Unit,
  onFavoriteClick: (Track) -> Unit,
  onDownloadClick: (Track) -> Unit,
  onAddToPlaylistClick: (Track) -> Unit,
  onPlayNextClick: (Track) -> Unit,
  onAddToQueueClick: (Track) -> Unit,
  onOpenAdminClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val greeting = remember {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    when (hour) {
      in 4..11 -> "Good morning"
      in 12..17 -> "Good afternoon"
      else -> "Good evening"
    }
  }

  val languages = listOf(
    "All", "Hindi", "English", "Punjabi", "Tamil", "Telugu",
    "Malayalam", "Bengali", "Bhojpuri", "International"
  )

  val filteredTracks = if (selectedLanguage == "All") {
    tracks
  } else {
    tracks.filter { it.language.equals(selectedLanguage, ignoreCase = true) }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .testTag("home_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    // Header Greeting & Admin Icon
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(MelodyXPurpleDeep.copy(alpha = 0.35f), MelodyXDarkBg)
            )
          )
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = greeting,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
              ),
              color = MelodyXTextPrimary
            )
            Text(
              text = "Discover today's trending music & authorized downloads",
              style = MaterialTheme.typography.bodySmall,
              color = MelodyXTextSecondary
            )
          }

          IconButton(
            onClick = onOpenAdminClick,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MelodyXSurfaceHighlight)
              .testTag("admin_header_btn")
          ) {
            Icon(
              imageVector = Icons.Default.AdminPanelSettings,
              contentDescription = "Admin Dashboard",
              tint = MelodyXPurple,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }

    // Quick Jump Grid (Recently Played / Liked / Top Hits)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickJumpCard(
          title = "Liked Songs",
          iconColor = MelodyXNeonPink,
          onClick = {
            val favPlaylist = Playlist("pl_liked", "Liked Songs", "Favorite tracks", "", emptyList())
            onPlaylistClick(favPlaylist)
          },
          modifier = Modifier.weight(1f)
        )
        if (playlists.isNotEmpty()) {
          QuickJumpCard(
            title = playlists.first().name,
            imageUrl = playlists.first().coverUrl,
            onClick = { onPlaylistClick(playlists.first()) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Language / Genre Filter Chips
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        languages.forEach { lang ->
          val isSelected = selectedLanguage == lang
          FilterChip(
            selected = isSelected,
            onClick = { onLanguageSelected(lang) },
            label = {
              Text(
                text = lang,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MelodyXGreen,
              selectedLabelColor = Color.Black,
              containerColor = MelodyXSurfaceHighlight,
              labelColor = MelodyXTextPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = MelodyXBorder,
              selectedBorderColor = MelodyXGreen
            ),
            modifier = Modifier.testTag("filter_chip_$lang")
          )
        }
      }
    }

    // YouTube Quota / Status Notice if any
    if (isYouTubeQuotaExceeded || youTubeErrorMessage != null) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MelodyXSurfaceHighlight)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MelodyXGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = youTubeErrorMessage ?: "YouTube Music integrated • High-speed streaming ready",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MelodyXTextSecondary
            )
          }
        }
      }
    }

    // Trending on YouTube Music
    if (youTubeTrendingTracks.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(6.dp))
        SectionTitle(
          title = "Trending on YouTube Music",
          subtitle = "Top official music videos & chartbusters"
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          items(youTubeTrendingTracks) { track ->
            YouTubeVideoCard(
              track = track,
              onClick = {
                if (onYouTubePlayClick != null) {
                  onYouTubePlayClick(track)
                } else {
                  onTrackClick(track, youTubeTrendingTracks)
                }
              }
            )
          }
        }
      }
    }

    // Recently Released on YouTube
    if (youTubeRecentReleases.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        SectionTitle(
          title = "Fresh Drops on YouTube",
          subtitle = "Recently released official music videos"
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          items(youTubeRecentReleases) { track ->
            YouTubeVideoCard(
              track = track,
              onClick = {
                if (onYouTubePlayClick != null) {
                  onYouTubePlayClick(track)
                } else {
                  onTrackClick(track, youTubeRecentReleases)
                }
              }
            )
          }
        }
      }
    }

    // Trending Songs Carousel
    item {
      SectionTitle(title = "Trending Now", subtitle = "Most streamed in India & Worldwide")
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        items(filteredTracks.take(8)) { track ->
          TrendingSongCard(
            track = track,
            isPlayingThis = currentTrack?.id == track.id && isPlaying,
            onClick = { onTrackClick(track, filteredTracks) }
          )
        }
      }
    }

    // Popular Playlists
    item {
      Spacer(modifier = Modifier.height(12.dp))
      SectionTitle(title = "Featured Playlists", subtitle = "Curated for every mood and beat")
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        items(playlists) { playlist ->
          PlaylistCardItem(
            playlist = playlist,
            onClick = { onPlaylistClick(playlist) }
          )
        }
      }
    }

    // Top Artists
    item {
      Spacer(modifier = Modifier.height(12.dp))
      SectionTitle(title = "Top Artists", subtitle = "Trending singers and composers")
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        items(artists) { artist ->
          ArtistCircleItem(
            artist = artist,
            onClick = { onArtistClick(artist) }
          )
        }
      }
    }

    // New Albums
    item {
      Spacer(modifier = Modifier.height(12.dp))
      SectionTitle(title = "New Releases & Albums", subtitle = "Fresh music drops")
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        items(albums) { album ->
          AlbumCardItem(
            album = album,
            onClick = {
              val albumTracks = tracks.filter { it.album == album.title }
              val pl = Playlist(
                id = album.id,
                name = album.title,
                description = "Album by ${album.artist} • ${album.releaseYear}",
                coverUrl = album.coverUrl,
                trackIds = albumTracks.map { it.id }
              )
              onPlaylistClick(pl)
            }
          )
        }
      }
    }

    // Recommended Songs List
    item {
      Spacer(modifier = Modifier.height(16.dp))
      SectionTitle(title = "Recommended For You", subtitle = "Based on your musical taste")
    }

    items(filteredTracks) { track ->
      TrackListItem(
        track = track,
        isPlayingThisTrack = currentTrack?.id == track.id,
        onClick = { onTrackClick(track, filteredTracks) },
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

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
  Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
      ),
      color = MelodyXTextPrimary
    )
    if (subtitle != null) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MelodyXTextSecondary
      )
    }
  }
}

@Composable
fun QuickJumpCard(
  title: String,
  imageUrl: String? = null,
  iconColor: Color? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .height(56.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(MelodyXSurfaceHighlight)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .background(
          brush = Brush.linearGradient(
            colors = listOf(iconColor ?: MelodyXPurple, MelodyXSurface)
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      if (imageUrl != null) {
        AsyncImage(
          model = imageUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          Icons.Default.Favorite,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    }
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MelodyXTextPrimary,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp)
    )
  }
}

@Composable
fun TrendingSongCard(
  track: Track,
  isPlayingThis: Boolean,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(136.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(128.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MelodyXSurfaceHighlight),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = track.artworkUrl,
        contentDescription = track.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      if (isPlayingThis) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MelodyXGreen,
            modifier = Modifier.size(36.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = track.title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = if (isPlayingThis) MelodyXGreen else MelodyXTextPrimary,
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
}

@Composable
fun PlaylistCardItem(
  playlist: Playlist,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(140.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(132.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MelodyXSurfaceHighlight)
    ) {
      AsyncImage(
        model = playlist.coverUrl,
        contentDescription = playlist.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = playlist.name,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MelodyXTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = "${playlist.trackIds.size} songs",
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
      color = MelodyXTextSecondary,
      maxLines = 1
    )
  }
}

@Composable
fun ArtistCircleItem(
  artist: Artist,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(96.dp)
      .clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(88.dp)
        .clip(CircleShape)
        .background(MelodyXSurfaceHighlight)
    ) {
      AsyncImage(
        model = artist.imageUrl,
        contentDescription = artist.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = artist.name,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
      color = MelodyXTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = "Artist",
      style = MaterialTheme.typography.labelSmall,
      color = MelodyXTextMuted
    )
  }
}

@Composable
fun AlbumCardItem(
  album: Album,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(136.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(128.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MelodyXSurfaceHighlight)
    ) {
      AsyncImage(
        model = album.coverUrl,
        contentDescription = album.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = album.title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MelodyXTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = "${album.artist} • ${album.releaseYear}",
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
      color = MelodyXTextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun YouTubeVideoCard(
  track: Track,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(176.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(MelodyXSurfaceHighlight),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = track.artworkUrl,
        contentDescription = track.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // YouTube red badge top-left
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(6.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFFFF0000))
          .padding(horizontal = 5.dp, vertical = 2.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.SmartDisplay,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(11.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "YouTube",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }
      }

      // Play icon button overlay center
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Play Video",
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = track.title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
      color = MelodyXTextPrimary,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = track.channelTitle ?: track.artist,
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
      color = MelodyXTextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

