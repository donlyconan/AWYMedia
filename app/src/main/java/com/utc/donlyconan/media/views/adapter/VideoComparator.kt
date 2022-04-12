package com.utc.donlyconan.media.views.adapter

import androidx.recyclerview.widget.DiffUtil
import com.utc.donlyconan.media.data.models.Video

object VideoComparator: DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }

}