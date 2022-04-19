package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.utc.donlyconan.media.app.AwyMediaApplication

class RecentVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = awyApp.lstVideoRepo
    val videoList = lstVideoRepo.getAllPlayingVideos().cachedIn(viewModelScope)
}