package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.cachedIn

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = awyApp.applicationComponent().getListVideoRepo()
    val videoList by lazy { lstVideoRepo.getListInTrash().cachedIn(viewModelScope) }
}