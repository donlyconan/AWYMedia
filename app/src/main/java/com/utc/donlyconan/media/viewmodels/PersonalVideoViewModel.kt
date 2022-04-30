package com.utc.donlyconan.media.viewmodels

import android.app.Application

class PersonalVideoViewModel(val app: Application) : ListVideoViewModel(app) {
    val lstVideos = listVideoRepo.getAllVideos()
}