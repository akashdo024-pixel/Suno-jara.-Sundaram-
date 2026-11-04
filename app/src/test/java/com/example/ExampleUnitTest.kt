package com.example

import com.example.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testTrackDurationFormatting() {
    val track = Track(
      id = "test_1",
      title = "Test Song",
      artist = "Test Artist",
      album = "Test Album",
      artworkUrl = "",
      audioUrl = "",
      durationSeconds = 205
    )
    assertEquals("3:25", track.formattedDuration)
  }

  @Test
  fun testTrackFileSizeFormatting() {
    val track = Track(
      id = "test_2",
      title = "Test Song",
      artist = "Test Artist",
      album = "Test Album",
      artworkUrl = "",
      audioUrl = "",
      fileSizeBytes = 5 * 1024 * 1024L
    )
    assertEquals("5.0 MB", track.formattedFileSize)
  }

  @Test
  fun testAuthorizedDownloadFlag() {
    val track = Track(
      id = "test_3",
      title = "Licensed Track",
      artist = "Artist",
      album = "Album",
      artworkUrl = "",
      audioUrl = "",
      isAuthorizedDownload = true
    )
    assertTrue(track.isAuthorizedDownload)
    assertFalse(track.isFavorite)
  }

  @Test
  fun testYouTubeTrackAttributes() {
    val track = Track(
      id = "yt_12345",
      title = "Tu Mil Jaaye",
      artist = "Arijit Singh",
      album = "T-Series",
      artworkUrl = "https://i.ytimg.com/vi/12345/hqdefault.jpg",
      audioUrl = "https://www.youtube.com/watch?v=12345",
      youtubeVideoId = "12345",
      channelTitle = "T-Series",
      isYouTubeSource = true
    )
    assertTrue(track.isYouTubeSource)
    assertEquals("12345", track.youtubeVideoId)
    assertEquals("T-Series", track.channelTitle)
    assertEquals("yt_12345", track.id)
  }
}
