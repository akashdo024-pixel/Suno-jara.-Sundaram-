package com.example.model

enum class DownloadStatus {
  NOT_DOWNLOADED,
  DOWNLOADING,
  DOWNLOADED,
  ERROR
}

enum class RepeatMode {
  OFF,
  ALL,
  ONE
}

data class Track(
  val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val artworkUrl: String,
  val audioUrl: String,
  val durationSeconds: Int = 180,
  val genre: String = "Pop",
  val language: String = "English",
  val releaseYear: String = "2024",
  val isAuthorizedDownload: Boolean = true,
  val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
  val downloadProgress: Float = 0f,
  val localFilePath: String? = null,
  val fileSizeBytes: Long = 0L,
  val isFavorite: Boolean = false,
  val playCount: Int = 0,
  val youtubeVideoId: String? = null,
  val channelTitle: String? = null,
  val isYouTubeSource: Boolean = false
) {
  val formattedDuration: String
    get() {
      val minutes = durationSeconds / 60
      val seconds = durationSeconds % 60
      return "%d:%02d".format(minutes, seconds)
    }

  val formattedFileSize: String
    get() {
      if (fileSizeBytes <= 0) return "4.2 MB"
      val mb = fileSizeBytes / (1024.0 * 1024.0)
      return "%.1f MB".format(mb)
    }
}

data class Playlist(
  val id: String,
  val name: String,
  val description: String,
  val coverUrl: String,
  val trackIds: List<String> = emptyList(),
  val isCustom: Boolean = false
)

data class Artist(
  val id: String,
  val name: String,
  val imageUrl: String,
  val monthlyListeners: String,
  val genres: List<String>
)

data class Album(
  val id: String,
  val title: String,
  val artist: String,
  val coverUrl: String,
  val releaseYear: String,
  val trackIds: List<String>
)

data class UserProfile(
  val name: String = "Alex Vance",
  val email: String = "akashdo024@gmail.com",
  val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
  val membershipTier: String = "Suno Jara Hi-Fi Premium",
  val streamQuality: String = "Very High (320 kbps)",
  val totalMinutesListened: Int = 1428,
  val topGenre: String = "Hindi & Punjabi Pop",
  val offlineOnly: Boolean = false
)
