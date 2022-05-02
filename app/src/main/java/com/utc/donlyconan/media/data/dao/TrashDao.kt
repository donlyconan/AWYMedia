package com.utc.donlyconan.media.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utc.donlyconan.media.data.models.Trash

@Dao
interface TrashDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg trash: Trash)

    @Delete
    fun delete(vararg trash: Trash)

    @Update
    fun update(vararg trash: Trash)

    @Query("Select * from trashes")
    fun getTrashes(): LiveData<List<Trash>>

    @Query("Delete from trashes")
    fun removeAll()

}