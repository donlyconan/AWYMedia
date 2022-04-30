package com.utc.donlyconan.media.data.repo

import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.models.Playlist
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(val playlistDao: PlaylistDao): PlaylistRepository {

    override fun insert(vararg playlist: Playlist) {
        playlistDao.insert(*playlist)
    }

    override fun delete(vararg playlists: Playlist) {
        playlistDao.delete(*playlists)
    }

    override fun countVideos(playlistId: Int): Int {
        return playlistDao.countVideos(playlistId)
    }

    override fun getAllPlaylist() = playlistDao.getAllPlaylist()

}