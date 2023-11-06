package com.utc.donlyconan.media.viewmodels

import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.data.repo.VideoRepository

class PersonalVideoViewModel(val videoRepository: VideoRepository, val trashRepository: TrashRepository) : ViewModel() {
    val videosLd = videoRepository.getAllVideosBySecuring(isSecured = false)
    val numberOfTrash = trashRepository.count()
}