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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXNeonPink
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXPurpleDeep
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

data class CategoryCardData(
  val title: String,
  val subtitle: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val gradient: List<Color>,
  val filterTag: String
)

@Composable
fun ExploreScreen(
  tracks: List<Track>,
  onCategorySelected: (String) -> Unit,
  onPlaylistClick: (Playlist) -> Unit,
  modifier: Modifier = Modifier
) {
  val languageHubs = listOf(
    CategoryCardData("Hindi Hits", "Arijit, Badshah, Shreya", Icons.Default.Language, listOf(Color(0xFFEC4899), Color(0xFF831843)), "Hindi"),
    CategoryCardData("Punjabi Beats", "Diljit, AP Dhillon, Karan", Icons.Default.Whatshot, listOf(Color(0xFFF59E0B), Color(0xFFB45309)), "Punjabi"),
    CategoryCardData("Tamil Wave", "Anirudh, AR Rahman, Sid", Icons.Default.Equalizer, listOf(Color(0xFF10B981), Color(0xFF065F46)), "Tamil"),
    CategoryCardData("Telugu Melodies", "DSP, Thaman S, Anurag", Icons.Default.Equalizer, listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6)), "Telugu"),
    CategoryCardData("Malayalam Chill", "Sushin Shyam, Jakes", Icons.Default.Language, listOf(Color(0xFF06B6D4), Color(0xFF0E7490)), "Malayalam"),
    CategoryCardData("Bengali Melodies", "Anupam Roy, Iman", Icons.Default.Language, listOf(Color(0xFFF43F5E), Color(0xFF881337)), "Bengali"),
    CategoryCardData("Bhojpuri Dance", "Pawan Singh, Khesari", Icons.Default.Whatshot, listOf(Color(0xFFEA580C), Color(0xFF9A3412)), "Bhojpuri"),
    CategoryCardData("International", "Taylor, The Weeknd, Dua", Icons.Default.Explore, listOf(Color(0xFF3B82F6), Color(0xFF1E3A8A)), "International")
  )

  val moods = listOf(
    CategoryCardData("Workout Energy", "130+ BPM high beats", Icons.Default.Whatshot, listOf(Color(0xFFEF4444), Color(0xFF7F1D1D)), "Workout"),
    CategoryCardData("Deep Focus", "Ambient, lofi & binaural", Icons.Default.Equalizer, listOf(Color(0xFF6366F1), Color(0xFF312E81)), "Focus"),
    CategoryCardData("Midnight Chill", "Slowed & reverbed calm", Icons.Default.Equalizer, listOf(Color(0xFF14B8A6), Color(0xFF134E4A)), "Chill"),
    CategoryCardData("Romantic Melodies", "Heartfelt love songs", Icons.Default.Language, listOf(Color(0xFFFB7185), Color(0xFF9F1239)), "Romantic")
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .padding(horizontal = 16.dp)
      .testTag("explore_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Explore & Discovery",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp
        ),
        color = MelodyXTextPrimary
      )
      Text(
        text = "Curated collections, regional languages & mood stations",
        style = MaterialTheme.typography.bodySmall,
        color = MelodyXTextSecondary,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
      )
    }

    // Language Hubs Section
    item {
      SectionTitle(title = "Languages of India & World", subtitle = "Tap to explore authentic native playlists")
      Spacer(modifier = Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        languageHubs.chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowItems.forEach { cat ->
              ExploreCard(
                category = cat,
                onClick = { onCategorySelected(cat.filterTag) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Moods & Activity Radios
    item {
      Spacer(modifier = Modifier.height(24.dp))
      SectionTitle(title = "Moods & Activities", subtitle = "Soundtracks matched to your day")
      Spacer(modifier = Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        moods.chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowItems.forEach { cat ->
              ExploreCard(
                category = cat,
                onClick = { onCategorySelected(cat.filterTag) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Charts Feature Card
    item {
      Spacer(modifier = Modifier.height(24.dp))
      SectionTitle(title = "Official Charts", subtitle = "Updated daily by Suno Jara listeners")
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(
            brush = Brush.horizontalGradient(
              colors = listOf(MelodyXPurpleDeep, MelodyXGreen)
            )
          )
          .clickable { onCategorySelected("Top Charts") }
          .padding(20.dp)
      ) {
        Column {
          Text(
            text = "SUNO JARA TOP 50 • INDIA & GLOBAL",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            ),
            color = Color.White.copy(alpha = 0.8f)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Top Viral Chartbusters",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            ),
            color = Color.White
          )
          Text(
            text = "Stream the most played songs across all 9 regional languages right now.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ExploreCard(
  category: CategoryCardData,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(100.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(brush = Brush.linearGradient(colors = category.gradient))
      .clickable(onClick = onClick)
      .padding(14.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = category.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          ),
          color = Color.White,
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = category.icon,
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.8f),
          modifier = Modifier.size(20.dp)
        )
      }

      Text(
        text = category.subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = Color.White.copy(alpha = 0.85f),
        maxLines = 1
      )
    }
  }
}
