package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.dao.TrashDao
import com.utc.donlyconan.media.data.models.Trash
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepositoryImpl @Inject constructor(val dao: TrashDao) : TrashRepository {

    override fun insert(vararg trash: Trash) = dao.insert(*trash)

    override fun delete(vararg trash: Trash) = dao.delete(*trash)

    override fun update(vararg trash: Trash) = dao.update(*trash)

    override fun getTrashes(): LiveData<List<Trash>> = dao.getTrashes()

    override fun find(videoId: Int): Trash? = dao.find(videoId)

    override suspend fun getAllTrashes(): List<Trash> = dao.getAllTrashes()

    override fun removeAll() = dao.removeAll()

}