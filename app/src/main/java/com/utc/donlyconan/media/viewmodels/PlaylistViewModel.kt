package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaylistViewModel(app: Application): BaseAndroidViewModel(app) {

    @Inject lateinit var playlistRepo: PlaylistRepository
    lateinit var listPlaylist: Flow<PagingData<Playlist>>

    init {
        awyApp.applicationComponent().inject(this)
        listPlaylist = playlistRepo.getAllPlaylist().cachedIn(viewModelScope)
    }
}