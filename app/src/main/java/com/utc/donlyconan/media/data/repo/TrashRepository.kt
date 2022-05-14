package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.models.Trash

interface TrashRepository {

    fun insert(vararg trash: Trash)

    fun delete(vararg trash: Trash)

    fun update(vararg trash: Trash)

    fun find(videoId: Int): Trash?

    fun getTrashes(): LiveData<List<Trash>>

    suspend fun getAllTrashes(): List<Trash>

    fun removeAll()

}