package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.key
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.utc.donlyconan.media.app.utils.atStartOfDay
import com.utc.donlyconan.media.app.utils.toShortTime
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

class PersonalVideoViewModel(val app: Application) : ListVideoViewModel(app) {

    val lstVideos = listVideoRepo.getAllVideos()

    fun sortedByTime(videos: List<Video>): List<Any> {
        val maps = videos.sortedByDescending { it.createdAt }
            .groupBy { it.createdAt.atStartOfDay() }
        val result = ArrayList<Any>(maps.size * 2)
        for (item in maps) {
            result.add(item.key.toShortTime())
            result.addAll(item.value)
        }
        maps.forEach { t, u ->
            Log.d("PersonalVideoViewModel", "sortedByTime: " + t)
        }
        Log.d("PersonalVideoViewModel", "sortedByTime: " + result)
        return result
    }

}

fun main() {
    println(1694002528L.atStartOfDay().toShortTime())
}