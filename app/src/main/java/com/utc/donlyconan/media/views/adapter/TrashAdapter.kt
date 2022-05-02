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
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.databinding.ItemTrashBinding
import com.utc.donlyconan.media.databinding.ItemVideoSingleModeBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.OnItemLongClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import java.text.DateFormat
import java.text.SimpleDateFormat


class TrashAdapter(var context: Context, var trashes: ArrayList<Trash>) :
    RecyclerView.Adapter<TrashAdapter.TrashHolder>(), OnItemClickListener {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null
    var selectedPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val binding: ItemTrashBinding = ItemTrashBinding.inflate(inflater)
        return TrashHolder(binding)
    }

    override fun onBindViewHolder(holder: TrashHolder, position: Int) {
        val item: Trash = trashes[position]
        holder.onItemLongClickListener = onItemLongClickListener
        holder.onItemClickListener = onItemClickListener
        holder.bind(item, position == trashes.size - 1)
    }

    override fun getItemCount(): Int {
        return trashes.size
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        selectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }


    fun submit(trashes: List<Trash>) {
        Log.d(TAG, "submit() called with: trashes = ${trashes.size}")
        this.trashes = ArrayList(trashes)
        notifyDataSetChanged()
    }


    class TrashHolder(val binding: ItemTrashBinding) :
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

        fun bind(trash: Trash, isLastItem: Boolean) {
            Log.d(TAG, "bind() called with: trash = $trash, isLastItem = $isLastItem")
            binding.tvTitle.text = trash.title
            binding.tvDate.text = simpleDateFormat.format(trash.updatedAt)
            binding.tvSize.text = trash.size.convertToStorageData()

            Glide.with(itemView.context)
                .load(trash.path)
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

    companion object {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
    }

}