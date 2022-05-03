package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.TrashFragment.Companion.TAG
import kotlinx.coroutines.launch

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    private val trashRepo = myApp.applicationComponent().getTrashRepo()
    private val videoRepo = myApp.applicationComponent().getVideoRepo()
    private val playlistRepo = myApp.applicationComponent().getPlaylistRepo()
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
        playlistRepo.removeVideoFromPlaylist(trash.videoId)
    }

    fun clearAll(trashes: List<Trash>) {
        Log.d(TAG, "clearAll() called size=${trashes.size}")
        viewModelScope.launch {
            trashRepo.removeAll()
            trashes.forEach { e ->
                playlistRepo.removeVideoFromPlaylist(e.videoId)
            }
            Log.d(TAG, "clearAll: Done!")
        }
    }
}