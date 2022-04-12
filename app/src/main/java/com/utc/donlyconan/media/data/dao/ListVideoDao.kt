package com.utc.donlyconan.media.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video

@Dao
interface ListVideoDao {

    @Query("Select * from videos order by video_name asc")
    fun getAllVideosOrderByVideoName(): PagingSource<Int, Video>

    @Query("Select * from videos order by size asc")
    fun getAllVideosOrderBySize(): PagingSource<Int, Video>

    @Query("Select * from videos order by created_at asc")
    fun getAllVideosOrderByCreation(): PagingSource<Int, Video>

    @Query("Select * from videos order by updated_at asc")
    fun getAllVideosOrderByRecent(): PagingSource<Int, Video>

    @Query("Select * from videos where played_time > 0 order by updated_at desc")
    fun getAllPlayingVideos(): PagingSource<Int, Video>

    @Query("Select * from videos where is_favorite=1 order by updated_at desc")
    fun getAllFavoriteVideos(): PagingSource<Int, Video>

    @Query("Select * from videos where video_id > :startId order by video_id limit 5 ")
    fun getAllNextVideos(startId: Long): PagingSource<Int, Video>

    @Query("Select * from videos where video_name like :keyword order by video_id asc")
    fun findAllVideos(keyword: String): PagingSource<Int, Video>
}
