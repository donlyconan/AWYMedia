package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.toShortTime
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.OnItemLongClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.io.path.Path


class VideoAdapter(var context: Context, val mode: Int = MODE_NORMAL) :
    PagingDataAdapter<Video, VideoAdapter.VideoHolder>(VideoComparator), OnItemClickListener {

    var inflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null
    var selectedPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater)
        val holder = VideoHolder(binding)
        holder.onItemLongClickListener = onItemLongClickListener
        holder.onItemClickListener = onItemClickListener
        return holder
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item: Video = getVideo(position)
        holder.bind(item, position == itemCount - 1, mode)
    }

    fun getVideo(position: Int) = getItem(position)!!

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }


    class VideoHolder(val binding: ItemVideoSingleModeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
        var onItemClickListener: OnItemClickListener? = null
        var onItemLongClickListener: OnItemLongClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.imgMenuMore.setOnClickListener(this)
            binding.rootLayout.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            onItemLongClickListener?.onItemLongClick(v, adapterPosition)
            return true
        }

        fun bind(video: Video, isLastItem: Boolean, mode: Int) {
            Log.d(TAG, "bind() called with: video = $video, " +
                        "isLastItem = $isLastItem, mode = $mode")
            binding.tvTitle.text = video.title
            binding.tvDate.text = DateFormat.getDateInstance().format(video.updatedAt)
            binding.tvSize.text = video.size.convertToStorageData()
            binding.tvDuration.text = (video.duration / 1000).toShortTime()
            Glide.with(itemView.context)
                .load(video.path)
                .into(binding.imgThumbnail)

            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            }
            when (mode) {
                MODE_RECENT -> {
                    binding.progress.apply {
                        visibility = View.VISIBLE
                        max = video.duration
                        progress = video.playedTime.toInt()
                    }
                }
                MODE_TRASH -> {
                    binding.imgMenuMore.let { v ->
                        val lp = v.layoutParams
                        lp.width = 0
                        v.layoutParams = lp
                    }
                }
            }
        }
    }

    companion object {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
        const val MODE_NORMAL = 1
        const val MODE_RECENT = 2
        const val MODE_TRASH = 3
    }
}