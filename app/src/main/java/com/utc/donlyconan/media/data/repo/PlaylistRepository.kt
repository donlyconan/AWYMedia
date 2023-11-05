package com.utc.donlyconan.media.data.repo

import android.app.Application
import android.content.Context
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.dao.PlaylistDao
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.PlaylistWithVideos
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.models.VideoPlaylistCrossRef
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    val application: Application,
    val playlistDao: PlaylistDao,
    val playlistWithVideosDao: PlaylistWithVideosDao,
    val videoDao: VideoDao): PlaylistDao by playlistDao {

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

    suspend fun getPlaylistWithVideos(playlistId: Int): PlaylistWithVideos? {
        return if(playlistId == Playlist.PRIVATE_PLAYLIST_FOLDER) {
            val videos = videoDao.getAllSecuredVideos()
            val playlist = Playlist(playlistId, application.getString(R.string.private_folder))
            PlaylistWithVideos(playlist, videos)
        } else {
            playlistWithVideosDao.get(playlistId)
        }
    }
}