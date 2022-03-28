package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl
import com.utc.donlyconan.media.views.fragments.PersonalVideoFragment.Companion.TAG
import kotlinx.coroutines.launch

class FavoriteVideoViewModel(app: Application) : AndroidViewModel(app) {
    val repository = VideoRepositoryImpl((app as AwyMediaApplication).listVideoDao, app.contentResolver)
    val videoList: LiveData<List<Video>> = repository.getAllPlayingVideos()
}