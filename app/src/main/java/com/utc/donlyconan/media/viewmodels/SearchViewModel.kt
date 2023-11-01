package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import java.util.LinkedList

class SearchViewModel(app: Application) : BaseAndroidViewModel(app) {
    val videoRepo = myApp.applicationComponent().getVideoRepository()
    val playlistRepo = myApp.applicationComponent().getPlaylistRepository()

    val _commonData: MutableLiveData<List<Any>> = MutableLiveData()
    val commonData: LiveData<List<Any>> get() = _commonData


    suspend fun search(keyword: String) {
        val linkedList = LinkedList<Any>()
        flowOf(videoRepo.getAllOnThread(), playlistRepo.getAllOnThread())
            .transform { list ->
                list.forEach { emit(it) }
            }
            .filter {
                if(it is Video) {
                    it.title?.contains(keyword) == true
                } else if(it is Playlist) {
                    it.title.contains(keyword)
                } else {
                    false
                }
            }
            .cancellable()
            .flowOn(Dispatchers.IO)
            .onCompletion {
                Log.d(TAG, "search: end!")
                _commonData.postValue(linkedList)
            }.collect() {
                linkedList += it
                if (linkedList.size % 20 == 0) {
                    _commonData.postValue(linkedList)
                }
            }
    }

}

//fun main() {
//    println("123u8374747234".contains("234"))
////    runBlocking {
////        val f1 = flowOf(1, 2, 3, 4,5)
////        val f2 = flowOf(8, 9, 10, 11)
////        val value = merge(f1, f2).onEach {
////            println(it)
////        }.toList()
////        println(value)
////    }
//}