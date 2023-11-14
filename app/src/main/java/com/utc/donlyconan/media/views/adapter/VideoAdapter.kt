package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.app.utils.formatShortTime
import com.utc.donlyconan.media.app.utils.formatToTime
import com.utc.donlyconan.media.app.utils.setVideoImage
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ItemGroupNameBinding
import com.utc.donlyconan.media.databinding.ItemPlaylistBinding
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding


class VideoAdapter(
    var context: Context,
    data: List<Video>,
    var showProgress: Boolean = false,
    var showOptionMenu: Boolean = true,
    val dragMode: Boolean = false,
) : BaseAdapter<Any>(Video.diffUtil, data) {

    companion object {
        const val TYPE_GROUP = 1
        const val TYPE_VIDEO = 2
        const val TYPE_PLAYLIST = 3
        const val TYPE_TRASH_ITEM = 3
    }

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item is String) {
            TYPE_GROUP
        } else if(item is Video) {
            TYPE_VIDEO
        } else if (item is Playlist) {
            TYPE_PLAYLIST
        } else {
            TYPE_TRASH_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalHolder {
        return when(viewType) {
            TYPE_GROUP -> {
                val binding: ItemGroupNameBinding = ItemGroupNameBinding.inflate(inflater, parent, false)
                GroupHolder(binding)
            }
            TYPE_VIDEO -> {
                val binding: ItemVideoSingleModeBinding = ItemVideoSingleModeBinding.inflate(inflater, parent, false)
                VideoHolder(binding)
            }
            else -> {
                val binding: ItemPlaylistBinding = ItemPlaylistBinding.inflate(inflater, parent, false)
                PlaylistAdapter.PlaylistHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if(item is Video && holder is VideoHolder) {
            holder.bind(item, showProgress, showOptionMenu, dragMode)
            holder.setLastItem(position == getData().size - 1)
        }
        if(item is String && holder is GroupHolder) {
            holder.bind(item)
            holder.onItemClickListener = null
            holder.onItemLongClickListener = null
        }
        if(item is Playlist && holder is PlaylistAdapter.PlaylistHolder) {
            holder.bind(item, item.firstVideo, this, position == itemCount - 1, showOptionMenu)
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

        fun bind(video: Video, showProgress: Boolean, showOptionMenu: Boolean, dragMode: Boolean = false) {
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

            binding.imgThumbnail.setVideoImage(video.videoUri)
            if (showProgress) {
                binding.progress.apply {
                    visibility = View.VISIBLE
                    max = video.duration
                    progress = video.playedTime.toInt()
                }
            } else {
                binding.progress.visibility = View.GONE
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

        fun setProgress(progress: Long, total: Long) {
            with(binding.progress) {
                if(visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                }
                max = total.toInt()
                setProgress(progress.toInt(), true)
                if(progress == total) {
                    visibility = View.GONE
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