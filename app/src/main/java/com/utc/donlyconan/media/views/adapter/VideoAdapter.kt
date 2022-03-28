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
import com.utc.donlyconan.media.views.fragments.PersonalVideoFragment.Companion.TAG
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import java.text.DateFormat
import java.text.SimpleDateFormat

class VideoAdapter(var context: Context, var videosList: List<Video>, val mode: Int = MODE_NORMAL):
    RecyclerView.Adapter<VideoAdapter.VideoHolder>(), OnItemClickListener {
    var inflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var selectedPostion: Int = -1
        private set

    override fun getItemCount(): Int = videosList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item: Video = videosList[position]
        holder.bind(item, this, position == videosList.size - 1, mode)
    }

    fun submit(videoList: List<Video>) {
        Log.d(TAG, "submit() called with: it = ${videoList?.size}")
        this.videosList = videoList
        notifyDataSetChanged()
        selectedPostion = -1
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPostion = position
        onItemClickListener?.onItemClick(v, position)
    }


    class VideoHolder(val binding: ItemVideoSingleModeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var onItemClickListener: OnItemClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.imgMenuMore.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(video: Video, listener: OnItemClickListener?, isLastItem: Boolean, mode: Int) {
            Log.d(TAG, "bind() called with: video = $video, listener = $listener, " +
                    "isLastItem = $isLastItem, mode = $mode")
            binding.tvTitle.setText(video.title)
            Glide.with(itemView.context)
                .load(video.data)
                .into(binding.imgThumbnail)
            binding.tvDate.text = DateFormat.getDateInstance().format(video.updatedAt)
            binding.tvSize.text = video.size.convertToStorageData()
            binding.tvDuration.text = (video.duration/1000).toShortTime()
            onItemClickListener = listener
            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            }
            if(mode == MODE_RECENT) {
                binding.progress.apply {
                    visibility = View.VISIBLE
                    max = video.duration
                    progress = video.playedTime.toInt()
                }
            }
        }
    }

    companion object {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
        const val MODE_NORMAL = 1
        const val MODE_RECENT = 2
        const val MODE_SHARED = 3
    }
}