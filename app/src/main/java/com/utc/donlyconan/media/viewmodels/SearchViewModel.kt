package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG

class SearchViewModel(app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()

    fun searchAllVideos(keyword: String): LiveData<List<Video>> {
        Log.d(TAG, "searchAllVideos() called with: keyword = $keyword")
        return lstVideoRepo.findAllVideos(keyword)
    }
}