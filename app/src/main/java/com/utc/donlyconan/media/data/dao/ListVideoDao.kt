package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.utc.donlyconan.media.data.models.Video

@Dao
interface ListVideoDao {

    @Query("Select * from videos where played_time > 0 and secured=0 order by updated_at desc")
    fun getAllPlayingVideos(): LiveData<List<Video>>

    @Query("Select * from videos where secured=0 and video_id not in (select video_id from video_playlist where playlist_id = :playlistId)")
    fun getAllVideosNotInPlaylist(playlistId: Int): LiveData<List<Video>>

}
