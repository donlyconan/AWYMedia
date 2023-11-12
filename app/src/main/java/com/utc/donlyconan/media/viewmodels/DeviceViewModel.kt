package com.utc.donlyconan.media.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.app.localinteraction.Client

class DeviceViewModel() : ViewModel() {
    val devicesMdl = MutableLiveData<List<Client>>()

    fun submit(clients: List<Client>) {
        devicesMdl.postValue(clients)
    }
}