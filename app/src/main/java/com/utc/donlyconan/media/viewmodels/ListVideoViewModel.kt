package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class ListVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    private val _selectedVideo = MutableLiveData<Video>()
    val listVideoRepo = myApp.applicationComponent().getListVideoRepository()
    val videoRepo = myApp.applicationComponent().getVideoRepository()

    fun update(video: Video) {
        videoRepo.update(video)
    }

    fun insert(video: Video) {
        videoRepo.insert(video)
    }

}