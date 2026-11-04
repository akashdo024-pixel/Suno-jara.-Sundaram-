package com.example.data.youtube

import android.text.Html
import android.util.Log
import com.example.BuildConfig
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class YouTubeApiResult<out T> {
  data class Success<out T>(val data: T) : YouTubeApiResult<T>()
  data class Error(val message: String, val isQuotaExceeded: Boolean = false) : YouTubeApiResult<Nothing>()
}

class YouTubeDataService(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(18, TimeUnit.SECONDS)
    .build()
) {
  private val apiKey: String
    get() = BuildConfig.YOUTUBE_API_KEY

  /**
   * Searches YouTube Data API v3 search.list for music videos matching query, artist, and language.
   */
  suspend fun searchMusic(
    query: String,
    artist: String? = null,
    language: String? = null,
    maxResults: Int = 20
  ): YouTubeApiResult<List<Track>> = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
      return@withContext YouTubeApiResult.Error("YouTube API key is missing. Configure it in AI Studio Secrets panel.")
    }

    try {
      val combinedQueryBuilder = StringBuilder(query.trim())
      if (!artist.isNullOrBlank() && !query.contains(artist, ignoreCase = true)) {
        combinedQueryBuilder.append(" ").append(artist.trim())
      }
      if (!language.isNullOrBlank() && !language.equals("All", ignoreCase = true) && !query.contains(language, ignoreCase = true)) {
        combinedQueryBuilder.append(" ").append(language.trim())
      }
      combinedQueryBuilder.append(" song")

      val encodedQuery = URLEncoder.encode(combinedQueryBuilder.toString(), "UTF-8")
      val url = "https://www.googleapis.com/youtube/v3/search" +
          "?part=snippet" +
          "&type=video" +
          "&videoCategoryId=10" +
          "&maxResults=$maxResults" +
          "&q=$encodedQuery" +
          "&key=$apiKey"

      val request = Request.Builder()
        .url(url)
        .addHeader("User-Agent", "SunoJara-Music/1.0")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful) {
          return@withContext parseErrorResponse(response.code, bodyStr)
        }

        val json = JSONObject(bodyStr)
        val items = json.optJSONArray("items") ?: return@withContext YouTubeApiResult.Success(emptyList())
        val tracks = mutableListOf<Track>()

        for (i in 0 until items.length()) {
          val item = items.getJSONObject(i)
          val idObj = item.optJSONObject("id")
          val videoId = idObj?.optString("videoId") ?: continue
          val snippet = item.optJSONObject("snippet") ?: continue

          val rawTitle = snippet.optString("title", "YouTube Track")
          val cleanTitle = unescapeHtml(rawTitle)
          val channelTitle = unescapeHtml(snippet.optString("channelTitle", "Official Artist Channel"))
          val publishedAt = snippet.optString("publishedAt", "")
          val year = if (publishedAt.length >= 4) publishedAt.substring(0, 4) else "2024"

          val thumbnails = snippet.optJSONObject("thumbnails")
          val highThumb = thumbnails?.optJSONObject("high")?.optString("url")
          val medThumb = thumbnails?.optJSONObject("medium")?.optString("url")
          val defThumb = thumbnails?.optJSONObject("default")?.optString("url")
          val artworkUrl = highThumb ?: medThumb ?: defThumb ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"

          // Detect language and artist separation from title if possible
          val (artistName, songTitle) = parseArtistAndTitle(cleanTitle, channelTitle)

          val track = Track(
            id = "yt_$videoId",
            title = songTitle,
            artist = artistName,
            album = channelTitle,
            artworkUrl = artworkUrl,
            audioUrl = "https://www.youtube.com/watch?v=$videoId",
            durationSeconds = 210, // YouTube search snippet doesn't include contentDetails duration
            genre = "YouTube Music",
            language = language?.takeIf { it != "All" } ?: inferLanguage(cleanTitle, channelTitle),
            releaseYear = year,
            isAuthorizedDownload = false, // YouTube terms prohibit direct audio downloading
            youtubeVideoId = videoId,
            channelTitle = channelTitle,
            isYouTubeSource = true
          )
          tracks.add(track)
        }

        YouTubeApiResult.Success(tracks)
      }
    } catch (e: Exception) {
      Log.e("YouTubeDataService", "Search failed", e)
      YouTubeApiResult.Error(e.localizedMessage ?: "Network error connecting to YouTube")
    }
  }

  /**
   * Fetches trending music videos using YouTube videos.list chart=mostPopular & videoCategoryId=10
   */
  suspend fun getTrendingMusic(
    regionCode: String = "IN",
    maxResults: Int = 20
  ): YouTubeApiResult<List<Track>> = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
      return@withContext YouTubeApiResult.Error("YouTube API key is missing.")
    }

    try {
      val url = "https://www.googleapis.com/youtube/v3/videos" +
          "?part=snippet" +
          "&chart=mostPopular" +
          "&videoCategoryId=10" +
          "&regionCode=$regionCode" +
          "&maxResults=$maxResults" +
          "&key=$apiKey"

      val request = Request.Builder()
        .url(url)
        .addHeader("User-Agent", "SunoJara-Music/1.0")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful) {
          return@withContext parseErrorResponse(response.code, bodyStr)
        }

        val json = JSONObject(bodyStr)
        val items = json.optJSONArray("items") ?: return@withContext YouTubeApiResult.Success(emptyList())
        val tracks = mutableListOf<Track>()

        for (i in 0 until items.length()) {
          val item = items.getJSONObject(i)
          val videoId = item.optString("id")
          if (videoId.isBlank()) continue
          val snippet = item.optJSONObject("snippet") ?: continue

          val rawTitle = snippet.optString("title", "Trending Music")
          val cleanTitle = unescapeHtml(rawTitle)
          val channelTitle = unescapeHtml(snippet.optString("channelTitle", "Artist"))
          val publishedAt = snippet.optString("publishedAt", "")
          val year = if (publishedAt.length >= 4) publishedAt.substring(0, 4) else "2024"

          val thumbnails = snippet.optJSONObject("thumbnails")
          val highThumb = thumbnails?.optJSONObject("high")?.optString("url")
          val standardThumb = thumbnails?.optJSONObject("standard")?.optString("url")
          val medThumb = thumbnails?.optJSONObject("medium")?.optString("url")
          val artworkUrl = standardThumb ?: highThumb ?: medThumb ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600"

          val (artistName, songTitle) = parseArtistAndTitle(cleanTitle, channelTitle)

          val track = Track(
            id = "yt_$videoId",
            title = songTitle,
            artist = artistName,
            album = channelTitle,
            artworkUrl = artworkUrl,
            audioUrl = "https://www.youtube.com/watch?v=$videoId",
            durationSeconds = 220,
            genre = "Trending Hits",
            language = inferLanguage(cleanTitle, channelTitle),
            releaseYear = year,
            isAuthorizedDownload = false,
            youtubeVideoId = videoId,
            channelTitle = channelTitle,
            isYouTubeSource = true
          )
          tracks.add(track)
        }

        YouTubeApiResult.Success(tracks)
      }
    } catch (e: Exception) {
      Log.e("YouTubeDataService", "Trending fetch failed", e)
      YouTubeApiResult.Error(e.localizedMessage ?: "Failed to fetch YouTube trending songs")
    }
  }

  /**
   * Fetches recently released music videos using search.list ordered by date
   */
  suspend fun getRecentlyReleasedMusic(
    language: String? = null,
    maxResults: Int = 20
  ): YouTubeApiResult<List<Track>> = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
      return@withContext YouTubeApiResult.Error("YouTube API key is missing.")
    }

    try {
      val query = if (!language.isNullOrBlank() && language != "All") {
        "$language official music video latest"
      } else {
        "official music video latest release"
      }
      val encodedQuery = URLEncoder.encode(query, "UTF-8")

      val url = "https://www.googleapis.com/youtube/v3/search" +
          "?part=snippet" +
          "&type=video" +
          "&videoCategoryId=10" +
          "&order=date" +
          "&maxResults=$maxResults" +
          "&q=$encodedQuery" +
          "&key=$apiKey"

      val request = Request.Builder()
        .url(url)
        .addHeader("User-Agent", "SunoJara-Music/1.0")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful) {
          return@withContext parseErrorResponse(response.code, bodyStr)
        }

        val json = JSONObject(bodyStr)
        val items = json.optJSONArray("items") ?: return@withContext YouTubeApiResult.Success(emptyList())
        val tracks = mutableListOf<Track>()

        for (i in 0 until items.length()) {
          val item = items.getJSONObject(i)
          val idObj = item.optJSONObject("id")
          val videoId = idObj?.optString("videoId") ?: continue
          val snippet = item.optJSONObject("snippet") ?: continue

          val cleanTitle = unescapeHtml(snippet.optString("title", "New Release"))
          val channelTitle = unescapeHtml(snippet.optString("channelTitle", "Channel"))
          val publishedAt = snippet.optString("publishedAt", "")
          val year = if (publishedAt.length >= 4) publishedAt.substring(0, 4) else "2024"

          val thumbnails = snippet.optJSONObject("thumbnails")
          val highThumb = thumbnails?.optJSONObject("high")?.optString("url")
          val medThumb = thumbnails?.optJSONObject("medium")?.optString("url")
          val artworkUrl = highThumb ?: medThumb ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600"

          val (artistName, songTitle) = parseArtistAndTitle(cleanTitle, channelTitle)

          val track = Track(
            id = "yt_$videoId",
            title = songTitle,
            artist = artistName,
            album = channelTitle,
            artworkUrl = artworkUrl,
            audioUrl = "https://www.youtube.com/watch?v=$videoId",
            durationSeconds = 200,
            genre = "New Release",
            language = language?.takeIf { it != "All" } ?: inferLanguage(cleanTitle, channelTitle),
            releaseYear = year,
            isAuthorizedDownload = false,
            youtubeVideoId = videoId,
            channelTitle = channelTitle,
            isYouTubeSource = true
          )
          tracks.add(track)
        }

        YouTubeApiResult.Success(tracks)
      }
    } catch (e: Exception) {
      Log.e("YouTubeDataService", "Recent releases fetch failed", e)
      YouTubeApiResult.Error(e.localizedMessage ?: "Failed to fetch new releases")
    }
  }

  private fun parseErrorResponse(code: Int, bodyStr: String): YouTubeApiResult.Error {
    return try {
      val json = JSONObject(bodyStr)
      val errorObj = json.optJSONObject("error")
      val message = errorObj?.optString("message") ?: "YouTube API Error HTTP $code"
      val errorsArray = errorObj?.optJSONArray("errors")
      var isQuota = false
      if (errorsArray != null) {
        for (i in 0 until errorsArray.length()) {
          val err = errorsArray.getJSONObject(i)
          val reason = err.optString("reason")
          if (reason.contains("quota", ignoreCase = true) || reason.contains("limit", ignoreCase = true)) {
            isQuota = true
            break
          }
        }
      }
      if (code == 403 || message.contains("quota", ignoreCase = true)) {
        isQuota = true
      }
      val friendlyMessage = if (isQuota) {
        "YouTube API daily quota limit reached. Showing cached & curated music."
      } else {
        message
      }
      YouTubeApiResult.Error(friendlyMessage, isQuotaExceeded = isQuota)
    } catch (e: Exception) {
      YouTubeApiResult.Error("YouTube API Error (HTTP $code)", isQuotaExceeded = code == 403)
    }
  }

  private fun unescapeHtml(text: String): String {
    return try {
      Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    } catch (e: Exception) {
      text.replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
    }
  }

  private fun parseArtistAndTitle(fullTitle: String, fallbackChannel: String): Pair<String, String> {
    // Many YouTube titles come in format: "Artist - Song Title (Official Music Video)"
    val clean = fullTitle
      .replace(Regex("(?i)\\b(official\\s*(music\\s*)?video|official\\s*audio|lyric\\s*video|full\\s*song|4k|hd|remix)\\b"), "")
      .replace("()", "")
      .replace("[]", "")
      .trim()

    val parts = clean.split(" - ", " – ", " | ", ": ")
    return if (parts.size >= 2) {
      val artist = parts[0].trim().ifBlank { fallbackChannel }
      val title = parts.subList(1, parts.size).joinToString(" - ").trim()
      Pair(artist, title.ifBlank { fullTitle })
    } else {
      Pair(fallbackChannel, clean.ifBlank { fullTitle })
    }
  }

  private fun inferLanguage(title: String, channel: String): String {
    val lower = "$title $channel".lowercase()
    return when {
      lower.contains("hindi") || lower.contains("arijit") || lower.contains("bollywood") || lower.contains("t-series") || lower.contains("zeemusic") -> "Hindi"
      lower.contains("punjabi") || lower.contains("diljit") || lower.contains("speed records") -> "Punjabi"
      lower.contains("tamil") || lower.contains("anirudh") || lower.contains("sony music south") -> "Tamil"
      lower.contains("telugu") || lower.contains("aditya music") -> "Telugu"
      lower.contains("malayalam") || lower.contains("sushin") -> "Malayalam"
      lower.contains("bengali") || lower.contains("svf") -> "Bengali"
      lower.contains("bhojpuri") || lower.contains("wave music") -> "Bhojpuri"
      lower.contains("k-pop") || lower.contains("latin") || lower.contains("vevo") -> "International"
      else -> "English"
    }
  }
}
