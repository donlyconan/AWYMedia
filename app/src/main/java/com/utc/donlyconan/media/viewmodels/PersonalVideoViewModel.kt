package com.utc.donlyconan.media.viewmodels

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.components.getAllVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonalVideoViewModel(val videoRepository: VideoRepository, val contentResolver: ContentResolver) : ViewModel() {

    val videosLd = videoRepository.getAllVideosBySecuring(isSecured = false)

    fun importVideos() = viewModelScope.launch(Dispatchers.IO) {
        val videos = videoRepository.loadAllVideos()
        videoRepository.insert(*videos.toTypedArray())
    }
}