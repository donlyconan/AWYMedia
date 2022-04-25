package com.utc.donlyconan.media.data.repo

import androidx.paging.PagingData
import com.utc.donlyconan.media.data.models.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    fun insert(vararg playlist: Playlist)

    fun delete(vararg playlist: Playlist)

    fun getAllPlaylist(): Flow<PagingData<Playlist>>

}