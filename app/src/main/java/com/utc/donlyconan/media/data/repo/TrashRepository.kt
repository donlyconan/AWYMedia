package com.utc.donlyconan.media.data.repo

import android.app.Application
import android.content.Context
import android.util.Log
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Trash
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepository @Inject constructor(val context: Application,val videoDao: VideoDao, val trashDao: TrashDao) : TrashDao by trashDao {

    suspend fun sync() {
        val titles = videoDao.getAllTitlesInPrivateFolder()
        val trashItems = trashDao.getAllTrashes()
        val trashes = context.filesDir?.listFiles { file ->
            !titles.contains(file.name) && trashItems.any { file.name == it.title }
        }?.map { Trash.fromFile(it) }
        Logs.d("sync: ${trashes?.size} special files.")
        trashes?.let { trashes ->
            trashDao.insert(*trashes.toTypedArray())
        }
    }

}