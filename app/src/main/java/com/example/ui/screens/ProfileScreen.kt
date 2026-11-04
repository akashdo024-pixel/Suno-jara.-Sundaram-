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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.UserProfile
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
fun ProfileScreen(
  userProfile: UserProfile,
  likedSongsCount: Int,
  downloadedTracksCount: Int,
  onOpenAdminClick: () -> Unit,
  onOpenAuthDialog: () -> Unit,
  onToggleOfflineMode: (Boolean) -> Unit,
  onChangeQuality: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MelodyXDarkBg)
      .padding(horizontal = 16.dp)
      .testTag("profile_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Profile & Settings",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp
        ),
        color = MelodyXTextPrimary
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // User Avatar & Plan Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(MelodyXPurpleDeep.copy(alpha = 0.3f), MelodyXSurfaceHighlight)
              )
            )
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(MelodyXPurple)
          ) {
            AsyncImage(
              model = userProfile.avatarUrl,
              contentDescription = "User Avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = userProfile.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MelodyXTextPrimary
          )
          Text(
            text = userProfile.email,
            style = MaterialTheme.typography.bodySmall,
            color = MelodyXTextSecondary
          )

          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(MelodyXGreen)
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = MelodyXDarkBg, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = userProfile.membershipTier,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MelodyXDarkBg
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onOpenAuthDialog,
            colors = ButtonDefaults.buttonColors(containerColor = MelodyXSurface),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text("Switch Account / Edit Profile", color = MelodyXTextPrimary, fontSize = 12.sp)
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Listening Stats Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatCard(
          title = "${userProfile.totalMinutesListened} mins",
          subtitle = "Total Listened",
          icon = Icons.Default.Headphones,
          color = MelodyXGreen,
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "$likedSongsCount tracks",
          subtitle = "Liked Songs",
          icon = Icons.Default.Equalizer,
          color = MelodyXNeonPink,
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "$downloadedTracksCount offline",
          subtitle = "Downloaded",
          icon = Icons.Default.DownloadDone,
          color = MelodyXPurple,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Audio Quality & Streaming Settings
    item {
      Text(
        text = "Audio & Playback Quality",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MelodyXTextPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          val qualities = listOf(
            "Standard (160 kbps)" to "Data saver mode",
            "Very High (320 kbps)" to "Crisp studio fidelity (Recommended)",
            "Hi-Fi Lossless (FLAC)" to "Pure uncompressed master quality"
          )

          qualities.forEach { (quality, desc) ->
            val isSelected = userProfile.streamQuality == quality
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChangeQuality(quality) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = quality,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) MelodyXGreen else MelodyXTextPrimary
                )
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MelodyXTextSecondary)
              }
              if (isSelected) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = MelodyXGreen, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Offline Mode Only Toggle
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Offline Mode Only",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = MelodyXTextPrimary
            )
            Text(
              text = "When active, Suno Jara only plays music saved to offline storage.",
              style = MaterialTheme.typography.bodySmall,
              color = MelodyXTextSecondary
            )
          }
          Switch(
            checked = userProfile.offlineOnly,
            onCheckedChange = onToggleOfflineMode,
            colors = SwitchDefaults.colors(
              checkedThumbColor = MelodyXGreen,
              checkedTrackColor = MelodyXSurface,
              uncheckedThumbColor = MelodyXTextMuted,
              uncheckedTrackColor = MelodyXBorder
            )
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Admin Dashboard Button
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .clickable(onClick = onOpenAdminClick)
          .testTag("admin_dashboard_tile"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MelodyXPurpleDeep),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.AdminPanelSettings,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Suno Jara Admin Dashboard",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
              color = MelodyXTextPrimary
            )
            Text(
              text = "Manage music catalog, metadata, licenses & analytics",
              style = MaterialTheme.typography.bodySmall,
              color = MelodyXTextSecondary
            )
          }

          Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MelodyXTextMuted
          )
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
        color = MelodyXTextPrimary,
        maxLines = 1
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MelodyXTextSecondary
      )
    }
  }
}
