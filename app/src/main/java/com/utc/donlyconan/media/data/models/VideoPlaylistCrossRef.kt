package com.utc.donlyconan.media.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import com.utc.donlyconan.media.app.utils.now


@Entity(
    tableName = "video_playlist",
    primaryKeys = ["video_id", "playlist_id"],
)
data class VideoPlaylistCrossRef(
    @ColumnInfo(name = "video_id")
    var videoId: Int,
    @ColumnInfo(name = "playlist_id")
    var playlistId: Int,
    @ColumnInfo(name = "video_uri")
    var videoUri: String,
    @ColumnInfo(name = "created_at")
    var createdAt: Long = now(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = now()
)