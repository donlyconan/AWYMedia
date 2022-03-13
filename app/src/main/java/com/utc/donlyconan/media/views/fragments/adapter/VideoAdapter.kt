package com.utc.donlyconan.media.views.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.toShortTime
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding
import com.utc.donlyconan.media.widget.viewextension.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.*

class VideoAdapter(var context: Context, videosList: ArrayList<Video>) :
    RecyclerView.Adapter<VideoAdapter.VideoHolder>() {
    var videosList: ArrayList<Video> = ArrayList<Video>()
    var inflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null

    init {
        this.videosList = videosList
    }

    override fun getItemCount(): Int = videosList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item: Video = videosList[position]
        holder.bind(item, onItemClickListener)
    }

    class VideoHolder(val binding: ItemVideoSingleModeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var onItemClickListener: OnItemClickListener? = null

        init {
            binding.imgThumbnail.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(video: Video, listener: OnItemClickListener?) {
            binding.tvTitle.setText(video.title)
            Glide.with(itemView.context)
                .load(video.data)
                .into(binding.imgThumbnail)
            binding.tvDate.text = simpleDateFormat.format(video.date)
            binding.tvSize.text = video.size.convertToStorageData()
            binding.tvDuration.text = video.duration.toShortTime()
            onItemClickListener = listener
        }
    }


    companion object {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
    }
}