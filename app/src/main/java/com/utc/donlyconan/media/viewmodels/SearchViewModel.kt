package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import java.util.Collections

class SearchViewModel(app: Application) : BaseAndroidViewModel(app) {
    val videoRepo = myApp.applicationComponent().getVideoRepository()
    val playlistRepo = myApp.applicationComponent().getPlaylistRepository()

    val _commonData: MutableLiveData<List<Any>> = MutableLiveData()
    val commonData: LiveData<List<Any>> get() = _commonData

    suspend fun search(keyword: String) {
        val data = mutableListOf<Any>(videoRepo.getAllPublicVideos())
        data.addAll(playlistRepo.getAllOnCurrentThread())
        _commonData.postValue(data)

    }

}