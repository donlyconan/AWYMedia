package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.utc.donlyconan.media.data.models.Video

@Dao
interface ListVideoDao {

    @Query("Select * from videos order by video_name asc")
    fun getAllVideosOrderByVideoName(): LiveData<List<Video>>

    @Query("Select * from videos order by size asc")
    fun getAllVideos(): LiveData<List<Video>>

    @Query("Select * from videos where played_time > 0 order by updated_at desc")
    fun getAllPlayingVideos(): LiveData<List<Video>>

    @Query("Select * from videos where is_favorite=1 order by updated_at desc")
    fun getAllFavoriteVideos(): LiveData<List<Video>>

    @Query("Select * from videos where video_id > :startId order by video_id limit 5 ")
    fun getAllNextVideos(startId: Long): LiveData<List<Video>>

    @Query("Select * from videos where video_name like :keyword order by video_id asc")
    fun findAllVideos(keyword: String): LiveData<List<Video>>

    @Query("Select * from videos")
    fun getListInTrash(): LiveData<List<Video>>

    @Query("Select * from videos where video_id not in (select video_id from video_playlist where playlist_id = :playlistId)")
    fun getAllVideosNotInPlaylist(playlistId: Int): LiveData<List<Video>>

}
