package com.utc.donlyconan.media.data.dao

import android.view.View
import androidx.paging.PagingSource
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video

@Dao
interface ListVideoDao {

    @Query("Select * from videos where deleted_at is null order by video_name asc")
    fun getAllVideosOrderByVideoName(): PagingSource<Int, Video>

    @Query("Select * from videos where deleted_at is null order by size asc")
    fun getAllVideosOrderBySize(): PagingSource<Int, Video>

    @Query("Select * from videos where deleted_at is null order by created_at asc")
    fun getAllVideosOrderByCreation(): PagingSource<Int, Video>

    @Query("Select * from videos where deleted_at is null order by updated_at asc")
    fun getAllVideosOrderByRecent(): PagingSource<Int, Video>

    @Query("Select * from videos where played_time > 0 and deleted_at is null order by updated_at desc")
    fun getAllPlayingVideos(): PagingSource<Int, Video>

    @Query("Select * from videos where is_favorite=1 and deleted_at is null order by updated_at desc")
    fun getAllFavoriteVideos(): PagingSource<Int, Video>

    @Query("Select * from videos where video_id > :startId and deleted_at is null order by video_id limit 5 ")
    fun getAllNextVideos(startId: Long): PagingSource<Int, Video>

    @Query("Select * from videos where video_name like :keyword and deleted_at is null order by video_id asc")
    fun findAllVideos(keyword: String): PagingSource<Int, Video>

    @Query("Select * from videos where deleted_at is not null order by deleted_at asc")
    fun getListInTrash(): PagingSource<Int, Video>

}
