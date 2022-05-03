package com.utc.donlyconan.media.data.models

import android.os.Parcelable
import androidx.room.*
import com.utc.donlyconan.media.app.settings.Settings
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos", indices = [Index(value = ["path"], unique = true)])
data class Video(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "video_id")
    var videoId: Int,
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
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,
) : Parcelable {
    @Ignore
    var isSelected: Boolean = false

    fun compareTo(v: Video, sortBy: Int) = when (sortBy) {
        Settings.SORT_BY_CREATION -> {
            (createdAt - v.createdAt).toInt()
        }
        Settings.SORT_BY_DURATION -> {
            duration - v.duration
        }
        Settings.SORT_BY_RECENT -> {
            (updatedAt - v.updatedAt).toInt()
        }
        else -> {
            val fch = title!!.first()
            val sch = v.title!!.first()
            fch.toInt() - sch.toInt()
        }
    }

    fun toTrash(): Trash {
        return Trash(videoId, title, path, duration, size, type, createdAt,
            updatedAt, System.currentTimeMillis())
    }

}