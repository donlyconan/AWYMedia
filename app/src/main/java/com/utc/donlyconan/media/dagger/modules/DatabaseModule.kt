package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import com.utc.donlyconan.media.data.dao.*
import com.utc.donlyconan.media.data.db.EGMDatabase
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {

    @Provides
    fun provideDatabase(application: Application): EGMDatabase  {
        return EGMDatabase.getInstance(application)
    }

    @Provides
    fun provideVideoDao(database: EGMDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideListVideoDao(database: EGMDatabase): ListVideoDao {
        return database.listVideoDao()
    }

    @Provides
    fun providePlaylistDao(database: EGMDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun providePlaylistWithVideosDao(database: EGMDatabase): PlaylistWithVideosDao {
        return database.playlistWithVideosDao()
    }

    @Provides
    fun provideTrashDao(database: EGMDatabase): TrashDao {
        return database.getTrashDao()
    }

}