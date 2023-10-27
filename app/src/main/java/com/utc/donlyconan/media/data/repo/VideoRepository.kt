package com.utc.donlyconan.media.data.repo

import android.os.Environment
import android.util.Log
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.components.loadAllVideos
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoRepository @Inject constructor(private val videoDao: VideoDao, val trashDao: TrashDao) : VideoDao by videoDao {
    companion object {
        val TAG: String = VideoRepository::class.java.simpleName

        val FOLDERS = listOf(
            androidFile(Environment.DIRECTORY_MOVIES),
            androidFile(Environment.DIRECTORY_DCIM),
            androidFile(Environment.DIRECTORY_DOCUMENTS),
            androidFile(Environment.DIRECTORY_DOWNLOADS),
            androidFile(Environment.DIRECTORY_MUSIC),
            androidFile(Environment.DIRECTORY_PODCASTS),
            androidFile(Environment.DIRECTORY_RINGTONES),
        )
    }

    suspend fun loadAllVideos(): List<Video> {
        val videos = ArrayList<Video>(100)
        FOLDERS.forEach { file ->
            videos.addAll(file.loadAllVideos())
        }
        return videos
    }

    /**
     * Sync data from the local storage with the app's data
     */
    suspend fun sync(): Boolean {
        Log.d(TAG, "sync() called: syncing...")
        val videos = loadAllVideos()
        val newList = ArrayList<Video>(videos.size)
        var synced = false

        for(video in videos) {
            val localVideo = get(video.videoUri)
            if(localVideo != null) {
                val newVideo = video.copy(
                    title = localVideo.title,
                    videoUri = localVideo.videoUri,
                    size = localVideo.size,
                    createdAt = localVideo.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    type = localVideo.type
                )
                newList.add(newVideo)
                synced = true
            } else {
                newList.add(video)
            }
        }
        this.clearVideosWith(false)
        this.update(*newList.toTypedArray())
        return synced
    }

    fun update(video: Video): Int {
        video.updatedAt = System.currentTimeMillis()
        return videoDao.update(video)
    }

    fun moveToRecyleBin(video: Video) {
        Log.d(TAG, "moveToTrash() called with: video = $video")
        val trash = video.convertToTrash()
        trashDao.insert(trash)
        videoDao.delete(video.videoId)
    }

}