package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.db.AwyMediaDatabase
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl
import com.utc.donlyconan.media.views.VideoDisplayActivity.Companion.TAG
import kotlinx.coroutines.launch

class VideoDisplayViewModel(app: Application) : AndroidViewModel(app) {
    val video: MutableLiveData<Video> = MutableLiveData<Video>()
    val playWhenReady = true
    val dao = AwyMediaDatabase.getInstance(app).getListVideoDao()
    val repository = VideoRepositoryImpl(dao, app.contentResolver)


    override fun onCleared() {
        Log.d(TAG, "onCleared() called video=" + video.value)
        video.value?.let {
            repository.updateVideo(it)
            Log.d(TAG, "clear: video updated")
        }
        super.onCleared()
    }

    override fun toString(): String {
        return "VideoDisplayViewModel(video=${video.value}, playWhenReady=$playWhenReady)"
    }
}