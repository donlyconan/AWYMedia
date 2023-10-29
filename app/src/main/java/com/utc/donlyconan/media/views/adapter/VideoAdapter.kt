package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.media.ThumbnailUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.formatToTime
import com.utc.donlyconan.media.app.utils.formatShortTime
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemGroupNameBinding
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding


class VideoAdapter(
    var context: Context,
    data: List<Video>,
    var showProgress: Boolean = false,
    var showOptionMenu: Boolean = true
) : BaseAdapter<Any>(Video.diffUtil, data) {

    companion object {
        const val TYPE_GROUP = 1
        const val TYPE_VIDEO = 2
    }

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item is String) {
            TYPE_GROUP
        } else {
            TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        return if(viewType == TYPE_GROUP) {
            val binding: ItemGroupNameBinding = ItemGroupNameBinding.inflate(inflater, parent, false)
            GroupHolder(binding)
        } else {
            val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater, parent, false)
            VideoHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if(item is Video && holder is VideoHolder) {
            holder.bind(item, showProgress, showOptionMenu)
            holder.setLastItem(position == getData().size - 1)
        }
        if(item is String && holder is GroupHolder) {
            holder.bind(item)
            holder.onItemClickListener = null
            holder.onItemLongClickListener = null
        }
    }

    fun getVideo(position: Int): Video = getData()[position] as Video


    class VideoHolder(val binding: ItemVideoSingleModeBinding) :
        LocalHolder(binding), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.imgMenuMore.setOnClickListener(this)
            binding.rootLayout.setOnLongClickListener(this)
        }

        fun bind(video: Video, showProgress: Boolean, showOptionMenu: Boolean) {
            binding.tvTitle.text = video.title
            binding.tvDate.text = video.createdAt.formatToTime()
            binding.tvSize.text = video.size.convertToStorageData()
            binding.tvDuration.text = (video.duration / 1000).formatShortTime()
            if(!showOptionMenu) {
                with(binding.imgMenuMore) {
                    layoutParams.width = 0
                    requestLayout()
                }
            }

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .fallback(R.drawable.ic_baseline_error_24)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(video.videoUri.toUri())
                .placeholder(R.drawable.im_loading)
                .error(R.drawable.img_error)
                .fitCenter()
                .into(binding.imgThumbnail)

            if (showProgress) {
                binding.progress.apply {
                    visibility = View.VISIBLE
                    max = video.duration
                    progress = video.playedTime.toInt()
                }
            }
        }

        fun setLastItem(isLastItem: Boolean) {
            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_height).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            } else if (binding.container.paddingBottom != 0) {
                binding.container.apply {
                    setPadding(paddingLeft, paddingTop, paddingRight, 0)
                }
            }
        }
    }

    class GroupHolder(var binding: ItemGroupNameBinding) : LocalHolder(binding) {

        fun bind(name: String) {
            binding.tvGroupName.text = name
        }

    }

}