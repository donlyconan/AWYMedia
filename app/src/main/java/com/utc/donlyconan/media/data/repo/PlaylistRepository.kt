package com.utc.donlyconan.media.data.repo

import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import javax.inject.Inject

class PlaylistRepository @Inject constructor(val playlistDao: PlaylistDao,
                                             val playlistWithVideosDao: PlaylistWithVideosDao): PlaylistDao by playlistDao {

    override fun update(vararg playlists: Playlist) {
        playlists.forEach { pl -> pl.updatedAt = System.currentTimeMillis() }
        playlistDao.update(*playlists)
    }

    override fun delete(vararg playlists: Playlist) {
        val playlistIds = playlists.map { it.playlistId!! }
            .toIntArray()
        playlistWithVideosDao.deleteByPlaylistId(*playlistIds)
        playlistDao.delete(*playlists)
    }

    fun deleteFromPlaylist(videoId: Int, playlistId: Int) =
        playlistWithVideosDao.deleteFromPlaylist(videoId, playlistId)


    fun getFirstVideo(playlistId: Int): Video? {
        return playlistWithVideosDao.getFirstVideo(playlistId)
    }
}