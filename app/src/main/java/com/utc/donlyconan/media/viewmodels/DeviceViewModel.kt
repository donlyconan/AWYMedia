package com.utc.donlyconan.media.viewmodels

import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.data.models.DeviceInfo

class DeviceViewModel() : ViewModel()  {
    val devicesMdl = MutableLiveData<List<DeviceInfo>>()

    fun submit(list: List<DeviceInfo>) {
        val oldList = devicesMdl.value
        if(oldList != list) {
            list.forEach { info1 ->
                val selected = oldList?.find { info2 ->  info1.deviceAddress == info2.deviceAddress }?.isSelected() ?: false
                info1.setSelected(selected)
            }
            devicesMdl.value = list
        }
    }

    fun update(device: WifiP2pDevice) {
        val oldList = devicesMdl.value?.toMutableList()
        oldList?.filter { it -> it.deviceAddress == device.deviceAddress }
            ?.forEachIndexed() { index, _ ->
                oldList[index] = DeviceInfo(device)
            }
        devicesMdl.value = oldList?.toList()
    }

}