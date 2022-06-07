package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import android.content.Context
import com.utc.donlyconan.media.app.settings.Settings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SettingsModule {

    @Singleton
    @Provides
    fun provideSettings(application: Application): Settings {
        return Settings.getInstance(application)
    }

}