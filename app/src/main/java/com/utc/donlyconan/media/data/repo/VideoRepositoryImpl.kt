package com.utc.donlyconan.media.data.repo

import android.util.Log
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoRepositoryImpl @Inject constructor(private val videoDao: VideoDao,
                                              private val trashDao: TrashDao) : VideoRepository {

    companion object {
        val TAG: String = VideoRepository::class.java.simpleName
    }

    override fun count(): Int = videoDao.count()

    override fun insert(vararg videos: Video) {
        videoDao.insert(*videos)
    }

    override fun update(video: Video): Int {
        video.updatedAt = System.currentTimeMillis()
        return videoDao.update(video)
    }

    override fun countPath(path: String): Int = videoDao.countPath(path)

    override fun delete(videoId: Int): Int = videoDao.delete(videoId)

    override fun getVideo(videoId: Int): Video = videoDao.getVideo(videoId)

    override fun getNext(videoId: Int): Video = videoDao.getNextVideo(videoId)

    override fun getPrevious(videoId: Int): Video = videoDao.getPreviousVideo(videoId)

    override fun moveToTrash(video: Video) {
        Log.d(TAG, "moveToTrash() called with: video = $video")
        val trash = video.toTrash()
        trashDao.insert(trash)
        videoDao.delete(video.videoId)
    }

}