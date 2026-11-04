package com.example.download

import android.content.Context
import android.util.Log
import com.example.model.DownloadStatus
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class DownloadManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.IO + Job())
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  private val downloadDir: File by lazy {
    File(context.filesDir, "melodyx_music").apply {
      if (!exists()) mkdirs()
    }
  }

  // Active download jobs keyed by track ID
  private val activeJobs = mutableMapOf<String, Job>()

  // Map of trackId to download progress (0f..1f)
  private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
  val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

  // Map of trackId to download status
  private val _downloadStatusMap = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
  val downloadStatusMap: StateFlow<Map<String, DownloadStatus>> = _downloadStatusMap.asStateFlow()

  // Total storage in bytes
  private val _totalStorageUsedBytes = MutableStateFlow(0L)
  val totalStorageUsedBytes: StateFlow<Long> = _totalStorageUsedBytes.asStateFlow()

  init {
    refreshDownloadedFiles()
  }

  fun refreshDownloadedFiles() {
    val initialStatus = mutableMapOf<String, DownloadStatus>()
    var totalBytes = 0L

    if (downloadDir.exists()) {
      downloadDir.listFiles()?.forEach { file ->
        if (file.isFile && file.length() > 0) {
          val trackId = file.nameWithoutExtension
          initialStatus[trackId] = DownloadStatus.DOWNLOADED
          totalBytes += file.length()
        }
      }
    }
    _downloadStatusMap.value = initialStatus
    _totalStorageUsedBytes.value = totalBytes
  }

  fun isTrackDownloaded(trackId: String): Boolean {
    return _downloadStatusMap.value[trackId] == DownloadStatus.DOWNLOADED && getLocalFile(trackId)?.exists() == true
  }

  fun getLocalFile(trackId: String): File? {
    val file = File(downloadDir, "$trackId.m4a")
    return if (file.exists() && file.length() > 0) file else null
  }

  fun downloadTrack(
    track: Track,
    onSuccess: (File) -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    if (!track.isAuthorizedDownload) {
      onError("Offline downloading is not licensed for this track by the copyright holder.")
      return
    }

    if (isTrackDownloaded(track.id)) {
      getLocalFile(track.id)?.let { onSuccess(it) }
      return
    }

    val job = scope.launch {
      updateStatus(track.id, DownloadStatus.DOWNLOADING)
      updateProgress(track.id, 0.05f)

      val targetFile = File(downloadDir, "${track.id}.m4a")
      val tempFile = File(downloadDir, "${track.id}.tmp")

      try {
        val request = Request.Builder()
          .url(track.audioUrl)
          .addHeader("User-Agent", "SunoJara-Music/1.0")
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
          }

          val body = response.body ?: throw Exception("Empty response body")
          val totalBytes = body.contentLength()
          val inputStream: InputStream = body.byteStream()
          val outputStream = FileOutputStream(tempFile)

          val buffer = ByteArray(8 * 1024)
          var downloadedBytes = 0L
          var read: Int

          while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
            downloadedBytes += read

            if (totalBytes > 0) {
              val progress = (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
              updateProgress(track.id, progress)
            }
          }

          outputStream.flush()
          outputStream.close()
          inputStream.close()

          if (tempFile.renameTo(targetFile)) {
            updateStatus(track.id, DownloadStatus.DOWNLOADED)
            updateProgress(track.id, 1f)
            refreshDownloadedFiles()
            withContext(Dispatchers.Main) {
              onSuccess(targetFile)
            }
          } else {
            throw Exception("Failed to rename temporary download file")
          }
        }
      } catch (e: Exception) {
        Log.e("DownloadManager", "Failed to download track ${track.title}", e)
        if (tempFile.exists()) tempFile.delete()
        updateStatus(track.id, DownloadStatus.ERROR)
        withContext(Dispatchers.Main) {
          onError(e.localizedMessage ?: "Download failed")
        }
      } finally {
        activeJobs.remove(track.id)
      }
    }

    activeJobs[track.id] = job
  }

  fun cancelDownload(trackId: String) {
    activeJobs[trackId]?.cancel()
    activeJobs.remove(trackId)
    val tempFile = File(downloadDir, "$trackId.tmp")
    if (tempFile.exists()) tempFile.delete()
    updateStatus(trackId, DownloadStatus.NOT_DOWNLOADED)
    updateProgress(trackId, 0f)
  }

  fun deleteDownload(trackId: String): Boolean {
    cancelDownload(trackId)
    val file = File(downloadDir, "$trackId.m4a")
    val deleted = if (file.exists()) file.delete() else false
    refreshDownloadedFiles()
    return deleted
  }

  fun clearAllDownloads(): Long {
    var freedBytes = 0L
    downloadDir.listFiles()?.forEach { file ->
      if (file.isFile) {
        freedBytes += file.length()
        file.delete()
      }
    }
    refreshDownloadedFiles()
    return freedBytes
  }

  private fun updateStatus(trackId: String, status: DownloadStatus) {
    val current = _downloadStatusMap.value.toMutableMap()
    current[trackId] = status
    _downloadStatusMap.value = current
  }

  private fun updateProgress(trackId: String, progress: Float) {
    val current = _downloadProgressMap.value.toMutableMap()
    current[trackId] = progress
    _downloadProgressMap.value = current
  }
}
