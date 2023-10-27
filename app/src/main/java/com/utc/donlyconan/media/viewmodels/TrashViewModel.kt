package com.utc.donlyconan.media.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.RecycleBinFragment.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrashViewModel(val trashDao: TrashDao, val videoDao: VideoDao, val playlistDao: PlaylistDao) : ViewModel()  {
    var videosMdl: LiveData<List<Trash>> = trashDao.getTrashes()

    fun restore(trash: Trash) {
        Log.d(TAG, "restore() called with: trash = $trash")
        val video = trash.convertToVideo()
        viewModelScope.launch(Dispatchers.IO) {
            videoDao.insert(video)
            trashDao.delete(trash)
        }
    }

    fun delete(item: Trash) {
        Log.d(TAG, "delete() called with: trash = $item")
        viewModelScope.launch(Dispatchers.IO) {
            trashDao.delete(item)
            playlistDao.removeVideoFromPlaylist(item.videoId)
        }
    }

}