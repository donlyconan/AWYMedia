package com.utc.donlyconan.media.viewmodels

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.fragments.TrashFragment.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

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

    fun delete(trash: Trash) {
        Log.d(TAG, "delete() called with: trash = $trash")
        trashDao.delete(trash)
        viewModelScope.launch {
            playlistDao.removeVideoFromPlaylist(trash.videoId)
            Log.d(TAG, "clearAll: deleteFromStorage=" + settings.deleteFromStorage)
            contentResolver.delete(Uri.parse(trash.path), null, null)
            Log.d(TAG, "clearAll: Done!")
        }
    }

    fun clearAll(trashes: List<Trash>) {
        Log.d(TAG, "clearAll() called size=${trashes.size}")
        viewModelScope.launch {
            trashes.forEach { trash ->
                playlistDao.removeVideoFromPlaylist(trash.videoId)
                Log.d(TAG, "clearAll: deleteFromStorage=" + settings.deleteFromStorage)
                if(settings.deleteFromStorage) {
                    contentResolver.delete(Uri.parse(trash.path), null, null)
                }
            }
            trashDao.removeAll()
            Log.d(TAG, "clearAll: Done!")
        }
    }

}