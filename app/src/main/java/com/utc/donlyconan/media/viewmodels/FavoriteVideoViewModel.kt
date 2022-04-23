package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn

class FavoriteVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    var lstVideoRepo = awyApp.applicationComponent().getListVideoRepo()
    var videoList = lstVideoRepo.getAllFavoriteVideo().cachedIn(viewModelScope)
}