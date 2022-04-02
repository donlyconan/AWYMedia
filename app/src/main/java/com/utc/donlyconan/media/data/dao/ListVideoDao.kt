package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video

@Dao
interface ListVideosDao {

    @Query("Select * from videos")
    fun getAllVideos(): LiveData<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(vararg videos: Video)

    @Update
    fun updateVideo(video: Video): Int

    @Query("Select count(video_id) from videos where url = :dataUri")
    suspend fun countVideoWithUri(dataUri: String): Int

    @Query("Delete from videos where video_id = :videoId")
    suspend fun deleteVideo(videoId: Long): Int

    @Query("Select * from videos where video_id = :videoId")
    fun getVideoWithId(videoId: Long): LiveData<Video>

    @Query("Select * from videos where played_time > 0 order by updated_at desc")
    fun getAllPlayingVideos(): LiveData<List<Video>>

    @Query("Select * from videos where is_favorite=1 order by updated_at desc")
    fun getAllFavoriteVideos(): LiveData<List<Video>>

    @Query("Select * from videos where video_id > :startId order by video_id limit 5")
    fun getAllNextVideos(startId: Long): LiveData<List<Video>>

    @Query("Select count(video_id) from videos where url=:url")
    fun countUrl(url: String): Int

    @Query("Select * from videos where video_name like :keyword")
    fun findAllVideos(keyword: String): LiveData<List<Video>>
}
