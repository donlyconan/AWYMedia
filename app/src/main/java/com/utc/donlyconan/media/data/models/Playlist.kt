package com.utc.donlyconan.media.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_group")
data class Playlist(
    @PrimaryKey
    var playlistId: Int,
    var title: String,
    var createdAt: Long?,
    var updatedAt: Long?
)