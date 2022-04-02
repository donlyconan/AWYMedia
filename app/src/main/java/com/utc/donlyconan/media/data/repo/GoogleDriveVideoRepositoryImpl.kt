package com.utc.donlyconan.media.data.repo

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.dao.ListVideosDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.components.getAllVideos

class GoogleDriveVideoRepositoryImpl(val dao: ListVideosDao, val contentResolver: ContentResolver): VideoRepository {

    suspend fun loadVideos(): List<Video> = contentResolver.getAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

    override fun getAllVideos(): LiveData<List<Video>> = dao.getAllVideos()

    override suspend fun insertVideo(vararg videos: Video) = dao.insertVideo(*videos)

    override fun updateVideo(video: Video): Int = dao.updateVideo(video)

    override suspend fun countVideoWithUri(dataUri: String): Int = dao.countVideoWithUri(dataUri)

    override suspend fun deleteVideo(videoId: Long): Int = dao.deleteVideo(videoId)

    override fun getAllPlayingVideos(): LiveData<List<Video>> = dao.getAllPlayingVideos()

    override fun getAllFavoriteVideo(): LiveData<List<Video>> = dao.getAllFavoriteVideos()

    override fun getAllNextVideos(startId: Long): LiveData<List<Video>> = dao.getAllNextVideos(startId)

    override fun findAllVideos(keyword: String) = dao.findAllVideos(keyword)

    override fun hasUrl(url: String): Boolean {
        val count = dao.countUrl(url)
        Log.d(TAG, "hasUrl() called with: url = $url, count=$count")
        return  count != 0
    }

    companion object {
        val TAG = VideoRepository::class.java.simpleName
    }
}