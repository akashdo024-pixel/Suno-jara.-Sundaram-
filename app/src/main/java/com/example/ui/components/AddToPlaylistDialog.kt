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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun AddToPlaylistDialog(
  track: Track?,
  playlists: List<Playlist>,
  onDismiss: () -> Unit,
  onSelectPlaylist: (Playlist) -> Unit,
  onCreateNewPlaylist: (String) -> Unit
) {
  if (track == null) return

  var showCreateField by remember { mutableStateOf(false) }
  var newPlaylistName by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MelodyXSurface,
    title = {
      Column {
        Text(
          text = "Add to Playlist",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MelodyXTextPrimary
        )
        Text(
          text = track.title,
          style = MaterialTheme.typography.bodySmall,
          color = MelodyXTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        if (showCreateField) {
          OutlinedTextField(
            value = newPlaylistName,
            onValueChange = { newPlaylistName = it },
            label = { Text("Playlist Name") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MelodyXGreen,
              unfocusedBorderColor = MelodyXBorder,
              focusedTextColor = MelodyXTextPrimary,
              unfocusedTextColor = MelodyXTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showCreateField = false }) {
              Text("Cancel", color = MelodyXTextMuted)
            }
            Button(
              onClick = {
                if (newPlaylistName.isNotBlank()) {
                  onCreateNewPlaylist(newPlaylistName)
                  newPlaylistName = ""
                  showCreateField = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen)
            ) {
              Text("Create", color = MelodyXSurface)
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        } else {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MelodyXSurfaceHighlight)
              .clickable { showCreateField = true }
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MelodyXGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Create New Playlist",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MelodyXGreen
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
        ) {
          items(playlists) { pl ->
            val containsTrack = pl.trackIds.contains(track.id)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onSelectPlaylist(pl)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(MelodyXSurfaceHighlight),
                contentAlignment = Alignment.Center
              ) {
                if (pl.coverUrl.isNotEmpty()) {
                  AsyncImage(
                    model = pl.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = MelodyXPurple,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = pl.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = MelodyXTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${pl.trackIds.size} songs",
                  style = MaterialTheme.typography.bodySmall,
                  color = MelodyXTextSecondary
                )
              }

              if (containsTrack) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = "Already in playlist",
                  tint = MelodyXGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = MelodyXTextPrimary)
      }
    }
  )
}
