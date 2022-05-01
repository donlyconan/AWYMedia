package com.utc.donlyconan.media.app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.views.VideoDisplayActivity

/**
 * Provide a way to play video
 */
fun playVideo(context: Context, video: Video) {
    Log.d(TAG, "playVideo() called with: video = $video")
    val intent = Intent(context, VideoDisplayActivity::class.java)
    intent.putExtra(VideoDisplayActivity.EXTRA_VIDEO, video)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    context.startActivity(intent)
}