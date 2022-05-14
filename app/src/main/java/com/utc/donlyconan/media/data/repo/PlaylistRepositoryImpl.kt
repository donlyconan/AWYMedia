package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.PlaylistWithVideos
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(val playlistDao: PlaylistDao,
                                                 val playlistWithVideosDao: PlaylistWithVideosDao): PlaylistRepository {

    override fun insert(vararg playlist: Playlist) {
        playlistDao.insert(*playlist)
    }

    override fun update(vararg playlists: Playlist) {
        playlists.forEach { pl -> pl.updatedAt = System.currentTimeMillis() }
        playlistDao.update(*playlists)
    }

    override fun delete(vararg playlists: Playlist) {
        playlistDao.delete(*playlists)
    }

    override fun countVideos(playlistId: Int): Int {
        return playlistDao.countVideos(playlistId)
    }

    override fun getAllPlaylist() = playlistDao.getAllPlaylist()

    override fun deleteFromPlaylist(videoId: Int, playlistId: Int) =
        playlistWithVideosDao.deleteFromPlaylist(videoId, playlistId)

    override fun getPlaylistWithVideos(): LiveData<List<PlaylistWithVideos>> =
        playlistWithVideosDao.getPlaylistWithVideos()

    override fun removeVideoFromPlaylist(videoId: Int) = playlistDao.removeVideoFromPlaylist(videoId)

    override fun removePlaylist(playlistId: Int) = playlistDao.removePlaylist(playlistId)

}