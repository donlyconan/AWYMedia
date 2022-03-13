package com.utc.donlyconan.media.data.repo

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.utc.donlyconan.media.data.models.Video

class VideoRepository {

    lateinit var videoList: ArrayList<Video>
    lateinit var context: Context

    constructor(context: Context) {
        Log.d(TAG, "constructor() called with: context = $context")
        this.context = context
        videoList = loadVideos()
    }

    fun loadVideos(): ArrayList<Video> {
        Log.d(TAG, "loadVideosWith() called with: context = $context")

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.SIZE,
        )
        val cursor: Cursor? = context.getContentResolver()
            .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
        val videoList = ArrayList<Video>(cursor?.count ?: 0)
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getInt(durationColumn)
                val date = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val data =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val video = Video(id, data, title, duration, size, date)
                videoList.add(video)
            }
        }
        Log.d(TAG, "loadVideos: loaded size=" + videoList.size)
        return videoList
    }

    companion object {
        val TAG = VideoRepository::class.java.simpleName
    }

}