package com.utc.donlyconan.media.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.utc.donlyconan.media.data.dao.*
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.models.VideoPlaylistCrossRef

val MIGRATION_1_2 = Migration(1, 2) { db ->
    db.execSQL("Alter table trashes add column external_uri text")
    db.execSQL("Alter table videos add column external_uri text")
}

@Database(
    entities = [Video::class, Playlist::class, VideoPlaylistCrossRef::class, Trash::class],
    version = 2, exportSchema = false
)
abstract class EGMDatabase: RoomDatabase() {

    companion object {
        private const val DATABASE_NAME = "EGPMedia"
        @Volatile
        private var instance: EGMDatabase? = null

        fun getInstance(context: Context): EGMDatabase = synchronized(this) {
            if(instance == null) {
                instance = Room.databaseBuilder(context, EGMDatabase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
            instance!!
        }
    }

    // Create daos in here
    abstract fun listVideoDao(): ListVideoDao

    abstract fun videoDao(): VideoDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun playlistWithVideosDao(): PlaylistWithVideosDao

    abstract fun getTrashDao(): TrashDao

}