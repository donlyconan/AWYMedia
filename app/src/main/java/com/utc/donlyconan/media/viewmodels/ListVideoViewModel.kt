package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.util.*

open class ListVideoViewModel(app: Application) : BaseAndroidViewModel(app) {
    private val _selectedVideo = MutableLiveData<Video>()
    val listVideoRepo = myApp.applicationComponent().getListVideoRepo()
    val videoRepo = myApp.applicationComponent().getVideoRepo()
    val selectedVideo: LiveData<Video> = _selectedVideo

    protected val job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    fun hasVideo(path: String): Boolean {
        return videoRepo.countPath(path) != 0
    }

    fun update(video: Video) {
        videoRepo.update(video)
    }

    fun insert(video: Video) {
        videoRepo.insert(video)
    }

    fun moveToTrash(video: Video) {

    }

    fun insertVideoIfNeed() = viewModelScope.launch {
        if (videoRepo.count() != 0) {
            Log.d(TAG, "insertDataIntoDbIfNeed: Database had been loaded!")
            return@launch
        }
        val videoList = listVideoRepo.loadAllVideos()
        Log.d(TAG, "insertDataIntoDb: loaded size = " + videoList.size)
        videoRepo.insert(*videoList.toTypedArray())
    }

}