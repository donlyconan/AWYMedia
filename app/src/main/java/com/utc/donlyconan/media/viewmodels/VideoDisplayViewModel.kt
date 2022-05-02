package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.ListVideoDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.widgets.TAG
import javax.inject.Inject

class VideoDisplayViewModel(app: Application) : BaseAndroidViewModel(app) {
    @Inject lateinit var videoRepo: VideoRepository
    val video: MutableLiveData<Video> = MutableLiveData<Video>()
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    var playWhenReady = true
    var isFinished = false
    var isContinue = false
    var isInitial = true

    init {
        myApp.applicationComponent().inject(this)
    }

    fun getNext(): Video {
        return videoRepo.getNext(video.value!!.videoId)
    }

    fun getPrevious(): Video {
        return videoRepo.getPrevious(video.value!!.videoId)
    }

    fun next() {
        video.value = getNext()
    }

    fun previous() {
        video.value = getPrevious()
    }

    fun endVideo() {
        Log.d(TAG, "endVideo() called")
        isFinished = true
        video.value?.let { video ->
            video.playedTime = 0
            videoRepo.update(video)
        }
    }

    fun save() {
        Log.d(TAG, "save() called + ${video.value}")
        val video = video.value
        if(video != null && !isFinished) {
            videoRepo.update(video)
        }
    }

    override fun toString(): String {
        return "VideoDisplayViewModel(video=${video.value}, playWhenReady=$playWhenReady)"
    }
}