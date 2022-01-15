package com.utc.donlyconan.media.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

abstract class AwyMediaDatabase: RoomDatabase() {

    companion object {
        val DATABASE_NAME = "AWY-Media"
        @Volatile
        private var instance: AwyMediaDatabase? = null

        fun getInstance(context: Context) = synchronized(this) {
            if(instance == null) {
                instance = Room.databaseBuilder(context, AwyMediaDatabase::class.java, DATABASE_NAME)
                    .build()
            }
            instance
        }
    }

    // Create daos in here

}