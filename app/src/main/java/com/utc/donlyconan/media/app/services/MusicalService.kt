package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.utc.donlyconan.media.IMusicalService
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.dao.VideoDao
import javax.inject.Inject

class MusicalService: Service() {

    companion object {
        val TAG: String = MusicalService.javaClass.simpleName
        const val channelId = "musical-service-id"
    }

    private val binder = object : IMusicalService.Stub() {

        override fun setVideoId(videoId: Int) {
            Log.d(TAG, "setVideoId() called with: videoId = $videoId")
            this@MusicalService.videoId = videoId
        }

        override fun play() {
            Log.d(TAG, "play() called")
            if(videoId != -1) {
                val video = videoDao.getVideo(videoId)
                Log.d(TAG, "play: video=$video")
                player?.apply {
                    setDataSource(applicationContext, Uri.parse(video.path))
                    prepare()
                    start()
                }
            } else {
                release()
            }
        }

        override fun release() {
            Log.d(TAG, "release() called")
            player?.stop()
            player?.release()
        }

        override fun next() {
            Log.d(TAG, "next() called")
            val video = videoDao.getNextVideo(videoId)
            videoId = video.videoId
            play()
        }

        override fun previous() {
            Log.d(TAG, "previous() called")
            Log.d(TAG, "next() called")
            val video = videoDao.getPreviousVideo(videoId)
            videoId = video.videoId
            play()
        }

        override fun stop() {
            Log.d(TAG, "stop() called")
            player?.stop()
        }

        override fun restart() {
            Log.d(TAG, "restart() called")
            player?.start()
        }

    }

    var videoId: Int = -1
    private var player: MediaPlayer? = null
    @Inject lateinit var videoDao: VideoDao

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        (applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        val notificationLayout = RemoteViews(packageName, R.layout.notify_media_player_info)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return binder.asBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        player?.stop()
        player?.release()
    }
}