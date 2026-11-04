package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.Album
import com.example.model.Artist
import com.example.model.DownloadStatus
import com.example.model.Playlist
import com.example.model.Track
import com.example.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit

class MusicRepository(private val context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("melodyx_prefs", Context.MODE_PRIVATE)

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  val youTubeService = com.example.data.youtube.YouTubeDataService()

  private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
  val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

  private val _trendingYouTubeTracks = MutableStateFlow<List<Track>>(emptyList())
  val trendingYouTubeTracks: StateFlow<List<Track>> = _trendingYouTubeTracks.asStateFlow()

  private val _recentReleaseTracks = MutableStateFlow<List<Track>>(emptyList())
  val recentReleaseTracks: StateFlow<List<Track>> = _recentReleaseTracks.asStateFlow()

  private val _youTubeApiErrorMessage = MutableStateFlow<String?>(null)
  val youTubeApiErrorMessage: StateFlow<String?> = _youTubeApiErrorMessage.asStateFlow()

  private val _isYouTubeQuotaExceeded = MutableStateFlow(false)
  val isYouTubeQuotaExceeded: StateFlow<Boolean> = _isYouTubeQuotaExceeded.asStateFlow()

  private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
  val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

  private val _favoriteTrackIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteTrackIds: StateFlow<Set<String>> = _favoriteTrackIds.asStateFlow()

  private val _historyTrackIds = MutableStateFlow<List<String>>(emptyList())
  val historyTrackIds: StateFlow<List<String>> = _historyTrackIds.asStateFlow()

  private val _artists = MutableStateFlow<List<Artist>>(emptyList())
  val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

  private val _albums = MutableStateFlow<List<Album>>(emptyList())
  val albums: StateFlow<List<Album>> = _albums.asStateFlow()

  private val _userProfile = MutableStateFlow(UserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _adminCustomTracks = MutableStateFlow<List<Track>>(emptyList())

  init {
    loadCuratedData()
    loadFavorites()
    loadPlaylists()
    loadHistory()
    loadAdminTracks()
    loadUserProfile()
  }

  // --- YouTube Data API v3 Music Search ---
  suspend fun searchYouTubeMusic(
    query: String,
    artist: String? = null,
    language: String? = null
  ): List<Track> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    when (val result = youTubeService.searchMusic(query = query, artist = artist, language = language)) {
      is com.example.data.youtube.YouTubeApiResult.Success -> {
        _isYouTubeQuotaExceeded.value = false
        _youTubeApiErrorMessage.value = null
        val ytTracks = result.data.map { track ->
          track.copy(isFavorite = _favoriteTrackIds.value.contains(track.id))
        }
        if (ytTracks.isNotEmpty()) {
          mergeIntoAllTracks(ytTracks)
          return@withContext ytTracks
        }
        // If empty on YouTube, fallback to iTunes
        searchRealMusic(query)
      }
      is com.example.data.youtube.YouTubeApiResult.Error -> {
        Log.w("MusicRepository", "YouTube search failed: ${result.message}, falling back to iTunes")
        _youTubeApiErrorMessage.value = result.message
        _isYouTubeQuotaExceeded.value = result.isQuotaExceeded
        searchRealMusic(query)
      }
    }
  }

  // --- YouTube Trending & Recently Released ---
  suspend fun fetchYouTubeTrending(regionCode: String = "IN") = withContext(Dispatchers.IO) {
    when (val result = youTubeService.getTrendingMusic(regionCode = regionCode)) {
      is com.example.data.youtube.YouTubeApiResult.Success -> {
        val tracks = result.data.map {
          it.copy(isFavorite = _favoriteTrackIds.value.contains(it.id))
        }
        _trendingYouTubeTracks.value = tracks
        mergeIntoAllTracks(tracks)
      }
      is com.example.data.youtube.YouTubeApiResult.Error -> {
        _youTubeApiErrorMessage.value = result.message
        _isYouTubeQuotaExceeded.value = result.isQuotaExceeded
      }
    }
  }

  suspend fun fetchYouTubeRecentReleases(language: String? = null) = withContext(Dispatchers.IO) {
    when (val result = youTubeService.getRecentlyReleasedMusic(language = language)) {
      is com.example.data.youtube.YouTubeApiResult.Success -> {
        val tracks = result.data.map {
          it.copy(isFavorite = _favoriteTrackIds.value.contains(it.id))
        }
        _recentReleaseTracks.value = tracks
        mergeIntoAllTracks(tracks)
      }
      is com.example.data.youtube.YouTubeApiResult.Error -> {
        _youTubeApiErrorMessage.value = result.message
        _isYouTubeQuotaExceeded.value = result.isQuotaExceeded
      }
    }
  }

  private fun mergeIntoAllTracks(newTracks: List<Track>) {
    val currentMap = _allTracks.value.associateBy { it.id }.toMutableMap()
    newTracks.forEach { currentMap[it.id] = it }
    _allTracks.value = currentMap.values.toList()
  }

  // --- Real Music API: iTunes Search API ---
  suspend fun searchRealMusic(query: String, filterGenre: String? = null): List<Track> =
    withContext(Dispatchers.IO) {
      if (query.isBlank()) return@withContext emptyList()
      try {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "https://itunes.apple.com/search?term=$encoded&entity=song&limit=35"
        val request = Request.Builder()
          .url(url)
          .addHeader("User-Agent", "SunoJara-Music/1.0")
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) return@withContext emptyList()
          val bodyStr = response.body?.string() ?: return@withContext emptyList()
          val json = JSONObject(bodyStr)
          val results = json.optJSONArray("results") ?: return@withContext emptyList()
          val list = mutableListOf<Track>()

          for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val trackId = "itunes_${item.optLong("trackId")}"
            val trackName = item.optString("trackName", "Unknown Song")
            val artistName = item.optString("artistName", "Unknown Artist")
            val collectionName = item.optString("collectionName", "Single")
            val rawArtwork = item.optString("artworkUrl100", "")
            // Scale to 600x600 for crisp visual presentation
            val hiResArtwork = if (rawArtwork.isNotEmpty()) {
              rawArtwork.replace("100x100bb", "600x600bb")
            } else {
              "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"
            }
            val previewUrl = item.optString("previewUrl", "")
            if (previewUrl.isEmpty()) continue

            val durationSeconds = (item.optLong("trackTimeMillis", 180000L) / 1000).toInt()
            val primaryGenre = item.optString("primaryGenreName", "Pop")
            val releaseDate = item.optString("releaseDate", "")
            val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else "2024"

            val track = Track(
              id = trackId,
              title = trackName,
              artist = artistName,
              album = collectionName,
              artworkUrl = hiResArtwork,
              audioUrl = previewUrl,
              durationSeconds = durationSeconds,
              genre = primaryGenre,
              language = inferLanguage(trackName, artistName, primaryGenre),
              releaseYear = year,
              isAuthorizedDownload = true,
              isFavorite = _favoriteTrackIds.value.contains(trackId)
            )
            list.add(track)
          }

          if (list.isNotEmpty()) {
            // Merge newly found tracks into _allTracks so the player can access metadata
            val currentMap = _allTracks.value.associateBy { it.id }.toMutableMap()
            list.forEach { currentMap[it.id] = it }
            _allTracks.value = currentMap.values.toList()
          }
          list
        }
      } catch (e: Exception) {
        Log.e("MusicRepository", "iTunes search error: ${e.message}")
        // Fallback to searching local catalog
        _allTracks.value.filter {
          it.title.contains(query, ignoreCase = true) ||
              it.artist.contains(query, ignoreCase = true) ||
              it.album.contains(query, ignoreCase = true) ||
              it.genre.contains(query, ignoreCase = true) ||
              it.language.contains(query, ignoreCase = true)
        }
      }
    }

  private fun inferLanguage(title: String, artist: String, genre: String): String {
    val lower = "$title $artist $genre".lowercase()
    return when {
      lower.contains("hindi") || lower.contains("arijit") || lower.contains("badshah") || lower.contains("bollywood") -> "Hindi"
      lower.contains("punjabi") || lower.contains("diljit") || lower.contains("dhillon") || lower.contains("moose") -> "Punjabi"
      lower.contains("tamil") || lower.contains("anirudh") || lower.contains("rahman") -> "Tamil"
      lower.contains("telugu") || lower.contains("dsp") || lower.contains("thaman") -> "Telugu"
      lower.contains("malayalam") || lower.contains("sushin") -> "Malayalam"
      lower.contains("bengali") || lower.contains("anupam") -> "Bengali"
      lower.contains("bhojpuri") || lower.contains("pawan") || lower.contains("khesari") -> "Bhojpuri"
      lower.contains("latino") || lower.contains("k-pop") || lower.contains("french") || lower.contains("afro") -> "International"
      else -> "English"
    }
  }

  // --- Favorite / Liked Tracks Management ---
  fun toggleFavorite(trackId: String): Boolean {
    val current = _favoriteTrackIds.value.toMutableSet()
    val isFav = if (current.contains(trackId)) {
      current.remove(trackId)
      false
    } else {
      current.add(trackId)
      true
    }
    _favoriteTrackIds.value = current
    saveFavorites(current)

    // Update isFavorite on tracks
    _allTracks.value = _allTracks.value.map {
      if (it.id == trackId) it.copy(isFavorite = isFav) else it
    }
    return isFav
  }

  fun isFavorite(trackId: String): Boolean {
    return _favoriteTrackIds.value.contains(trackId)
  }

  fun getFavoriteTracks(): List<Track> {
    val favIds = _favoriteTrackIds.value
    return _allTracks.value.filter { favIds.contains(it.id) }
  }

  // --- Listening History ---
  fun recordHistory(track: Track) {
    val current = _historyTrackIds.value.toMutableList()
    current.remove(track.id)
    current.add(0, track.id)
    if (current.size > 50) current.removeAt(current.lastIndex)
    _historyTrackIds.value = current
    saveHistory(current)

    // Increment play count
    _allTracks.value = _allTracks.value.map {
      if (it.id == track.id) it.copy(playCount = it.playCount + 1) else it
    }
  }

  fun getHistoryTracks(): List<Track> {
    val map = _allTracks.value.associateBy { it.id }
    return _historyTrackIds.value.mapNotNull { map[it] }
  }

  fun clearHistory() {
    _historyTrackIds.value = emptyList()
    prefs.edit().remove("history_tracks").apply()
  }

  // --- Playlist Management ---
  fun createPlaylist(name: String, description: String = ""): Playlist {
    val newPl = Playlist(
      id = "pl_${UUID.randomUUID()}",
      name = name.trim().ifEmpty { "My Playlist #${_playlists.value.size + 1}" },
      description = description,
      coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
      trackIds = emptyList(),
      isCustom = true
    )
    val updated = _playlists.value + newPl
    _playlists.value = updated
    savePlaylists(updated)
    return newPl
  }

  fun addTrackToPlaylist(playlistId: String, trackId: String) {
    val updated = _playlists.value.map { pl ->
      if (pl.id == playlistId && !pl.trackIds.contains(trackId)) {
        pl.copy(trackIds = pl.trackIds + trackId)
      } else pl
    }
    _playlists.value = updated
    savePlaylists(updated)
  }

  fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
    val updated = _playlists.value.map { pl ->
      if (pl.id == playlistId) {
        pl.copy(trackIds = pl.trackIds - trackId)
      } else pl
    }
    _playlists.value = updated
    savePlaylists(updated)
  }

  fun deletePlaylist(playlistId: String) {
    val updated = _playlists.value.filter { it.id != playlistId }
    _playlists.value = updated
    savePlaylists(updated)
  }

  fun editPlaylist(playlistId: String, newName: String, newDescription: String) {
    val updated = _playlists.value.map { pl ->
      if (pl.id == playlistId) {
        pl.copy(name = newName, description = newDescription)
      } else pl
    }
    _playlists.value = updated
    savePlaylists(updated)
  }

  // --- Admin Catalog Management ---
  fun addCustomTrack(track: Track) {
    val current = _adminCustomTracks.value + track
    _adminCustomTracks.value = current
    saveAdminTracks(current)
    _allTracks.value = listOf(track) + _allTracks.value
  }

  fun updateTrackMetadata(updatedTrack: Track) {
    _allTracks.value = _allTracks.value.map {
      if (it.id == updatedTrack.id) updatedTrack else it
    }
  }

  fun updateUserProfile(name: String, email: String, quality: String, offlineOnly: Boolean) {
    val updated = _userProfile.value.copy(
      name = name,
      email = email,
      streamQuality = quality,
      offlineOnly = offlineOnly
    )
    _userProfile.value = updated
    prefs.edit()
      .putString("user_name", name)
      .putString("user_email", email)
      .putString("user_quality", quality)
      .putBoolean("user_offline", offlineOnly)
      .apply()
  }

  // --- Persistence Helpers ---
  private fun saveFavorites(favs: Set<String>) {
    prefs.edit().putStringSet("favorites", favs).apply()
  }

  private fun loadFavorites() {
    val favs = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    _favoriteTrackIds.value = favs
  }

  private fun saveHistory(history: List<String>) {
    val array = JSONArray(history)
    prefs.edit().putString("history_tracks", array.toString()).apply()
  }

  private fun loadHistory() {
    val str = prefs.getString("history_tracks", null) ?: return
    try {
      val array = JSONArray(str)
      val list = mutableListOf<String>()
      for (i in 0 until array.length()) {
        list.add(array.getString(i))
      }
      _historyTrackIds.value = list
    } catch (e: Exception) {
      Log.e("MusicRepository", "Failed to load history", e)
    }
  }

  private fun savePlaylists(playlists: List<Playlist>) {
    val array = JSONArray()
    playlists.forEach { pl ->
      val obj = JSONObject().apply {
        put("id", pl.id)
        put("name", pl.name)
        put("description", pl.description)
        put("coverUrl", pl.coverUrl)
        put("isCustom", pl.isCustom)
        put("trackIds", JSONArray(pl.trackIds))
      }
      array.put(obj)
    }
    prefs.edit().putString("custom_playlists", array.toString()).apply()
  }

  private fun loadPlaylists() {
    val str = prefs.getString("custom_playlists", null)
    if (str != null) {
      try {
        val array = JSONArray(str)
        val list = mutableListOf<Playlist>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val tArray = obj.optJSONArray("trackIds") ?: JSONArray()
          val tIds = mutableListOf<String>()
          for (j in 0 until tArray.length()) {
            tIds.add(tArray.getString(j))
          }
          list.add(
            Playlist(
              id = obj.getString("id"),
              name = obj.getString("name"),
              description = obj.optString("description", ""),
              coverUrl = obj.optString("coverUrl", ""),
              trackIds = tIds,
              isCustom = obj.optBoolean("isCustom", false)
            )
          )
        }
        if (list.isNotEmpty()) {
          _playlists.value = list
          return
        }
      } catch (e: Exception) {
        Log.e("MusicRepository", "Failed to parse playlists", e)
      }
    }
    // Default initial playlists
    _playlists.value = listOf(
      Playlist(
        id = "pl_top_hits",
        name = "Today's Top Hits",
        description = "The biggest songs in India and worldwide right now.",
        coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
        trackIds = listOf("m1", "m2", "m3", "m4", "m5", "m6")
      ),
      Playlist(
        id = "pl_desi_vibes",
        name = "Desi Pop & Beats",
        description = "Top trending Hindi and Punjabi chartbusters.",
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
        trackIds = listOf("m1", "m3", "m5", "m7", "m9")
      ),
      Playlist(
        id = "pl_south_wave",
        name = "South Indian Wave",
        description = "Tamil, Telugu and Malayalam cinematic bangers.",
        coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600",
        trackIds = listOf("m4", "m8", "m10")
      ),
      Playlist(
        id = "pl_chill_acoustic",
        name = "Late Night Chill",
        description = "Relaxed beats and acoustic melodies for unwinding.",
        coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
        trackIds = listOf("m2", "m6", "m11", "m12")
      )
    )
  }

  private fun saveAdminTracks(tracks: List<Track>) {
    val array = JSONArray()
    tracks.forEach { t ->
      val obj = JSONObject().apply {
        put("id", t.id)
        put("title", t.title)
        put("artist", t.artist)
        put("album", t.album)
        put("artworkUrl", t.artworkUrl)
        put("audioUrl", t.audioUrl)
        put("durationSeconds", t.durationSeconds)
        put("genre", t.genre)
        put("language", t.language)
        put("releaseYear", t.releaseYear)
        put("isAuthorizedDownload", t.isAuthorizedDownload)
      }
      array.put(obj)
    }
    prefs.edit().putString("admin_custom_tracks", array.toString()).apply()
  }

  private fun loadAdminTracks() {
    val str = prefs.getString("admin_custom_tracks", null) ?: return
    try {
      val array = JSONArray(str)
      val list = mutableListOf<Track>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          Track(
            id = obj.getString("id"),
            title = obj.getString("title"),
            artist = obj.getString("artist"),
            album = obj.getString("album"),
            artworkUrl = obj.getString("artworkUrl"),
            audioUrl = obj.getString("audioUrl"),
            durationSeconds = obj.optInt("durationSeconds", 180),
            genre = obj.optString("genre", "Pop"),
            language = obj.optString("language", "English"),
            releaseYear = obj.optString("releaseYear", "2024"),
            isAuthorizedDownload = obj.optBoolean("isAuthorizedDownload", true)
          )
        )
      }
      _adminCustomTracks.value = list
      if (list.isNotEmpty()) {
        _allTracks.value = list + _allTracks.value
      }
    } catch (e: Exception) {
      Log.e("MusicRepository", "Failed to load admin tracks", e)
    }
  }

  private fun loadUserProfile() {
    val name = prefs.getString("user_name", "Alex Vance") ?: "Alex Vance"
    val email = prefs.getString("user_email", "akashdo024@gmail.com") ?: "akashdo024@gmail.com"
    val quality = prefs.getString("user_quality", "Very High (320 kbps)") ?: "Very High (320 kbps)"
    val offline = prefs.getBoolean("user_offline", false)
    _userProfile.value = UserProfile(
      name = name,
      email = email,
      streamQuality = quality,
      offlineOnly = offline
    )
  }

  private fun loadCuratedData() {
    // Rich, diverse catalog spanning Hindi, English, Punjabi, Tamil, Telugu, Malayalam, Bengali, Bhojpuri, International
    // Uses legitimate open streaming / Creative Commons / iTunes CDN preview URLs with real audio
    val curated = listOf(
      Track(
        id = "m1",
        title = "Chaleya (Jawan)",
        artist = "Arijit Singh & Shilpa Rao",
        album = "Jawan Soundtrack",
        artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/44/21/df/4421df0c-c603-c287-c376-782cfecb1da7/mzaf_11306306566417757917.plus.aac.p.m4a",
        durationSeconds = 200,
        genre = "Bollywood Pop",
        language = "Hindi",
        releaseYear = "2023",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m2",
        title = "Blinding Lights",
        artist = "The Weeknd",
        album = "After Hours",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/58/59/3a/58593a1d-a3df-a8ad-1430-84a27e77b639/mzaf_1079366635292671382.plus.aac.p.m4a",
        durationSeconds = 200,
        genre = "Synthwave Pop",
        language = "English",
        releaseYear = "2020",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m3",
        title = "Lover",
        artist = "Diljit Dosanjh",
        album = "MoonChild Era",
        artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/ae/2e/d5/ae2ed5ea-128a-7e0e-9d2a-4df458cbfdc9/mzaf_5241088665971431690.plus.aac.p.m4a",
        durationSeconds = 186,
        genre = "Punjabi Pop",
        language = "Punjabi",
        releaseYear = "2021",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m4",
        title = "Hukum - Thalaivar Alappara",
        artist = "Anirudh Ravichander",
        album = "Jailer Soundtrack",
        artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/91/92/ff/9192ffca-4d55-6b5d-2b7e-9ae9d4f04c7c/mzaf_3196947230485609347.plus.aac.p.m4a",
        durationSeconds = 208,
        genre = "Tamil Beat",
        language = "Tamil",
        releaseYear = "2023",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m5",
        title = "Kesariya",
        artist = "Arijit Singh",
        album = "Brahmāstra",
        artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/44/21/df/4421df0c-c603-c287-c376-782cfecb1da7/mzaf_11306306566417757917.plus.aac.p.m4a",
        durationSeconds = 268,
        genre = "Romantic",
        language = "Hindi",
        releaseYear = "2022",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m6",
        title = "Cruel Summer",
        artist = "Taylor Swift",
        album = "Lover",
        artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/3d/9d/54/3d9d547f-8ec0-2178-360d-400e28f32194/mzaf_10427976839396324222.plus.aac.p.m4a",
        durationSeconds = 178,
        genre = "Electropop",
        language = "English",
        releaseYear = "2019",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m7",
        title = "Brown Munde",
        artist = "AP Dhillon, Gurinder Gill, Shinda Kahlon",
        album = "Brown Munde",
        artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/ae/2e/d5/ae2ed5ea-128a-7e0e-9d2a-4df458cbfdc9/mzaf_5241088665971431690.plus.aac.p.m4a",
        durationSeconds = 267,
        genre = "Punjabi Hip-Hop",
        language = "Punjabi",
        releaseYear = "2020",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m8",
        title = "Samajavaragamana",
        artist = "Sid Sriram & Thaman S",
        album = "Ala Vaikunthapurramuloo",
        artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/f6/cb/3f/f6cb3f9b-6320-a616-5e58-947b19a1db69/mzaf_18205260171060938487.plus.aac.p.m4a",
        durationSeconds = 224,
        genre = "Telugu Melody",
        language = "Telugu",
        releaseYear = "2020",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m9",
        title = "Apna Bana Le",
        artist = "Arijit Singh & Sachin-Jigar",
        album = "Bhediya",
        artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/44/21/df/4421df0c-c603-c287-c376-782cfecb1da7/mzaf_11306306566417757917.plus.aac.p.m4a",
        durationSeconds = 261,
        genre = "Soulful",
        language = "Hindi",
        releaseYear = "2022",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m10",
        title = "Manavalan Thug (Thallumaala)",
        artist = "Dabzee, SA & Sushin Shyam",
        album = "Thallumaala",
        artworkUrl = "https://images.unsplash.com/photo-1469488865564-c2de10f69f96?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/91/92/ff/9192ffca-4d55-6b5d-2b7e-9ae9d4f04c7c/mzaf_3196947230485609347.plus.aac.p.m4a",
        durationSeconds = 212,
        genre = "Malayalam Beats",
        language = "Malayalam",
        releaseYear = "2022",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m11",
        title = "Tumi Jaake Bhalobasho",
        artist = "Iman Chakraborty & Anupam Roy",
        album = "Praktan",
        artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/44/21/df/4421df0c-c603-c287-c376-782cfecb1da7/mzaf_11306306566417757917.plus.aac.p.m4a",
        durationSeconds = 270,
        genre = "Bengali Classic",
        language = "Bengali",
        releaseYear = "2016",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m12",
        title = "Lollipop Lagelu",
        artist = "Pawan Singh",
        album = "Bhojpuri Superhits",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/ae/2e/d5/ae2ed5ea-128a-7e0e-9d2a-4df458cbfdc9/mzaf_5241088665971431690.plus.aac.p.m4a",
        durationSeconds = 245,
        genre = "Bhojpuri Dance",
        language = "Bhojpuri",
        releaseYear = "2018",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m13",
        title = "Levitating",
        artist = "Dua Lipa",
        album = "Future Nostalgia",
        artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/58/59/3a/58593a1d-a3df-a8ad-1430-84a27e77b639/mzaf_1079366635292671382.plus.aac.p.m4a",
        durationSeconds = 203,
        genre = "Nu-Disco",
        language = "International",
        releaseYear = "2020",
        isAuthorizedDownload = true
      ),
      Track(
        id = "m14",
        title = "Bad Boy",
        artist = "Badshah & Nikhita Gandhi",
        album = "Saaho Soundtrack",
        artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600",
        audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/ae/2e/d5/ae2ed5ea-128a-7e0e-9d2a-4df458cbfdc9/mzaf_5241088665971431690.plus.aac.p.m4a",
        durationSeconds = 178,
        genre = "Club Hip Hop",
        language = "Hindi",
        releaseYear = "2019",
        isAuthorizedDownload = true
      )
    )

    _allTracks.value = curated

    _artists.value = listOf(
      Artist(
        id = "art_arijit",
        name = "Arijit Singh",
        imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
        monthlyListeners = "42.8M",
        genres = listOf("Bollywood", "Romantic", "Sufi")
      ),
      Artist(
        id = "art_taylor",
        name = "Taylor Swift",
        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        monthlyListeners = "105.2M",
        genres = listOf("Pop", "Folk", "Country")
      ),
      Artist(
        id = "art_diljit",
        name = "Diljit Dosanjh",
        imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
        monthlyListeners = "28.4M",
        genres = listOf("Punjabi", "Pop", "Bhangra")
      ),
      Artist(
        id = "art_weeknd",
        name = "The Weeknd",
        imageUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400",
        monthlyListeners = "96.5M",
        genres = listOf("R&B", "Synthwave", "Pop")
      ),
      Artist(
        id = "art_anirudh",
        name = "Anirudh Ravichander",
        imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400",
        monthlyListeners = "22.1M",
        genres = listOf("Tamil", "EDM", "Film Score")
      ),
      Artist(
        id = "art_sid",
        name = "Sid Sriram",
        imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
        monthlyListeners = "18.3M",
        genres = listOf("Carnatic", "Telugu", "Tamil")
      )
    )

    _albums.value = listOf(
      Album(
        id = "alb_jawan",
        title = "Jawan (Original Motion Picture Soundtrack)",
        artist = "Anirudh Ravichander",
        coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
        releaseYear = "2023",
        trackIds = listOf("m1", "m4")
      ),
      Album(
        id = "alb_after_hours",
        title = "After Hours",
        artist = "The Weeknd",
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
        releaseYear = "2020",
        trackIds = listOf("m2")
      ),
      Album(
        id = "alb_moonchild",
        title = "MoonChild Era",
        artist = "Diljit Dosanjh",
        coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
        releaseYear = "2021",
        trackIds = listOf("m3", "m7")
      ),
      Album(
        id = "alb_lover",
        title = "Lover",
        artist = "Taylor Swift",
        coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
        releaseYear = "2019",
        trackIds = listOf("m6")
      )
    )
  }
}
