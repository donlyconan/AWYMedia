package com.utc.donlyconan.media.viewmodels

import android.app.Application

class FavoriteVideoViewModel(app: Application) : ListVideoViewModel(app) {
    var lstVideos = listVideoRepo.getAllFavoriteVideo()
}