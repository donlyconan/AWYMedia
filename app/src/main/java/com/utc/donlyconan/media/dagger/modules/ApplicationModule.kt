package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import com.utc.donlyconan.media.app.AwyMediaApplication
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

}