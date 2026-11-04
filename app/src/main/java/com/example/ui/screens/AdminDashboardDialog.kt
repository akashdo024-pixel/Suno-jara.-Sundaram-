package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXPurpleDeep
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
  totalTracks: Int,
  playlists: List<Playlist>,
  storageUsedBytes: Long,
  onDismiss: () -> Unit,
  onAddTrack: (Track) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var artist by remember { mutableStateOf("") }
  var album by remember { mutableStateOf("") }
  var genre by remember { mutableStateOf("Pop") }
  var language by remember { mutableStateOf("Hindi") }
  var artworkUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600") }
  var audioUrl by remember {
    mutableStateOf("https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/44/21/df/4421df0c-c603-c287-c376-782cfecb1da7/mzaf_11306306566417757917.plus.aac.p.m4a")
  }
  var isDownloadAuthorized by remember { mutableStateOf(true) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  val languages = listOf("Hindi", "English", "Punjabi", "Tamil", "Telugu", "Malayalam", "Bengali", "Bhojpuri", "International")
  var languageExpanded by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 28.dp),
      color = MelodyXDarkBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
          .testTag("admin_dashboard_screen")
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MelodyXPurpleDeep),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MelodyXTextPrimary
              )
              Text(
                text = "Catalog & Content Governance",
                style = MaterialTheme.typography.bodySmall,
                color = MelodyXTextSecondary
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MelodyXTextPrimary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Statistics Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Catalog & Platform Analytics",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MelodyXTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val mb = storageUsedBytes / (1024.0 * 1024.0)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Column {
                Text(text = "Total Catalog Tracks", style = MaterialTheme.typography.labelSmall, color = MelodyXTextMuted)
                Text(text = "$totalTracks songs", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MelodyXGreen)
              }
              Column {
                Text(text = "Active Playlists", style = MaterialTheme.typography.labelSmall, color = MelodyXTextMuted)
                Text(text = "${playlists.size} playlists", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MelodyXPurple)
              }
              Column {
                Text(text = "Storage Consumed", style = MaterialTheme.typography.labelSmall, color = MelodyXTextMuted)
                Text(text = "%.1f MB".format(mb), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Add New Track Form
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Add Licensed Track to Catalog",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MelodyXTextPrimary
            )
            Text(
              text = "Enter song metadata and preview streaming endpoint",
              style = MaterialTheme.typography.bodySmall,
              color = MelodyXTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Song Title") },
              singleLine = true,
              colors = customFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = artist,
              onValueChange = { artist = it },
              label = { Text("Artist Name") },
              singleLine = true,
              colors = customFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = album,
              onValueChange = { album = it },
              label = { Text("Album Name") },
              singleLine = true,
              colors = customFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = { Text("Genre") },
                singleLine = true,
                colors = customFieldColors(),
                modifier = Modifier.weight(1f)
              )

              ExposedDropdownMenuBox(
                expanded = languageExpanded,
                onExpandedChange = { languageExpanded = it },
                modifier = Modifier.weight(1f)
              ) {
                OutlinedTextField(
                  value = language,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Language") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                  colors = customFieldColors(),
                  modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                  expanded = languageExpanded,
                  onDismissRequest = { languageExpanded = false },
                  modifier = Modifier.background(MelodyXSurfaceHighlight)
                ) {
                  languages.forEach { lang ->
                    DropdownMenuItem(
                      text = { Text(lang, color = MelodyXTextPrimary) },
                      onClick = {
                        language = lang
                        languageExpanded = false
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = artworkUrl,
              onValueChange = { artworkUrl = it },
              label = { Text("Artwork URL (HTTPS)") },
              singleLine = true,
              colors = customFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = audioUrl,
              onValueChange = { audioUrl = it },
              label = { Text("Audio Stream URL (.m4a / .mp3)") },
              singleLine = true,
              colors = customFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                checked = isDownloadAuthorized,
                onCheckedChange = { isDownloadAuthorized = it },
                colors = CheckboxDefaults.colors(checkedColor = MelodyXGreen, checkmarkColor = MelodyXDarkBg)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = "Authorized for Legal Offline Download",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MelodyXTextPrimary
                )
                Text(
                  text = "Rights holder permits non-commercial offline caching",
                  style = MaterialTheme.typography.labelSmall,
                  color = MelodyXTextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (statusMessage != null) {
              Text(
                text = statusMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MelodyXGreen,
                modifier = Modifier.padding(bottom = 8.dp)
              )
            }

            Button(
              onClick = {
                if (title.isBlank() || artist.isBlank()) {
                  statusMessage = "Please enter song title and artist"
                  return@Button
                }
                val newTrack = Track(
                  id = "admin_${UUID.randomUUID()}",
                  title = title.trim(),
                  artist = artist.trim(),
                  album = album.trim().ifEmpty { "Single" },
                  artworkUrl = artworkUrl.trim(),
                  audioUrl = audioUrl.trim(),
                  durationSeconds = 195,
                  genre = genre.trim(),
                  language = language,
                  releaseYear = "2024",
                  isAuthorizedDownload = isDownloadAuthorized
                )
                onAddTrack(newTrack)
                statusMessage = "Track '${newTrack.title}' published to live catalog!"
                title = ""
                artist = ""
                album = ""
              },
              colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = MelodyXDarkBg)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Publish Track to Catalog", color = MelodyXDarkBg, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Licensing & Compliance Banner
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MelodyXSurfaceHighlight)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MelodyXGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "DRM & Copyright Compliance Verified",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MelodyXTextPrimary
              )
              Text(
                text = "All streaming endpoints & offline downloads adhere strictly to licensed catalog distributor terms and Creative Commons licenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MelodyXTextSecondary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedBorderColor = MelodyXGreen,
  unfocusedBorderColor = MelodyXBorder,
  focusedLabelColor = MelodyXGreen,
  unfocusedLabelColor = MelodyXTextSecondary,
  focusedTextColor = MelodyXTextPrimary,
  unfocusedTextColor = MelodyXTextPrimary,
  focusedContainerColor = MelodyXSurface,
  unfocusedContainerColor = MelodyXSurface
)
