package com.utc.donlyconan.media.viewmodels

import android.app.Application

class PlaylistViewModel(app: Application): BaseAndroidViewModel(app) {
    val playlistRepo = myApp.applicationComponent().getPlaylistRepository()
    val listPlaylist = playlistRepo.getAll()
}