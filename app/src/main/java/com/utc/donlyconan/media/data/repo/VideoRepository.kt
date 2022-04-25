package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.room.Query
import com.utc.donlyconan.media.data.models.Video
import kotlinx.coroutines.flow.Flow

/**
 * Handle CRUD with Video Objects
 */
interface VideoRepository {

    suspend fun count(): Int

    suspend fun insert(vararg videos: Video)

    fun update(video: Video): Int

    fun countPath(path: String): Int

    suspend fun delete(videoId: Int): Int

    fun getVideo(videoId: Int): Video

}