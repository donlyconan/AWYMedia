package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG

class VideoDisplayViewModel(app: Application) : BaseAndroidViewModel(app) {
    val video: MutableLiveData<Video> = MutableLiveData<Video>()
    val playWhenReady = true
    val lstVideoRepo = awyApp.lstVideoRepo
    val videoRepo = awyApp.videoRepo
    val videoList = lstVideoRepo.getAllVideos(Settings.SORT_BY_NAME).asLiveData()

    fun saveVideoIfNeed() {
        Log.d(TAG, "saveVideoIfNeed() called video=${video.value}")
        video.value?.let { video ->
            videoRepo.update(video)
            Log.d(TAG, "saveVideoIfNeed: video updated")
        }
    }

    override fun toString(): String {
        return "VideoDisplayViewModel(video=${video.value}, playWhenReady=$playWhenReady)"
    }
}