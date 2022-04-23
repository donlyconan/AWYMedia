package com.utc.donlyconan.media.dagger.components

import android.app.Application
import android.content.Context
import android.os.Binder
import com.utc.donlyconan.media.app.services.MusicalService
import com.utc.donlyconan.media.dagger.modules.ApplicationModule
import com.utc.donlyconan.media.dagger.modules.DatabaseModule
import com.utc.donlyconan.media.dagger.modules.RepositoryModule
import com.utc.donlyconan.media.dagger.modules.SettingsModule
import com.utc.donlyconan.media.data.dao.ListVideoDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.fragments.*
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    DatabaseModule::class,
    RepositoryModule::class,
    SettingsModule::class
])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        // Takes an instance of application when creating ApplicationComponent
        fun applicationModule(module: ApplicationModule): Builder

        fun build(): ApplicationComponent

    }

    fun getVideoRepo(): VideoRepository
    fun getListVideoRepo(): ListVideoRepository

    fun inject(fragment: PersonalVideoFragment)
    fun inject(fragment: DetailedPlaylistFragment)
    fun inject(fragment: ExpendedPlaylistFragment)
    fun inject(fragment: FavoriteFragment)
    fun inject(fragment: ListVideoFragment)
    fun inject(fragment: MainDisplayFragment)
    fun inject(fragment: MusicalFragment)
    fun inject(fragment: PlaylistFragment)
    fun inject(fragment: RecentFragment)
    fun inject(fragment: SearchBarFragment)
    fun inject(fragment: SplashScreenFragment)
    fun inject(fragment: TrashFragment)
    fun inject(fragment: VideoDisplayFragment)
    fun inject(fragment: MusicalService)
    fun inject(fragment: VideoDisplayActivity)
}