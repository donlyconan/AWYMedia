package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn

class RecentVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = awyApp.applicationComponent().getListVideoRepo()
    val videoList = lstVideoRepo.getAllPlayingVideos().cachedIn(viewModelScope)
}