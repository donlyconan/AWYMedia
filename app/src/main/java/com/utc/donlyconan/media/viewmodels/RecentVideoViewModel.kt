package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.util.Log
import com.utc.donlyconan.media.app.utils.atStartOfDay
import com.utc.donlyconan.media.app.utils.formatShortTime
import com.utc.donlyconan.media.data.models.Video

class RecentVideoViewModel(app: Application) : ListVideoViewModel(app) {
    val videosLd = listVideoRepo.getAllPlayingVideos()
}