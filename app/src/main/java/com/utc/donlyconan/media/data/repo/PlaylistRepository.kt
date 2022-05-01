package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video

interface PlaylistRepository {

    fun insert(vararg playlist: Playlist)

    fun delete(vararg playlists: Playlist)

    fun countVideos(playlistId: Int): Int

    fun getAllPlaylist(): LiveData<List<Playlist>>

}