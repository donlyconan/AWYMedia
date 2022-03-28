package com.utc.donlyconan.media.extension.components

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.utc.donlyconan.media.data.models.Video

const val TAG = "ContentResolver"


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
            val createdAt = cursor.getLong(createdAtColumn)
            val updatedAt = cursor.getLong(updatedAtColumn)
            val size = cursor.getLong(sizeColumn)
            val type = title.split('.').last()
            val data =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
            videoList += Video(videoId, title, data.toString(), duration, size, type, 0L,
                createdAt, updatedAt)
        }
    }

    Log.d(TAG, "getAllVideos: loaded size=" + videoList.size)
    return videoList
}