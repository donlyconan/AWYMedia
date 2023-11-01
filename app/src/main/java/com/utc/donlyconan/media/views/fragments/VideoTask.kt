package com.utc.donlyconan.media.views.fragments

import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.data.models.Video

class VideoTask(val videos: List<Video>, val time: Long = now(), val succeed: Runnable? = null, val error: Runnable? = null) {
    fun getPeriodOfCreatedTime(): Long {
        return now() - time
    }

    companion object {
        fun from(video: Video, time: Long = now(),  succeed: Runnable? = null, error: Runnable? = null): VideoTask {
            return VideoTask(listOf(video), time, succeed, error)
        }
    }
}