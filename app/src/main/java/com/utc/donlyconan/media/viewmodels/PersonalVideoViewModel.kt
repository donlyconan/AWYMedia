package com.utc.donlyconan.media.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.repo.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonalVideoViewModel(val videoRepository: VideoRepository, val contentResolver: ContentResolver) : ViewModel() {

    val videosLd = videoRepository.getAllVideosBySecuring(isSecured = false)

    fun sync() = viewModelScope.launch(Dispatchers.IO) {
        videoRepository.sync()
    }
}