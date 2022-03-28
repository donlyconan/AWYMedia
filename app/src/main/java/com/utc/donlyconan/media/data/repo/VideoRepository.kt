package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.models.Video

interface VideoRepository {

    fun getAllVideos(): LiveData<List<Video>>

    suspend fun insertVideo(vararg videos: Video)

    fun updateVideo(video: Video): Int

    suspend fun countVideoWithUri(dataUri: String): Int

    suspend fun deleteVideo(videoId: Long): Int

    fun getAllPlayingVideos(): LiveData<List<Video>>

    fun getAllFavoriteVideo(): LiveData<List<Video>>

    fun getAllNextVideos(startId: Long): LiveData<List<Video>>

    fun hasUrl(url: String): Boolean
}