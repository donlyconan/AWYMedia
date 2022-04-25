package com.utc.donlyconan.media.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "video_playlist", primaryKeys = ["video_id", "playlist_id"])
data class VideoPlaylistCrossRef(
    @ColumnInfo(name = "video_id")
    var videoId: Int,
    @ColumnInfo(name = "playlist_id")
    var playlistId: Int,
    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis()
)