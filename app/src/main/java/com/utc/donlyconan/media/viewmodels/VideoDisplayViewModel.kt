package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.widgets.TAG
import javax.inject.Inject

class VideoDisplayViewModel(app: Application) : BaseAndroidViewModel(app) {
    @Inject lateinit var videoRepo: VideoRepository
    val video: MutableLiveData<Video> = MutableLiveData<Video>()
    var mPlayWhenReady = MutableLiveData(false)
    var mLdSpeed = MutableLiveData(1.0f)
    var mRepeatMode = MutableLiveData(ExoPlayer.REPEAT_MODE_OFF)
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    var playlist: ArrayList<Video> = arrayListOf()
    var isFinished = false
    var isContinue = false
    var isInitialized = true
    var isLooped = false
    var position: Int = 0
    var isResetPosition = false
    var isRestoredState = false

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

    /**
     * Set status finish for playing operation and save video state
     */
    fun finishPlaying() {
        Log.d(TAG, "endVideo() called")
        isFinished = true
        video.value?.let { video ->
            video.playedTime = 0
            videoRepo.update(video)
        }
    }

    fun updatePosition(position: Int) {
        Log.d(TAG, "updatePosition() called with: position = $position")
        this.position = position
        video.value = playlist[position]
    }

    fun save() {
        Log.d(TAG, "save() called + ${video.value}")
        val video = video.value
        if(video != null && !isFinished) {
            videoRepo.update(video)
        }
    }

    /**
     * Check a dialog can be shown or not
     * @return true if can show dialog
     */
    fun canShowDialog(): Boolean {
        return !isContinue && isInitialized && getVideo().playedTime > 0L
    }

    /**
     * Get current video that is playing by exoplayer
     * @return current video
     */
    fun getVideo(): Video {
        return playlist[position]
    }

    override fun toString(): String {
        return "VideoDisplayViewModel(video=${video.value}, playWhenReady=$mPlayWhenReady)"
    }
}