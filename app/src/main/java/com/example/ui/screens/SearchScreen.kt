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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class GenreBrowseCard(val name: String, val colors: List<Color>)

@Composable
fun SearchScreen(
  query: String,
  isSearching: Boolean,
  searchResults: List<Track>,
  catalogTracks: List<Track>,
  currentTrack: Track?,
  selectedFilter: String,
  onQueryChange: (String) -> Unit,
  onFilterChange: (String) -> Unit,
  onTrackClick: (Track, List<Track>) -> Unit,
  onFavoriteClick: (Track) -> Unit,
  onDownloadClick: (Track) -> Unit,
  onAddToPlaylistClick: (Track) -> Unit,
  onPlayNextClick: (Track) -> Unit,
  onAddToQueueClick: (Track) -> Unit,
  onWatchVideoClick: ((Track) -> Unit)? = null,
  isQuotaExceeded: Boolean = false,
  errorMessage: String? = null,
  modifier: Modifier = Modifier
) {
  val trendingTags = listOf(
    "Arijit Singh", "Diljit Dosanjh", "Taylor Swift", "The Weeknd",
    "Tamil Beats", "Punjabi Pop", "Bhojpuri Hits", "Synthwave"
  )

  val browseGenres = listOf(
    GenreBrowseCard("Pop & Hits", listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
    GenreBrowseCard("Bollywood & Desi", listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
    GenreBrowseCard("Punjabi Hip-Hop", listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
    GenreBrowseCard("South Wave", listOf(Color(0xFF10B981), Color(0xFF047857))),
    GenreBrowseCard("Chill & Acoustic", listOf(Color(0xFF06B6D4), Color(0xFF0284C7))),
    GenreBrowseCard("Workout & Energy", listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
    GenreBrowseCard("Romantic Melodies", listOf(Color(0xFFF43F5E), Color(0xFF9F1239))),
    GenreBrowseCard("International", listOf(Color(0xFF6366F1), Color(0xFF4338CA)))
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .padding(horizontal = 16.dp)
      .testTag("search_screen")
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Search",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
      ),
      color = MelodyXTextPrimary
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Search Input Bar
    OutlinedTextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .testTag("search_text_input"),
      placeholder = {
        Text("Songs, artists, albums, or genres...", color = MelodyXTextMuted)
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = if (query.isNotEmpty()) MelodyXGreen else MelodyXTextMuted
        )
      },
      trailingIcon = {
        if (isSearching) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = MelodyXGreen,
            strokeWidth = 2.dp
          )
        } else if (query.isNotEmpty()) {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              tint = MelodyXTextSecondary
            )
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MelodyXGreen,
        unfocusedBorderColor = MelodyXBorder,
        focusedContainerColor = MelodyXSurfaceHighlight,
        unfocusedContainerColor = MelodyXSurfaceHighlight,
        focusedTextColor = MelodyXTextPrimary,
        unfocusedTextColor = MelodyXTextPrimary
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips Row
    val filterChips = listOf("All", "Songs", "Artists", "Bollywood", "International")
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      filterChips.forEach { chip ->
        val selected = selectedFilter == chip
        FilterChip(
          selected = selected,
          onClick = { onFilterChange(chip) },
          label = { Text(chip, fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MelodyXPurple,
            selectedLabelColor = Color.White,
            containerColor = MelodyXSurface,
            labelColor = MelodyXTextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MelodyXBorder,
            selectedBorderColor = MelodyXPurple
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (query.isEmpty()) {
      // Empty query state: trending search tags + browse genres
      LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        item {
          Text(
            text = "Trending Searches",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
          )

          // Tag Pills
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            trendingTags.take(4).forEach { tag ->
              TagPill(text = tag, onClick = { onQueryChange(tag) })
            }
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            trendingTags.drop(4).take(4).forEach { tag ->
              TagPill(text = tag, onClick = { onQueryChange(tag) })
            }
          }

          Text(
            text = "Browse All Genres",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }

        // Browse Genre Cards Grid
        item {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            browseGenres.chunked(2).forEach { rowCards ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                rowCards.forEach { card ->
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(88.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(brush = Brush.linearGradient(colors = card.colors))
                      .clickable { onQueryChange(card.name) }
                      .padding(14.dp),
                    contentAlignment = Alignment.TopStart
                  ) {
                    Text(
                      text = card.name,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                      ),
                      color = Color.White
                    )
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // Results list
      val displayResults = if (searchResults.isNotEmpty()) {
        searchResults
      } else {
        // Fallback filter over catalog
        catalogTracks.filter {
          it.title.contains(query, ignoreCase = true) ||
              it.artist.contains(query, ignoreCase = true) ||
              it.genre.contains(query, ignoreCase = true) ||
              it.language.contains(query, ignoreCase = true)
        }
      }

      LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        item {
          Text(
            text = "${displayResults.size} matching results",
            style = MaterialTheme.typography.labelMedium,
            color = MelodyXTextMuted,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }

        if (displayResults.isEmpty() && !isSearching) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "No songs found for '$query'",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = MelodyXTextPrimary
                )
                Text(
                  text = "Try searching for a different artist, track, or language",
                  style = MaterialTheme.typography.bodySmall,
                  color = MelodyXTextSecondary,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }

        if (isQuotaExceeded || errorMessage != null) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MelodyXSurfaceHighlight)
                .padding(10.dp)
            ) {
              Text(
                text = errorMessage ?: "YouTube quota status: Showing cached music search",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MelodyXTextSecondary
              )
            }
          }
        }

        items(displayResults) { track ->
          TrackListItem(
            track = track,
            isPlayingThisTrack = currentTrack?.id == track.id,
            onClick = {
              if (track.isYouTubeSource && onWatchVideoClick != null) {
                onWatchVideoClick(track)
              } else {
                onTrackClick(track, displayResults)
              }
            },
            onFavoriteClick = { onFavoriteClick(track) },
            onDownloadClick = { onDownloadClick(track) },
            onAddToPlaylistClick = { onAddToPlaylistClick(track) },
            onPlayNextClick = { onPlayNextClick(track) },
            onAddToQueueClick = { onAddToQueueClick(track) },
            onWatchVideoClick = if (track.youtubeVideoId != null) {
              { onWatchVideoClick?.invoke(track) }
            } else null
          )
        }
      }
    }
  }
}

@Composable
private fun TagPill(text: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(MelodyXSurfaceHighlight)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      color = MelodyXTextPrimary
    )
  }
}
