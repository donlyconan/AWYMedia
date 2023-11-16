package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.utc.donlyconan.media.data.models.Video

class SearchViewModel(app: Application) : BaseAndroidViewModel(app) {
    val videoRepo = myApp.applicationComponent().getVideoRepository()
    val playlistRepo = myApp.applicationComponent().getPlaylistRepository()

    val _commonData: MutableLiveData<List<Any>> = MutableLiveData()
    val commonData: LiveData<List<Any>> get() = _commonData

    suspend fun searchForVideo(text: String) {
        val data = videoRepo.getAllOnThread().filter {  video: Video ->
            video.title?.lowercase()?.contains(text) == true
        }
        _commonData.postValue(data)
    }

    suspend fun searchForPlaylist(text: String) {
        val data = playlistRepo.getAllOnThread().filter {  playlist ->
            playlist.title.lowercase().contains(text)
        }
        _commonData.postValue(data)
    }

}