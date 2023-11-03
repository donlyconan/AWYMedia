package com.utc.donlyconan.media.viewmodels

import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.data.repo.VideoRepository

class PersonalVideoViewModel(val videoRepository: VideoRepository) : ViewModel() {
    val videosLd = videoRepository.getAllVideosBySecuring(isSecured = false)
}