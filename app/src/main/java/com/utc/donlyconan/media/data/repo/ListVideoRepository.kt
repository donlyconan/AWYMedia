package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.utc.donlyconan.media.data.models.Video
import kotlinx.coroutines.flow.Flow

interface ListVideoRepository {

    suspend fun loadAllVideos(): List<Video>

    fun getAllVideos(sortId: Int): Flow<PagingData<Video>>

    fun getAllPlayingVideos(): Flow<PagingData<Video>>

    fun getAllFavoriteVideo(): Flow<PagingData<Video>>

    fun getAllNextVideos(startId: Long): Flow<PagingData<Video>>

    fun findAllVideos(keyword: String): Flow<PagingData<Video>>

    fun getListInTrash(): Flow<PagingData<Video>>
}