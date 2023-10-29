package com.utc.donlyconan.media.data.dao

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    /**
     * Returns number of records available
     */
    @Query("Select count(video_id) from videos")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg videos: Video)

    @Update
    fun update(vararg video: Video): Int

    @Query("Delete from videos where video_id = :videoId")
    suspend fun delete(videoId: Int): Int

    @Query("Delete from videos where video_uri in (:uris)")
    suspend fun delete(vararg uris: String): Int

    @Query("Select * from videos where video_id = :videoId")
    fun get(videoId: Int): Video

    @Query("Select * from videos where video_uri = :uri")
    fun get(uri: String): Video?

    @Query("Select * from videos")
    fun getAll(): LiveData<List<Video>>

    @Query("Select * from videos")
    fun getAllAsFlow(): Flow<Video>


    @Query("Select * from videos where secured=:isSecured")
    fun getAllVideosBySecuring(isSecured: Boolean): LiveData<List<Video>>

    @Query("Select * from videos where is_favorite=:isFavorite")
    fun getAllFavorites(isFavorite: Boolean = false): LiveData<List<Video>>

    @Query("Select * from videos where video_id > :fromId order by video_id asc limit 1")
    fun getNextVideo(fromId: Int): Video

    @Query("Select * from videos where video_id < :fromId order by video_id desc limit 1")
    fun getPreviousVideo(fromId: Int): Video

    @Query("Select count(video_id) from videos where video_uri=:path")
    fun countPath(path: String): Int

    @Query("Select * from videos where video_uri=:path limit 1")
    fun getVideoInfo(path: String): Video

    @Query("Select * from videos where secured = 0")
    suspend fun getAllPublicVideos(): List<Video>

    @Query("Delete from videos where secured = :isSecured")
    fun clearVideosWith(isSecured: Boolean = false): Int
}