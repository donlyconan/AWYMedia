package com.utc.donlyconan.media.data.repo

import com.utc.donlyconan.media.data.models.Video

/**
 * Handle CRUD with Video Objects
 */
interface VideoRepository {

    suspend fun count(): Int

    fun insert(vararg videos: Video)

    fun update(video: Video): Int

    fun countPath(path: String): Int

    fun delete(videoId: Int): Int

    fun getVideo(videoId: Int): Video

    fun getNext(videoId: Int): Video

    fun getPrevious(videoId: Int): Video

    fun moveToTrash(video: Video)

}