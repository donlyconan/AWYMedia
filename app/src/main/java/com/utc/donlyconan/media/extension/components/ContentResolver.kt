package com.utc.donlyconan.media.extension.components

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.data.models.Video
import java.io.File
import java.io.FileFilter

const val TAG = "ContentResolver"

val FOLDERS = listOf(
    androidFile(Environment.DIRECTORY_MOVIES),
    androidFile(Environment.DIRECTORY_DCIM),
    androidFile(Environment.DIRECTORY_DOCUMENTS),
    androidFile(Environment.DIRECTORY_DOWNLOADS),
    androidFile(Environment.DIRECTORY_MUSIC),
    androidFile(Environment.DIRECTORY_PODCASTS),
    androidFile(Environment.DIRECTORY_RINGTONES),
)

/**
 * Get all videos in the device
 */
fun ContentResolver.getAllVideos(uri: Uri, selection: String? = null, sortOrder: String? = null): List<Video> {
    Log.d(TAG, "getAllVideos() called with: uri = $uri, selection = $selection, sortOrder = $sortOrder")

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.SIZE,
    )
    val videoList = ArrayList<Video>()

    query(uri, projection, selection, null, sortOrder)
        ?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val createdAtColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val updatedAtColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val videoId = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getInt(durationColumn)
                var createdAt = cursor.getLong(createdAtColumn)
                val updatedAt = cursor.getLong(updatedAtColumn)
                val size = cursor.getLong(sizeColumn)
                val type = title.split('.').last()
                val data =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
                if (createdAt == 0L) {
                    createdAt = System.currentTimeMillis()
                }
                videoList += Video(
                    videoId.toInt(), title, data.toString(), duration, size, type, 0L,
                    createdAt, updatedAt
                )
            }
        }

    Log.d(TAG, "getAllVideos: loaded size=" + videoList.size)
    return videoList
}

/**
 * Getting video info from uri
 * Uri should be a specify video
 */
fun ContentResolver.getVideoInfo(uri: Uri): Video? {
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.SIZE,
    )
    query(uri, projection, null, null, null)
        ?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val createdAtColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val updatedAtColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val videoId = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getInt(durationColumn)
                var createdAt = cursor.getLong(createdAtColumn)
                val updatedAt = cursor.getLong(updatedAtColumn)
                val size = cursor.getLong(sizeColumn)
                val type = title.split('.').last()
                if (createdAt == 0L) {
                    createdAt = System.currentTimeMillis()
                }
                return Video(videoId.toInt(), title, uri.toString(), duration, size, type, 0L,
                    createdAt, updatedAt)
            }
        }
    return null
}

var videoExtentions = listOf("mp4", "wav", "mp3", "ogg", "webm", "fmp4", "m4a")

fun File.loadAllVideos(excludes: List<String>? = null): List<Video>  {
    val filter = FileFilter { file ->
        if(excludes?.contains(file.name) == true) {
            false
        } else {
            videoExtentions.any { ext -> file.extension == ext}
        }
    }
    val videos = arrayListOf<Video>()
    recursiveFile(listFiles(filter), filter, videos)
    return videos
}

fun File.browseFolder(excludes: List<String>? = null, onAccepted: (file: File) -> Unit) {
    val filter = FileFilter { file ->
        if(excludes?.contains(file.name) == true) {
            false
        } else {
            videoExtentions.any { ext -> file.extension == ext}
        }
    }
    recursiveFile(listFiles(filter), filter, onAccepted = onAccepted)
}

fun recursiveFile(listFiles: Array<File>?, fileFilter: FileFilter, videos: ArrayList<Video>? = null, onAccepted: (file: File) -> Unit? = {_ -> }) {
    listFiles?.let { files ->
        for (file in files) {
            if(file.isFile) {
                videos?.add(Video.fromFile(file))
                onAccepted.invoke(file)
            } else {
                recursiveFile(file.listFiles(fileFilter), fileFilter, videos)
            }
        }
    }
}

fun String.getMediaUri(context: Context, onFinish: (uri: Uri) -> Unit) {
    MediaScannerConnection.scanFile(context, arrayOf(toUri().toFile().absolutePath), arrayOf("*/*")) { path, uri ->
        onFinish.invoke(uri)
    }
}