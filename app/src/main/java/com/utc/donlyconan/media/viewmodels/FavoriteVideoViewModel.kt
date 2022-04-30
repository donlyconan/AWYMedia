package com.utc.donlyconan.media.viewmodels

import android.app.Application

class FavoriteVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    var lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    var videoList = lstVideoRepo.getAllFavoriteVideo()
}