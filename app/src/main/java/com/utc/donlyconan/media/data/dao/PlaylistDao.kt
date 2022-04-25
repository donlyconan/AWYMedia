package com.utc.donlyconan.media.data.dao

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.*
import com.utc.donlyconan.media.data.models.Playlist

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg playlist: Playlist)

    @Delete
    fun delete(vararg playlist: Playlist)

    @Query("Select * from playlist")
    fun getAllPlaylist(): PagingSource<Int, Playlist>

}