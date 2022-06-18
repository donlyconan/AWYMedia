package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.PlaylistWithVideos
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.models.VideoPlaylistCrossRef

@Dao
interface PlaylistWithVideosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg records: VideoPlaylistCrossRef)

    @Query("Delete from video_playlist where playlist_id = :playlistId")
    fun delete(playlistId: Int)

    @Query("Delete from video_playlist where video_id=:videoId and playlist_id=:playlistId")
    fun deleteFromPlaylist(videoId: Int, playlistId: Int)

    @Transaction
    @Query("Select * from playlist")
    fun getPlaylistWithVideos(): LiveData<List<PlaylistWithVideos>>

    @Transaction
    @Query("Select * from playlist where playlist_id = :playlistId")
    fun getPlaylist(playlistId: Int): LiveData<PlaylistWithVideos>

    @Query("Select * from videos where " +
            "video_id=(Select video_id from video_playlist where playlist_id=:playlistId limit 1)")
    fun getFirstVideo(playlistId: Int): Video?
}