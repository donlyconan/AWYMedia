package com.utc.donlyconan.media.views.adapter

import android.content.Context
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
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.databinding.ItemGroupNameBinding
import com.utc.donlyconan.media.databinding.ItemTrashBinding
import java.text.DateFormat
import java.util.Objects


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
            binding.tvDate.text = trash.deletedAt.formatToTime()
            binding.tvSize.text = trash.size.convertToStorageData()
            binding.rdRadio.isChecked = trash.isSelected()

            binding.rdRadio.setOnCheckedChangeListener { buttonView, isChecked ->
                trash.setSelected(isChecked)
            }

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .fallback(R.drawable.ic_baseline_error_24)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(trash.videoUri.toUri())
                .placeholder(R.drawable.im_loading)
                .error(R.drawable.img_error)
                .fitCenter()
                .into(binding.imgThumbnail)

            if (isLastItem) {
                binding.container.apply {
                    val paddingBottom =
                        resources.getDimension(R.dimen.list_video_item_margin_bottom).toInt()
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                }
            }
        }

        override fun onClick(v: View) {
            binding.rdRadio.isChecked = binding.rdRadio.isChecked.not()
        }
    }

    class GroupHolder(var binding: ItemGroupNameBinding) : LocalHolder(binding) {

        fun bind(name: String) {
            binding.tvGroupName.text = name
        }

    }

}