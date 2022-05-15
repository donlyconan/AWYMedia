package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import androidx.room.Query
import androidx.room.Transaction
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.PlaylistWithVideos
import com.utc.donlyconan.media.data.models.Video

interface PlaylistRepository {

    fun insert(vararg playlist: Playlist)

    fun update(vararg playlists: Playlist)

    fun delete(vararg playlists: Playlist)

    fun countVideos(playlistId: Int): Int

    fun getAllPlaylist(): LiveData<List<Playlist>>

    fun deleteFromPlaylist(videoId: Int, playlistId: Int)

    fun getPlaylistWithVideos(): LiveData<List<PlaylistWithVideos>>

    fun removeVideoFromPlaylist(videoId: Int)

    fun removePlaylist(playlistId: Int)

    fun findAll(keyword: String): LiveData<List<Playlist>>

}