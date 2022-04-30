package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.fragments.RecentFragment

class RecentVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    val videoRepo = myApp.applicationComponent().getVideoRepo()
    val videoList = lstVideoRepo.getAllPlayingVideos()

    fun update(video: Video) {
        Log.d(RecentFragment.TAG, "update() called with: video = $video")
        videoRepo.update(video)
    }
}