package com.utc.donlyconan.media.views.adapter

import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


abstract class BaseAdapter<T>(differ: ItemCallback<T>, private var data: List<T>):
    ListAdapter<T, BaseAdapter.LocalHolder>(differ), OnItemClickListener {

    companion object {
        val TAG: String = BaseAdapter.javaClass.simpleName
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var _lastSelectedPosition: Int = -1
    val lastSelectedPosition get() = _lastSelectedPosition

    override fun onBindViewHolder(holder: LocalHolder, position: Int) {
        holder.onItemClickListener = this
        holder.onItemLongClickListener = onItemLongClickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }


    public override fun getItem(position: Int): T{
        return data[position]
    }

    fun getSelectedItems(): List<T> = data.filter { (it as? Selectable)?.isSelected() == true }

    fun countSelectedItem(): Int = data.count { (it as? Selectable)?.isSelected() == true }

    override fun onItemClick(v: View, position: Int) {
        _lastSelectedPosition = position
        onItemClickListener?.onItemClick(v, position)
    }

    fun getData(): List<T> = data

    /**
     * Update data and reset view again if need
     */
    fun submit(data: List<T>, reset: Boolean = true) {
        Log.d(TAG, "submit() called with: dataSize = ${this.data.size}")
        this.data = data
        if(reset) {
            notifyDataSetChanged()
        }
    }

    open fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
        notifyDataSetChanged()
    }

    open fun setOnLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
        notifyDataSetChanged()
    }

    abstract class LocalHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root),
        OnClickListener, OnLongClickListener {

        var onItemClickListener: OnItemClickListener? = null
        var onItemLongClickListener: OnItemLongClickListener? = null

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, absoluteAdapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            onItemLongClickListener?.onItemLongClick(v, absoluteAdapterPosition)
            return true
        }

        open fun bind(value: Any) {}

    }

}


interface OnItemClickListener {
    fun onItemClick(v: View, position: Int)
}


interface OnItemLongClickListener {
    fun onItemLongClick(v: View, position: Int)
}

interface Selectable {
    fun setSelected(isSelected: Boolean)
    fun isSelected(): Boolean
}

