package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.TrashFragment.Companion.TAG

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    private val trashRepo = myApp.applicationComponent().getTrashRepo()
    private val videoRepo = myApp.applicationComponent().getVideoRepo()
    val videoList = trashRepo.getTrashes()

    fun restore(trash: Trash) {
        Log.d(TAG, "restore() called with: trash = $trash")
        val video = trash.toVideo()
        videoRepo.insert(video)
        trashRepo.delete(trash)
    }

    fun delete(trash: Trash) {
        Log.d(TAG, "delete() called with: trash = $trash")
        trashRepo.delete(trash)
    }
}