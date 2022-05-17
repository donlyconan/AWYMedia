package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.google.android.exoplayer2.ExoPlayer
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
    var playlist: ArrayList<Video> = arrayListOf()
    var playWhenReady = MutableLiveData(true)
    var speed = MutableLiveData(1.0f)
    var repeatMode = MutableLiveData(ExoPlayer.REPEAT_MODE_OFF)
    var isFinished = false
    var isContinue = false
    var isInitial = true
    var isLooped = false
    var position: Int = 0
    var isResetPosition = false

    init {
        myApp.applicationComponent().inject(this)
    }

    fun hasNext() = position < playlist.size - 1

    fun hasPrev() = position > 0

    private fun getNext(): Video? {
        if(hasNext()) {
            return playlist[++position]
        }
        return null
    }

    private fun getPrevious(): Video? {
        if(hasPrev()) {
            return  playlist[--position]
        }
        return null
    }

    fun next(): Boolean {
        if(hasNext()) {
            video.value = getNext()
            isResetPosition = true
            return true
        }
        return false
    }

    fun previous(): Boolean {
        if(hasPrev()) {
            video.value = getPrevious()
            isResetPosition = true
            return true
        }
        return false
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

    fun canShowDialog(): Boolean {
        return !isContinue && isInitial && currentVideo().playedTime > 0L
    }

    fun currentVideo(): Video {
        return playlist[position]
    }

    override fun toString(): String {
        return "VideoDisplayViewModel(video=${video.value}, playWhenReady=$playWhenReady)"
    }
}