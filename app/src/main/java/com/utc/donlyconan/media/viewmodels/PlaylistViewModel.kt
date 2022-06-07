package com.utc.donlyconan.media.viewmodels

import android.app.Application

class PlaylistViewModel(app: Application): BaseAndroidViewModel(app) {
    val playlistRepo = myApp.applicationComponent().getPlaylistRepo()
    val listPlaylist = playlistRepo.getAllPlaylist()
}