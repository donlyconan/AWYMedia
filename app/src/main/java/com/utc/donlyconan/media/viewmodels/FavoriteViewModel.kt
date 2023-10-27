package com.utc.donlyconan.media.viewmodels

import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.data.repo.VideoRepository

class FavoriteViewModel(videoRepo: VideoRepository) : ViewModel() {
    var lstVideos = videoRepo.getAllFavorites(isFavorite = true)
}