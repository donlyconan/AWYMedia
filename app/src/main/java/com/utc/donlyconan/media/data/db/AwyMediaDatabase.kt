package com.utc.donlyconan.media.data.db

import android.content.Context
import androidx.constraintlayout.widget.ConstraintSet
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.utc.donlyconan.media.data.dao.ListVideosDao
import com.utc.donlyconan.media.data.models.Video

@Database(entities = [Video::class], version = 1, exportSchema = false)
abstract class AwyMediaDatabase: RoomDatabase() {

    companion object {
        val DATABASE_NAME = "AWY_Media"
        @Volatile
        private var instance: AwyMediaDatabase? = null

        fun getInstance(context: Context): AwyMediaDatabase = synchronized(this) {
            if(instance == null) {
                instance = Room.databaseBuilder(context, AwyMediaDatabase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build()
            }
            instance!!
        }
    }

    // Create daos in here
    abstract fun getListVideoDao(): ListVideosDao

}