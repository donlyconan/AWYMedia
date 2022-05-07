package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.utc.donlyconan.media.IMusicalService
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import javax.inject.Inject

/**
 * This is service which will play media sound
 */
class MusicalService : Service() {

    companion object {
        val TAG: String = MusicalService::class.java.simpleName
        const val channelId = "musical-service-id"
    }

    private val binder = object : IMusicalService.Stub() {
        override fun setPlaylist(position: Int, playlist: MutableList<Video>) {
            Log.d(TAG,
                "setPlaylist() called with: position = $position, playlist.size = ${playlist.size}")
            this@MusicalService.position = position
            this@MusicalService.playlist = playlist
        }

        override fun play() {
            val video = playlist.get(position)
            Log.d(TAG, "play() called video=$video")
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(this@MusicalService, video.path.toUri())
                prepare()
                start()
            }
        }

        override fun next() {
            Log.d(TAG, "next() called position=$position")
            if (position < playlist!!.size - 1) {
                position++
                release()
                play()
            }
        }

        override fun previous() {
            Log.d(TAG, "previous() called position=$position")
            if (position > 0) {
                position--
                release()
                play()
            }
        }

        override fun pause() {
            Log.d(TAG, "pause() called")
            player?.pause()
        }

        override fun release() {
            Log.d(TAG, "release() called")
            player?.apply {
                stop()
                release()
            }
            player = null
        }

    }

    var videoId: Int = -1
    private var player: MediaPlayer? = null
    private var position: Int = 0
    private var playlist: MutableList<Video> = arrayListOf()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        (applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return binder.asBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        binder.release()
    }
}