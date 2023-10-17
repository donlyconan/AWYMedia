package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import com.utc.donlyconan.media.app.utils.atStartOfDay
import com.utc.donlyconan.media.app.utils.toShortTime
import com.utc.donlyconan.media.data.models.Video

class RecentVideoViewModel(app: Application) : ListVideoViewModel(app) {

    val lstVideos = listVideoRepo.getAllPlayingVideos()

    fun sortedByTime(videos: List<Video>): List<Any> {
        val maps = videos.sortedByDescending { it.updatedAt }
            .groupBy { it.updatedAt.atStartOfDay() }
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