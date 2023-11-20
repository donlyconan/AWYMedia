package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg videos: Video)

    @Update
    fun update(vararg video: Video): Int

    @Query("Delete from videos where video_id in (:videoId)")
    suspend fun delete(vararg videoId: Int): Int

    @Query("Delete from videos where video_uri in (:uris)")
    suspend fun delete(vararg uris: String): Int

    @Query("Select * from videos where video_id = :videoId")
    fun get(videoId: Int): Video?

    @Query("Select * from videos where video_uri = :uri")
    fun get(uri: String): Video?

    @Query("Select * from videos where secured = 0")
    fun getAll(): LiveData<List<Video>>

    @Query("Select * from videos where secured = 0")
    fun getAllOnThread(): List<Video>

    @Query("Select * from videos where secured=:isSecured")
    fun getAllVideosBySecuring(isSecured: Boolean): LiveData<List<Video>>

    @Query("Select * from videos where is_favorite=:isFavorite and secured = 0")
    fun getAllFavorites(isFavorite: Boolean = false): LiveData<List<Video>>

    @Query("Select * from videos where video_uri=:path limit 1")
    fun getVideoInfo(path: String): Video

    @Query("Select * from videos where secured = 0")
    suspend fun getAllPublicVideos(): List<Video>

    @Query("Select video_name from videos where secured = 1")
    fun getAllTitlesInPrivateFolder(): List<String>

    @Query("Select * from videos where secured = 1")
    fun getAllSecuredVideos(): List<Video>

    @Query("Select * from videos where video_uri like :partUri")
    fun getLikely(partUri: String): Video?

}