package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Track
import com.example.ui.theme.MelodyXDarkBg
import com.example.ui.theme.MelodyXGreen
import com.example.ui.theme.MelodyXSurfaceHighlight
import com.example.ui.theme.MelodyXTextMuted
import com.example.ui.theme.MelodyXTextPrimary
import com.example.ui.theme.MelodyXTextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEmbeddedPlayer(
  track: Track,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val videoId = track.youtubeVideoId ?: return
  var isLoading by remember { mutableStateOf(true) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  // Clean lifecycle handling for WebView
  DisposableEffect(videoId) {
    onDispose {
      webViewRef?.apply {
        loadUrl("about:blank")
        stopLoading()
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(MelodyXDarkBg)
      .testTag("youtube_embedded_player")
  ) {
    // Header with YouTube badge and track details
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFF0000))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.SmartDisplay,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "YouTube",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MelodyXTextPrimary,
            maxLines = 1,
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

      if (onClose != null) {
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close Video", tint = MelodyXTextPrimary, modifier = Modifier.size(18.dp))
        }
      }
    }

    // Video Player Box (16:9 ratio)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .background(Color.Black),
      contentAlignment = Alignment.Center
    ) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(AndroidColor.BLACK)

            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              mediaPlaybackRequiresUserGesture = false
              loadWithOverviewMode = true
              useWideViewPort = true
              allowContentAccess = true
              cacheMode = WebSettings.LOAD_DEFAULT
            }

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
              }
            }

            // HTML5 Official YouTube IFrame Player embed
            // NO API key is passed into the HTML or JavaScript
            val embedHtml = """
              <!DOCTYPE html>
              <html>
              <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
              <style>
                html, body {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  background-color: #000000;
                  overflow: hidden;
                }
                .container {
                  position: relative;
                  width: 100%;
                  height: 100%;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                }
                iframe {
                  width: 100%;
                  height: 100%;
                  border: 0;
                }
              </style>
              </head>
              <body>
                <div class="container">
                  <iframe 
                    id="yt-player" 
                    src="https://www.youtube.com/embed/$videoId?autoplay=1&enablejsapi=1&playsinline=1&rel=0&modestbranding=1&controls=1" 
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                    allowfullscreen>
                  </iframe>
                </div>
              </body>
              </html>
            """.trimIndent()

            loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
            webViewRef = this
          }
        },
        update = { webView ->
          webViewRef = webView
        },
        modifier = Modifier.fillMaxSize()
      )

      if (isLoading) {
        CircularProgressIndicator(
          color = MelodyXGreen,
          strokeWidth = 2.5.dp,
          modifier = Modifier.size(36.dp)
        )
      }
    }
  }
}
