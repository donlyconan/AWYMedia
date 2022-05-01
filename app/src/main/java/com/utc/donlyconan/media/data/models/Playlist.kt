package com.utc.donlyconan.media.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "playlist")
data class Playlist(
    @ColumnInfo(name = "playlist_id")
    @PrimaryKey(autoGenerate = true)
    var playlistId: Int?,
    var title: String,
    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis()
) {
    @Ignore
    var itemSize: Int = 0
}