package com.utc.donlyconan.media.data.repo

import androidx.lifecycle.LiveData
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.PlaylistWithVideos
import com.utc.donlyconan.media.data.models.Video
import javax.inject.Inject

class PlaylistRepository @Inject constructor(val playlistDao: PlaylistDao,
                                             val playlistWithVideosDao: PlaylistWithVideosDao): PlaylistDao by playlistDao {

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

    fun deleteFromPlaylist(videoId: Int, playlistId: Int) =
        playlistWithVideosDao.deleteFromPlaylist(videoId, playlistId)


    fun getFirstVideo(playlistId: Int): Video? {
        return playlistWithVideosDao.getFirstVideo(playlistId)
    }
}