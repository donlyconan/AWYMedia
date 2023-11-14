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
        // filter videos that is already in the storage and not contains in the db
        val filteredVideos = allVideos.dropWhile { video ->
            localVideos.any { vd ->
                if(vd.videoUri == video.videoUri) {
                    vd.copyFrom(video)
                    videoDao.update(vd)
                }
                vd.videoUri == video.videoUri
            }
        }.toList()
        videoDao.insert(*filteredVideos.toTypedArray())
        Log.d(TAG, "sync: inserted size = ${filteredVideos.size}")

        // remove all videos that don't exist in the devices
        withContext(Dispatchers.IO) {
            val videoIds = localVideos.filterNot { video ->
                allVideos.any { it.videoUri == video.videoUri }
            }.map { it.videoId }.toIntArray()
            Log.d(TAG, "sync: remove the list no longer exist:  $videoIds")
            videoDao.delete(*videoIds)
            playlistWithVideosDao.deleteByVideoId(* videoIds)
        }
        return filteredVideos.isNotEmpty()
    }


    suspend fun moveToRecycleBin(video: Video) {
        Log.d(TAG, "moveToTrash() called with: video = $video")
        val trash = video.convertToTrash()
        playlistWithVideosDao.deleteByVideoId(video.videoId)
        trashDao.insert(trash)
        videoDao.delete(video.videoId)
    }

    fun insertOrUpdate(video: Video) {
        val existedVideo = get(video.videoUri)
        if(existedVideo == null) {
            insert(video)
        } else {
            update(video)
        }
    }

}