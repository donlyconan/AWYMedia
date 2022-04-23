package com.utc.donlyconan.media.dagger.modules

import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.ListVideoRepositoryImpl
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindListVideoRepository(impl: ListVideoRepositoryImpl): ListVideoRepository

    @Singleton
    @Binds
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository

}