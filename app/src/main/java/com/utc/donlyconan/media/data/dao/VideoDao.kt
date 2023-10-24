package com.utc.donlyconan.media.data.dao

import android.database.Cursor
import androidx.room.*
import com.utc.donlyconan.media.data.models.Video

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
    fun update(video: Video): Int

    @Query("Delete from videos where video_id = :videoId")
    fun delete(videoId: Int): Int

    @Query("Select * from videos where video_id = :videoId")
    fun get(videoId: Int): Video

    @Query("Select * from videos where path = :uri")
    fun get(uri: String): Video

    @Query("Select * from videos where video_id > :fromId order by video_id asc limit 1")
    fun getNextVideo(fromId: Int): Video

    @Query("Select * from videos where video_id < :fromId order by video_id desc limit 1")
    fun getPreviousVideo(fromId: Int): Video

    @Query("Select count(video_id) from videos where path=:path")
    fun countPath(path: String): Int

    @Query("Select * from videos where path=:path limit 1")
    fun getVideoInfo(path: String): Video

    @Query("Select * from videos ")
    fun iterator(): Cursor
}