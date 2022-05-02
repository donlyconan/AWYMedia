package com.utc.donlyconan.media.dagger.modules

import com.utc.donlyconan.media.data.repo.*
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

    @Singleton
    @Binds
    abstract fun bindPlaylistRepo(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Singleton
    @Binds
    abstract fun bindTrashRepo(impl: TrashRepositoryImpl): TrashRepository


}