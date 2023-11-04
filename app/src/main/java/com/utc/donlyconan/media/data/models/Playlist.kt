package com.utc.donlyconan.media.data.models

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.views.adapter.Selectable

@Entity(tableName = "playlist")
data class Playlist(
    @ColumnInfo(name = "playlist_id")
    @PrimaryKey(autoGenerate = true)
    var playlistId: Int?,
    var title: String,
    @ColumnInfo(name = "created_at")
    var createdAt: Long = now(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = now()
): Selectable {
    @Ignore
    var itemSize: Int = 0
    @Ignore
    var firstVideo: Video? = null
    @Ignore
    var isChecked: Boolean = false

    fun compareTo(v: Playlist, sortBy: Int) = when (sortBy) {
        Settings.SORT_BY_NAME_UP -> {
            val fch = title.first()
            val sch = v.title.first()
            sch.code - fch.code
        }
        else -> {
            val fch = title.first()
            val sch = v.title.first()
            fch.code - sch.code
        }
    }

    override fun isSelected(): Boolean {
        return isChecked
    }

    override fun setSelected(isSelected: Boolean) {
        this.isChecked = isSelected
    }

    companion object {

        const val NO_PLAYLIST = -1
        const val PRIVATE_PLAYLIST_FOLDER = Int.MIN_VALUE

        var diffUtil =  object : DiffUtil.ItemCallback<Playlist>() {

            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem.playlistId == newItem.playlistId
            }

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem == newItem
            }

        }
    }
}