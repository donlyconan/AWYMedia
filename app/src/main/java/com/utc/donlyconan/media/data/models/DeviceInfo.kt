package com.utc.donlyconan.media.data.models

import android.net.wifi.p2p.WifiP2pDevice
import androidx.recyclerview.widget.DiffUtil
import com.utc.donlyconan.media.views.adapter.Selectable

class DeviceInfo(source: WifiP2pDevice): WifiP2pDevice(source), Selectable {

    private var isChecked: Boolean = false

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                if(oldItem is DeviceInfo && newItem is DeviceInfo) {
                    oldItem.deviceAddress == newItem.deviceAddress
                }
                return false
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun setSelected(isSelected: Boolean) {
        this.isChecked = isSelected
    }

    override fun isSelected(): Boolean {
        return isChecked
    }

}