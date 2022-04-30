package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import java.util.*

class ListVideoViewModel(app: Application) : BaseAndroidViewModel(app) {

    private lateinit var _videoList: LiveData<List<Video>>
    private val _selectedVideo = MutableLiveData<Video>()
    val selectedVideo: LiveData<Video> = _selectedVideo
    val listVideoRepo = myApp.applicationComponent().getListVideoRepo()
    val videoRepo = myApp.applicationComponent().getVideoRepo()

    fun getVideoList(sortId: Int): LiveData<List<Video>> {
        return listVideoRepo.getAllVideos(sortId)
    }

    fun hasVideo(path: String): Boolean {
        return videoRepo.countPath(path) != 0
    }

    fun update(video: Video) {
        videoRepo.update(video)
    }

    fun insert(video: Video) {
        videoRepo.insert(video)
    }

    fun moveToTrash(video: Video) {

    }

}