package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.convertToStorageData
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.databinding.ItemGroupNameBinding
import com.utc.donlyconan.media.databinding.ItemTrashBinding
import java.text.DateFormat


class RecycleBinAdapter(var context: Context, trashes: ArrayList<Any>) :
    BaseAdapter<Any>(Trash.diffUtil, trashes) {

    companion object {
        const val TYPE_GROUP_NAME = 1
        const val TYPE_TRASH_ITEM = 2
    }

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val item = getData()[position]
        return if(item is String) {
             TYPE_GROUP_NAME
        } else {
            TYPE_TRASH_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        return if(viewType == TYPE_GROUP_NAME) {
            GroupHolder(ItemGroupNameBinding.inflate(inflater, parent, false))
        } else {
            TrashHolder(ItemTrashBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item: Any = getItem(position)
        if(holder is GroupHolder && item is String) {
            holder.bind(item)
            holder.onItemClickListener = null
            holder.onItemLongClickListener = null
        }
        if(holder is TrashHolder && item is Trash) {
            holder.bind(item, position == getData().size - 1)
        }
    }

    class TrashHolder(val binding: ItemTrashBinding) : LocalHolder(binding) {
        init {
            binding.rootLayout.setOnClickListener(this)
            binding.imgMenuMore.setOnClickListener(this)
            binding.rootLayout.setOnLongClickListener(this)
        }

        fun bind(trash: Trash, isLastItem: Boolean) {
            Log.d(TAG, "bind() called with: trash = $trash, isLastItem = $isLastItem")
            binding.tvTitle.text = trash.title
            binding.tvDate.text = DateFormat.getDateInstance().format(trash.updatedAt * 1000)
            binding.tvSize.text = trash.size.convertToStorageData()

            Glide.with(itemView.context)
                .load(trash.videoUri)
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

    class GroupHolder(var binding: ItemGroupNameBinding) : LocalHolder(binding) {

        fun bind(name: String) {
            binding.tvGroupName.text = name
        }

    }

}