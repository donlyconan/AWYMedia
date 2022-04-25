package com.utc.donlyconan.media.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.views.adapter.Constant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(val playlistDao: PlaylistDao): PlaylistRepository {

    override fun insert(vararg playlist: Playlist) {
        playlistDao.insert(*playlist)
    }

    override fun delete(vararg playlist: Playlist) {
        playlistDao.delete(*playlist)
    }

    override fun getAllPlaylist(): Flow<PagingData<Playlist>> = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { playlistDao.getAllPlaylist() }
    ).flow
}