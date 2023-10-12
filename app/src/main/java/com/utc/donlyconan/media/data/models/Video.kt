package com.utc.donlyconan.media.data.models

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.*
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.views.adapter.Selectable
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
) : Parcelable, Selectable {
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

    fun toTrash(): Trash {
        return Trash(videoId, title, path, duration, size, type, createdAt,
            updatedAt, System.currentTimeMillis())
    }

    companion object {
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