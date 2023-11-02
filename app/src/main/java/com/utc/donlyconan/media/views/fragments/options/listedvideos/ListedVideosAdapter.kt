package com.utc.donlyconan.media.views.fragments.options.listedvideos

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.formatShortTime
import com.utc.donlyconan.media.app.utils.setVideoImage
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemSimpleVideoViewBinding
import com.utc.donlyconan.media.views.adapter.BaseAdapter


class ListedVideosAdapter(
    var context: Context, data: List<Video>,
) : BaseAdapter<Video>(Video.diffVideoUtil, data) {

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalVideoHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val binding = ItemSimpleVideoViewBinding.inflate(inflater, parent, false)
        return LocalVideoHolder(binding)
    }

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        holder.bind(item)
    }

    class LocalVideoHolder(val binding: ItemSimpleVideoViewBinding) :
        LocalHolder(binding), View.OnClickListener, View.OnLongClickListener {

        override fun bind(value: Any) {
            val video = value as Video
            binding.tvTitle.text = video.title
            binding.imgChecked.isChecked = video.isChecked
            binding.imgThumbnail.setVideoImage(video.videoUri)
            binding.progress.visibility = View.VISIBLE
            binding.progress.progress = video.playedTime.toInt()
            binding.progress.max = video.duration
            binding.tvDuration.text = (video.duration / 1000).formatShortTime()
        }

    }


}