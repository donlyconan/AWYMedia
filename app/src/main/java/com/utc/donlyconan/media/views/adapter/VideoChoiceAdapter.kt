package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.setVideoImage
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemVideoChoiceBinding
import com.utc.donlyconan.media.extension.widgets.TAG


class VideoChoiceAdapter(var context: Context, var videos: ArrayList<Video>) :
    RecyclerView.Adapter<VideoChoiceAdapter.VideoHolder>(), OnItemClickListener {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null
    var selectedPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding: ItemVideoChoiceBinding = ItemVideoChoiceBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item: Video = videos[position]
        holder.onItemLongClickListener = onItemLongClickListener
        holder.onItemClickListener = onItemClickListener
        holder.bind(item, position == itemCount - 1)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }

    fun submit(videos: List<Video>) {
        Log.d(TAG, "submmit() called with: videos.size = ${videos.size}")
        this.videos.clear()
        this.videos.addAll(videos)
        notifyDataSetChanged()
    }


    class VideoHolder(val binding: ItemVideoChoiceBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
        var onItemClickListener: OnItemClickListener? = null
        var onItemLongClickListener: OnItemLongClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.rootLayout.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            onItemLongClickListener?.onItemLongClick(v, adapterPosition)
            return true
        }

        fun bind(video: Video, isLastItem: Boolean) {
            Log.d(TAG, "bind() called with: video = $video, isLastItem = $isLastItem")
            binding.tvTitle.text = video.title
            binding.tvSize.text = video.size.convertToStorageData()

            binding.imgThumbnail.setVideoImage(video.videoUri)
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