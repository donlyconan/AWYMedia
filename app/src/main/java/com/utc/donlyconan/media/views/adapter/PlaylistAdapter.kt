package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.databinding.ItemPlaylistBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import java.text.SimpleDateFormat


class PlaylistAdapter(var context: Context) :
    PagingDataAdapter<Playlist, PlaylistAdapter.VideoHolder>(PlaylistComparator), OnItemClickListener {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var selectedPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding: ItemPlaylistBinding = ItemPlaylistBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item = getPlaylistItem(position)
        holder.bind(item, this, position == itemCount - 1)
    }

    fun getPlaylistItem(position: Int) = getItem(position)!!

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }


    class VideoHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var onItemClickListener: OnItemClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(playlist: Playlist, listener: OnItemClickListener?, isLastItem: Boolean) {
            Log.d(TAG, "bind() called with: video = $playlist, listener = $listener, " +
                        "isLastItem = $isLastItem")
            binding.tvTitle.text = playlist.title
            onItemClickListener = listener
            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            }
        }
    }

    companion object {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
    }
}