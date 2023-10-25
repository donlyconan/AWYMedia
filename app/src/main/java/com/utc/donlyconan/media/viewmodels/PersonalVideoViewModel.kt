package com.utc.donlyconan.media.viewmodels

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.extension.components.getAllVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonalVideoViewModel(val videoDao: VideoDao, val contentResolver: ContentResolver) : ViewModel() {

    val videosLd = videoDao.getAll(isSecured = false)

    fun importVideos() = viewModelScope.launch(Dispatchers.IO) {
        val videos = contentResolver.getAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoDao.insert(*videos.toTypedArray())
    }
}