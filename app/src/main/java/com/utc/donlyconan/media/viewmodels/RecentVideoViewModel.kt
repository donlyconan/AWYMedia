package com.utc.donlyconan.media.viewmodels

import android.app.Application

class RecentVideoViewModel(app: Application) : ListVideoViewModel(app) {
    val lstVideos = listVideoRepo.getAllPlayingVideos()
}