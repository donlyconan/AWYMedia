package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = awyApp.lstVideoRepo
    val videoList by lazy { lstVideoRepo.getListInTrash().cachedIn(viewModelScope) }
}