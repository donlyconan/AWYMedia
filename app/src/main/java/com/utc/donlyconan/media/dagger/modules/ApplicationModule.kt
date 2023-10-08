package com.utc.donlyconan.media.dagger.modules

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ApplicationModule(@Named("application") var application: Application) {

    @Provides
    fun provideApplication(): Application {
        return application
    }

}