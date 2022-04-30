package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.models.Video

interface ListVideoRepository {

    suspend fun loadAllVideos(): List<Video>

    fun getAllVideos(): LiveData<List<Video>>

    fun getAllPlayingVideos(): LiveData<List<Video>>

    fun getAllFavoriteVideo(): LiveData<List<Video>>

    fun getAllNextVideos(startId: Long): LiveData<List<Video>>

    fun findAllVideos(keyword: String): LiveData<List<Video>>

    fun getListInTrash(): LiveData<List<Video>>
}