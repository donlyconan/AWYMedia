package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import android.content.ContentResolver
import com.utc.donlyconan.media.app.FileManager
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(@Named("application") var application: Application) {

    @Provides
    fun provideApplication(): Application {
        return application
    }

    @Provides
    fun provideContentResolver(application: Application): ContentResolver {
        return application.contentResolver
    }

    @Provides
    @Singleton
    fun provideFileManager(application: Application) : FileManager {
        return FileManager(application)
    }

}