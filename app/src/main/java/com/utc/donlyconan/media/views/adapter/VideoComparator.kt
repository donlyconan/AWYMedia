package com.utc.donlyconan.media.views.adapter

import androidx.recyclerview.widget.DiffUtil
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video

object VideoComparator: DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }

}

object PlaylistComparator: DiffUtil.ItemCallback<Playlist>() {

    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.playlistId == newItem.playlistId
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

}