package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl
import com.utc.donlyconan.media.views.fragments.PersonalVideoFragment.Companion.TAG
import kotlinx.coroutines.launch

class PersonalVideoViewModel(app: Application) : AndroidViewModel(app) {
    val repository = VideoRepositoryImpl((app as AwyMediaApplication).listVideoDao, app.contentResolver)
    val videoList: LiveData<List<Video>> = repository.getAllVideos()
    var isLoaded: Boolean = false
    var selectedVideo: Video ?= null

    fun insertVideosIntoDbIfNeed() {
        Log.d(TAG, "insertVideosIntoDbIfNeed() called")
        viewModelScope.launch {
            val videoList = repository.loadVideos()
            if(!isLoaded && !videoList.isEmpty()) {
                repository.insertVideo(*videoList.toTypedArray())
                Log.d(TAG, "insertVideosIfNeed() called inserted=" + videoList.size)
                isLoaded = true
            }
        }
    }
}