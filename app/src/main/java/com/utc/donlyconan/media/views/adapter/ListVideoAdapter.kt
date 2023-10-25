package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.toShortTime
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding
import com.utc.donlyconan.media.extension.widgets.TAG
import java.text.DateFormat


class ListVideoAdapter(var context: Context, var videos: List<Video>) :
    RecyclerView.Adapter<ListVideoAdapter.VideoHolder>(), OnItemClickListener {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item: Video = videos[position]
        holder.onItemClickListener = onItemClickListener
        holder.bind(item, position == itemCount - 1)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        onItemClickListener?.onItemClick(v, position)
    }

    fun submit(videos: List<Video>) {
        Log.d(TAG, "submit() called with: videos = $videos")
        this.videos = videos
        notifyDataSetChanged()
    }


    class VideoHolder(val binding: ItemVideoSingleModeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var onItemClickListener: OnItemClickListener? = null
        var onItemLongClickListener: OnItemLongClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.imgMenuMore.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(video: Video, isLastItem: Boolean) {
            Log.d(TAG, "bind() called with: video = $video, isLastItem = $isLastItem")
            binding.tvTitle.text = video.title
            binding.tvDate.text = DateFormat.getDateInstance().format(video.updatedAt)
            binding.tvSize.text = video.size.convertToStorageData()
            binding.tvDuration.text = (video.duration / 1000).toShortTime()
            Glide.with(itemView.context)
                .load(video.videoUri)
                .into(binding.imgThumbnail)
            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            }
        }
    }

}