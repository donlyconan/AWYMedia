package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Playlist

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg playlist: Playlist)

    @Delete
    fun delete(vararg playlists: Playlist)

    @Query("Select count(video_id) from video_playlist where playlist_id=:playlistId")
    fun countVideos(playlistId: Int): Int

    @Query("Select * from playlist")
    fun getAllPlaylist(): LiveData<List<Playlist>>

}