package com.utc.donlyconan.media.views.fragments.privatefolder

import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.data.dao.VideoDao

class PrivateFolderViewModel(val videoDao: VideoDao) : ViewModel() {
    val videosLd = videoDao.getAll(true)
}