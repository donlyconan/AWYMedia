package com.utc.donlyconan.media.data.repo

import android.app.Application
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepository @Inject constructor(val context: Application,
                                          private val videoDao: VideoDao,
                                          private val trashDao: TrashDao) : TrashDao by trashDao {

    /**
     * Perform sync all files in the local storage
     * if files don't have info in the Video table. It's considered as trash items
     */
    suspend fun sync() {
        val titles = videoDao.getAllTitlesInPrivateFolder()
        val trashItems = trashDao.getAllTrashes()
        val trashes = context.filesDir?.listFiles()?.filter { file ->  file.isFile }
            ?.filterNot { file->
            titles.any { it == file.name } || trashItems.any { it.title == file.name }
        }?.map { Trash.fromFile(it) }
        Logs.d("sync: ${trashes?.size} special files.")
        trashes?.let { trashes ->
            trashDao.insert(*trashes.toTypedArray())
        }
        removeWhenFileIsUnavailable()
    }

    suspend fun removeWhenFileIsUnavailable() {
        Logs.d( "removeWhenFileIsUnavailable() called")
        val fileList = context.fileList()
        val trashes = trashDao.getAllTrashes()
            .filter {
                !fileList.any() { fileName -> fileName == it.title }
            }
        Logs.d("removeWhenFileIsUnavailable: has ${trashes.size} that is not found!")
        trashDao.delete(*trashes.toTypedArray())
    }

}