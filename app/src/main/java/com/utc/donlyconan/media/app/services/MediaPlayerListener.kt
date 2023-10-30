package com.utc.donlyconan.media.app.services

import android.net.Uri
import com.google.android.exoplayer2.Player

interface MediaPlayerListener : Player.Listener {

    fun onAudioServiceAvailable(available: Boolean)

    fun onInitialVideo(uri: Uri) {}

}