package com.utc.donlyconan.media.data.models

import androidx.recyclerview.widget.DiffUtil
import androidx.room.*
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.views.adapter.Selectable

@Entity(tableName = "trashes", indices = [Index(value = ["video_uri"], unique = true)])
data class Trash(
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
    @ColumnInfo(name = "created_at")
    var createdAt: Long,
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = now(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long = now(),
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "secured")
    var isSecured: Boolean = false,
    @ColumnInfo(name = "subtitle_uri")
    var subtitleUri: String? = null,
): Selectable {
    @Ignore
    var isChecked: Boolean = false

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

    override fun setSelected(isSelected: Boolean) {
        isChecked = isSelected
    }

    override fun isSelected(): Boolean {
        return isChecked
    }

    fun convertToVideo(): Video {
        return Video(videoId, title, videoUri, duration, size, type, 0, createdAt, now(), isFavorite, isSecured, subtitleUri)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if (oldItem is Trash && newItem is Trash) {
                    oldItem.videoId == newItem.videoId
                } else if (oldItem is String && newItem is String) {
                    oldItem == newItem
                } else {
                    false
                }
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }

}