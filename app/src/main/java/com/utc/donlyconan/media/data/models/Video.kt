package com.utc.donlyconan.media.data.models

import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.views.adapter.Selectable
import java.io.File
import java.util.Calendar

@Entity(tableName = "videos", indices = [Index(value = ["video_uri"], unique = true)])
data class Video(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "video_id")
    var videoId: Int,
    @ColumnInfo(name = "video_name")
    var title: String?,
    @ColumnInfo(name = "video_uri")
    var videoUri: String,
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
    var updatedAt: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "secured")
    var isSecured: Boolean = false,
    @ColumnInfo(name = "subtitle_uri")
    var subtitleUri: String? = null,
) : Selectable {
    @Ignore
    var isChecked: Boolean = false

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

    fun convertToTrash(): Trash {
        return Trash(videoId, title, videoUri, duration, size, type, createdAt,
            updatedAt, deletedAt = System.currentTimeMillis(), isSecured, subtitleUri)
    }

    companion object {
        fun fromFile(file: File): Video {
            return Video(
                videoId = 0,
                title = file.name,
                videoUri = file.toUri().toString(),
                duration = 0,
                size = file.length(),
                createdAt = file.lastModified(),
                type = file.extension
            )
        }

        val diffUtil = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if(oldItem is Video && newItem is Video) {
                    oldItem.videoId == newItem.videoId
                } else if(oldItem is String && newItem is String) {
                    oldItem == newItem
                } else {
                    false
                }
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }

        val diffVideoUtil = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.videoId == newItem.videoId
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun isSelected(): Boolean {
        return isChecked
    }

    override fun setSelected(isSelected: Boolean) {
        this.isChecked = isSelected
    }

}