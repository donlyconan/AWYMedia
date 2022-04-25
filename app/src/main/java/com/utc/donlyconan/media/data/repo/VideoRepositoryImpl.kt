package com.utc.donlyconan.media.data.repo

import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoRepositoryImpl @Inject constructor(private val dao: VideoDao) : VideoRepository {

    companion object {
        val TAG = VideoRepository::class.java.simpleName
    }

    override suspend fun count(): Int = dao.count()

    override suspend fun insert(vararg videos: Video) = dao.insert(*videos)

    override fun update(video: Video): Int = dao.update(video)

    override fun countPath(path: String): Int = dao.countPath(path)

    override suspend fun delete(videoId: Int): Int = dao.delete(videoId)

    override fun getVideo(videoId: Int): Video = dao.getVideo(videoId)

}