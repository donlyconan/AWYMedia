package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.flow.Flow

class SearchViewModel(app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = awyApp.lstVideoRepo

    fun searchAllVideos(keyword: String): Flow<PagingData<Video>> {
        Log.d(TAG, "searchAllVideos() called with: keyword = $keyword")
        return lstVideoRepo.findAllVideos(keyword).cachedIn(viewModelScope)
    }
}