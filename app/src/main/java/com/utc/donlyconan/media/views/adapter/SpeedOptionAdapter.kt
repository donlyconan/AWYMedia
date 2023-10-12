package com.utc.donlyconan.media.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utc.donlyconan.media.databinding.ItemSpeedOptionBinding


class SpeedOptionAdapter(var context: Context, var speedList: List<Float>):
    RecyclerView.Adapter<SpeedOptionAdapter.SpeedOptionHolder>() {
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var onItemClickListener: OnItemClickListener? = null
    var selectedItem: Float = -1f

    override fun getItemCount(): Int = speedList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedOptionHolder {
        val binding: ItemSpeedOptionBinding = ItemSpeedOptionBinding.inflate(inflater)
        return SpeedOptionHolder(binding)
    }

    override fun onBindViewHolder(holder: SpeedOptionHolder, position: Int) {
        val item = speedList[position]
        holder.bind(item, selectedItem.equals(item), onItemClickListener)
    }

    class SpeedOptionHolder(val binding: ItemSpeedOptionBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        private var onItemClickListener: OnItemClickListener? = null

        init {
            binding.tvParam.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }

        fun bind(speed: Float, isSelected: Boolean, listener: OnItemClickListener?) {
            binding.tvParam.text = "${speed}x"
            binding.tvParam.isSelected = isSelected
            onItemClickListener = listener
        }
    }
}