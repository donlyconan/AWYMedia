package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.ItemPlaylistBinding
import com.utc.donlyconan.media.extension.widgets.TAG

class PlaylistAdapter(var context: Context,
                      var playlists: ArrayList<Playlist>,
                      private val repository: PlaylistRepository) :
    ListAdapter<Playlist, PlaylistAdapter.VideoHolder>(Playlist.diffUtil), OnItemClickListener {

    var onItemLongClickListener: OnItemLongClickListener? = null
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var selectedPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding: ItemPlaylistBinding = ItemPlaylistBinding.inflate(inflater)
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val item = playlists[position]
        item.itemSize = repository.countVideos(item.playlistId!!)
        holder.onItemLongClickListener = onItemLongClickListener
        holder.bind(item, repository.getFirstVideo(item.playlistId!!), this, position == itemCount - 1)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }

    fun submit(playlists: List<Playlist>) {
        Log.d(TAG, "submit() called with: playlists = ${playlists.size}")
        this.playlists = ArrayList(playlists)
        notifyDataSetChanged()
    }


    class VideoHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
        var onItemClickListener: OnItemClickListener? = null
        var onItemLongClickListener: OnItemLongClickListener? = null

        init {
            binding.rootLayout.setOnClickListener(this)
            binding.rootLayout.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View): Boolean {
            onItemLongClickListener?.onItemLongClick(v, adapterPosition)
            return true
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(playlist: Playlist, video: Video?, listener: OnItemClickListener?, isLastItem: Boolean) {
            Log.d(
                TAG, "bind() called with: video = $playlist, listener = $listener, " +
                        "isLastItem = $isLastItem")
            binding.tvTitle.text = playlist.title
            onItemClickListener = listener
            binding.tvNumber.text = "${playlist.itemSize} videos"

            Glide.with(itemView.context)
                .load(video?.path)
                .error(R.drawable.outline_playlist_play_24)
                .circleCrop()
                .into(binding.thumbnailCard)

            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            } else if (binding.container.paddingBottom != 0) {
                binding.container.apply {
                    setPadding(paddingLeft, paddingTop, paddingRight, 0)
                }
            }
        }
    }
}