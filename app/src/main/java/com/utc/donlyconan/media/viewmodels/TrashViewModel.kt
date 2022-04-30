package com.utc.donlyconan.media.viewmodels

import android.app.Application

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    val lstVideoRepo = myApp.applicationComponent().getListVideoRepo()
    val videoList = lstVideoRepo.getListInTrash()
}