package com.utc.donlyconan.media.viewmodels

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.RecycleBinFragment.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrashViewModel : ViewModel()  {

    @Inject lateinit var trashDao: TrashDao
    @Inject lateinit var videoDao:VideoDao
    @Inject lateinit var playlistDao: PlaylistDao
    @Inject lateinit var settings: Settings
    @Inject lateinit var contentResolver: ContentResolver
    lateinit var videosMdl: LiveData<List<Trash>>
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.Default) }

    fun init() {
        videosMdl = trashDao.getTrashes()
    }

    fun restore(trash: Trash) {
        Log.d(TAG, "restore() called with: trash = $trash")
        val video = trash.toVideo()
        videoDao.insert(video)
        trashDao.delete(trash)
    }

    fun delete(item: Trash) {
        Log.d(TAG, "delete() called with: trash = $item")
        trashDao.delete(item)
        viewModelScope.launch {
            playlistDao.removeVideoFromPlaylist(item.videoId)
            deleteFile(Uri.parse(item.videoUri))
            Log.d(TAG, "clearAll: Done!")
        }
    }

    private fun deleteFile(uri: Uri) {
        contentResolver.delete(uri, null, null)
    }

    fun clearAll(trashes: List<Trash>) {
        Log.d(TAG, "clearAll() called size=${trashes.size}")
        viewModelScope.launch {
            trashes.forEach { trash ->
                playlistDao.removeVideoFromPlaylist(trash.videoId)
                Log.d(TAG, "clearAll: deleteFromStorage=" + settings.deleteFromStorage)
                if(settings.deleteFromStorage) {
                    contentResolver.delete(Uri.parse(trash.videoUri), null, null)
                }
            }
            trashDao.removeAll()
            Log.d(TAG, "clearAll: Done!")
        }
    }

}