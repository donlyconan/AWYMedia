package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.utc.donlyconan.media.data.models.Video

class SharedViewModel(app: Application): BaseAndroidViewModel(app) {

    val playlist = MutableLiveData<List<Video>>()

}