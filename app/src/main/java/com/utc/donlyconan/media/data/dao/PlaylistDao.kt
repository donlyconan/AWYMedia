package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg playlist: Playlist)

    @Update
    fun update(vararg playlist: Playlist)

    @Delete
    fun delete(vararg playlists: Playlist)

    @Query("Select * from playlist where playlist_id=:playlistId")
    fun get(playlistId: Int): Playlist

    @Query("Select * from playlist")
    fun getAll(): LiveData<List<Playlist>>

//    @Query("Select * from video_playlist where playlist_id=:playlistId")
//    fun getAllVideos(playlistId: Int): List<Video>

    @Query("Select count(video_id) from video_playlist where playlist_id=:playlistId and video_id in (Select video_id from videos)")
    fun countVideos(playlistId: Int): Int

    @Query("Delete from video_playlist where video_id=:videoId")
    fun removeVideoFromPlaylist(videoId: Int)

    @Query("Delete from video_playlist where playlist_id=:playlistId")
    fun removePlaylist(playlistId: Int)

    @Query("Select * from playlist where title like :keyword")
    fun findAll(keyword: String): LiveData<List<Playlist>>

    @Query("Select * from playlist where playlist_id=:playlistId")
    fun findById(playlistId: Int): Playlist

}