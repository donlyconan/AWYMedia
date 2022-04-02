package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    val listVideoDao by lazy { (app as AwyMediaApplication).listVideoDao }
    val repository by lazy { VideoRepositoryImpl(listVideoDao, app.contentResolver) }
}