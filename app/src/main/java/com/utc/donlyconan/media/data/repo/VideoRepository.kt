package com.utc.donlyconan.media.data.repo

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.components.loadAllVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoRepository @Inject constructor(
    val contentResolver: ContentResolver,
    private val videoDao: VideoDao ,
    val trashDao: TrashDao,
    val playlistWithVideosDao: PlaylistWithVideosDao) : VideoDao by videoDao {
    companion object {
        val TAG: String = VideoRepository::class.java.simpleName
    }

    suspend fun loadAllVideos(): List<Video> {
        val videos = contentResolver.loadAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        Log.d(TAG, "loadAllVideos() video size = ${videos.size}")
        return videos
    }

    /**
     * Sync data from the local storage with the app's data
     */
    suspend fun sync(): Boolean {
        Log.d(TAG, "sync() called: syncing...")
        val localVideos = videoDao.getAllPublicVideos()
        val allVideos = loadAllVideos()
        val filteredVideos = allVideos.dropWhile { video ->
            localVideos.firstOrNull() { it.videoUri == video.videoUri }?.also { vd ->
                if(!vd.equals(video)) {
                    vd.copyFrom(video)
                    videoDao.update(vd)
                }
            } != null
        }.toList()
        Log.d(TAG, "sync: insert size = ${filteredVideos.size}")
        videoDao.insert(*filteredVideos.toTypedArray())

        // remove all videos that don't exist in the devices
        withContext(Dispatchers.IO) {
            val noExistedVideos = localVideos.filterNot { video ->
                allVideos.any { it.videoUri == video.videoUri }
            }.map { it.videoUri }.toList()
            Log.d(TAG, "sync: remove the list no longer exist:  $noExistedVideos")
            videoDao.delete(*noExistedVideos.toTypedArray())
        }
        return filteredVideos.isNotEmpty()
    }


    suspend fun moveToRecycleBin(video: Video) {
        Log.d(TAG, "moveToTrash() called with: video = $video")
        val trash = video.convertToTrash()
        playlistWithVideosDao.removeVideo(video.videoId)
        trashDao.insert(trash)
        videoDao.delete(video.videoId)
    }

}