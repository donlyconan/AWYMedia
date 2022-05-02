package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import com.utc.donlyconan.media.data.dao.*
import com.utc.donlyconan.media.data.db.AwyMediaDatabase
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {

    @Provides
    fun provideDatabase(application: Application): AwyMediaDatabase  {
        return AwyMediaDatabase.getInstance(application)
    }

    @Provides
    fun provideVideoDao(database: AwyMediaDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideListVideoDao(database: AwyMediaDatabase): ListVideoDao {
        return database.listVideoDao()
    }

    @Provides
    fun providePlaylistDao(database: AwyMediaDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun providePlaylistWithVideosDao(database: AwyMediaDatabase): PlaylistWithVideosDao {
        return database.playlistWithVideosDao()
    }

    @Provides
    fun provideTrashDao(database: AwyMediaDatabase): TrashDao {
        return database.getTrashDao()
    }

}