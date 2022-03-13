package com.utc.donlyconan.media.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel

class VideoDisplayViewModel : ViewModel() {
    var videoUri: Uri? = null
    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition = 0L


    override fun toString(): String {
        return "VideoDisplayViewModel(videoUri=$videoUri, playWhenReady=$playWhenReady, " +
                "currentWindow=$currentWindow, playbackPosition=$playbackPosition)"
    }
}