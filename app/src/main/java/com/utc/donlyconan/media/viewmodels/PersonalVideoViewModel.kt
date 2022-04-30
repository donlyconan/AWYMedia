package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.launch

class PersonalVideoViewModel(val app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    val videoRepo = myApp.applicationComponent().getVideoRepo()
    var selectedVideo: Video? = null

    fun insertDataIntoDbIfNeed() {
        Log.d(TAG, "insertDataIntoDb() called")
        viewModelScope.launch {
            if(videoRepo.count() != 0) {
                Log.d(TAG, "insertDataIntoDbIfNeed: Database had been loaded!")
                return@launch
            }
            val videoList = lstVideoRepo.loadAllVideos()
            Log.d(TAG, "insertDataIntoDb: loaded size = " + videoList.size)
            videoRepo.insert(*videoList.toTypedArray())
        }
    }

    fun sortBy(sortId: Int): LiveData<List<Video>> {
        Log.d(TAG, "sortVideoList() called with: sortId = $sortId")
        return lstVideoRepo.getAllVideos()
    }
}