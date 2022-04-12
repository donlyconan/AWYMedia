package com.utc.donlyconan.media.data.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos", indices = [Index(value = ["path"], unique = true)])
data class Video(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "video_id")
    var videoId: Long,
    @ColumnInfo(name = "video_name")
    var title: String?,
    @ColumnInfo(name = "path")
    var path: String,
    @ColumnInfo(name = "duration")
    var duration: Int,
    @ColumnInfo(name = "size")
    var size: Long,
    @ColumnInfo(name = "type")
    var type: String?,
    @ColumnInfo(name = "played_time")
    var playedTime: Long = 0,
    @ColumnInfo(name = "created_at")
    var createdAt: Long,
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long?= null,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false
): Parcelable