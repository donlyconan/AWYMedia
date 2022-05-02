package com.utc.donlyconan.media.data.models

import android.os.Parcelable
import androidx.room.*
import com.utc.donlyconan.media.app.settings.Settings
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "trashes", indices = [Index(value = ["path"], unique = true)])
open class Trash(
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
    @ColumnInfo(name = "created_at")
    var createdAt: Long,
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long = System.currentTimeMillis(),

) : Parcelable {
    @Ignore
    var isSelected: Boolean = false

    fun compareTo(v: Trash, sortBy: Int) = when (sortBy) {
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

    fun toVideo(): Video {
        return Video(videoId, title, path, duration, size, type, 0, createdAt, updatedAt, false)
    }

}