package com.utc.donlyconan.media.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.RecycleBinFragment.Companion.TAG
import java.lang.Exception

class TrashViewModel(val trashDao: TrashDao, val videoDao: VideoDao) : ViewModel()  {
    var videosMdl: LiveData<List<Trash>> = trashDao.getTrashes()

    suspend fun restore(fileService: FileService, vararg trash: Trash, onError: (e: Exception) -> Unit = {e -> }) {
        Log.d(TAG, "restore() called with: trash = ${trash.size}")
        trash.filter { it.isSecured }
            .apply {
                trashDao.delete(*toTypedArray())
            }
            .map { it.convertToVideo() }
            .apply {
                videoDao.insert(*toTypedArray())
            }
        val externalItems = trash.filter { !it.isSecured }
        externalItems.map { it.title!! }
            .forEach { fname ->
                try {
                    fileService.saveIntoExternal(fname) { _, _, _ ->
                        trashDao.delete(fname)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "restore: ", e)
                    onError?.invoke(e)
                }
            }
    }

    suspend fun delete(fileService: FileService, vararg item: Trash) {
        Log.d(TAG, "delete() called with: trash = $item")
        val names = item.map { it.title!! }.toTypedArray()
        if(fileService.deleteFileFromLocalData(*names) > 0) {
            trashDao.delete(*item)
        }
    }
}