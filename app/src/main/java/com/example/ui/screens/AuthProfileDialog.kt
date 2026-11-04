package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.MelodyXBorder
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXPurple
import com.example.ui.theme.MelodyXSurface
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@Composable
fun AuthProfileDialog(
  currentProfile: UserProfile,
  onDismiss: () -> Unit,
  onSaveProfile: (name: String, email: String) -> Unit
) {
  var name by remember { mutableStateOf(currentProfile.name) }
  var email by remember { mutableStateOf(currentProfile.email) }
  var isGoogleConnected by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MelodyXSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MelodyXGreen, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "User Profile & Account",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MelodyXTextPrimary
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Manage your Suno Jara profile details and linked sign-in credentials.",
          style = MaterialTheme.typography.bodySmall,
          color = MelodyXTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Google Sign-In linked banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MelodyXSurfaceHighlight)
            .border(1.dp, MelodyXBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Google Account Linked",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MelodyXTextPrimary
              )
              Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MelodyXGreen
              )
            }
            Icon(Icons.Default.Check, contentDescription = null, tint = MelodyXGreen, modifier = Modifier.size(20.dp))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Display Name") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MelodyXGreen,
            unfocusedBorderColor = MelodyXBorder,
            focusedTextColor = MelodyXTextPrimary,
            unfocusedTextColor = MelodyXTextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MelodyXGreen,
            unfocusedBorderColor = MelodyXBorder,
            focusedTextColor = MelodyXTextPrimary,
            unfocusedTextColor = MelodyXTextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && email.isNotBlank()) {
            onSaveProfile(name.trim(), email.trim())
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = MelodyXGreen)
      ) {
        Text("Save Changes", color = MelodyXDarkBg, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = MelodyXTextMuted)
      }
    }
  )
}
