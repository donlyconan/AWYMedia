package com.utc.donlyconan.media.app.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.utc.donlyconan.media.IMusicService
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.VideoDisplayActivity


const val ACTION_MUSIC_SERVICE_RECEIVE = "com.utc.donlyconan.media.app.services.ACTION_MUSIC_SERVICE_RECEIVE"

/**
 * This is service which will play media sound
 */
class MusicService : Service() {

    companion object {
        val TAG: String = MusicService::class.java.simpleName
    }

    private var position: Int = 0
    private var playlist: ArrayList<Video> = arrayListOf()
    private var isForegroundService: Boolean = false
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: AWYNotificationManager
    private var isKeepPlaying: Boolean = false
    private var speed = 1.0f
    private var repeatMode = Player.REPEAT_MODE_OFF

    private val binder = MusicServiceBinderImpl()
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    private var player: Player? = null
    private lateinit var mediaSessionConnector: MediaSessionConnector


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        (applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
        mediaSession = MediaSessionCompat(this, TAG).apply {
            isActive = true
        }
        notificationManager = AWYNotificationManager(this, mediaSession.sessionToken,
            PlayerNotificationListener())
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_MUSIC_SERVICE_RECEIVE))
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, " +
                "flags = $flags, startId = $startId")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return binder.asBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        binder.release()
        unregisterReceiver(broadcastReceiver)
    }

    private val broadcastReceiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: player is available [${player == null}]")
            player?.let { player ->
                val index = player.currentWindowIndex
                playlist[index].playedTime = player.currentPosition
                val intent = VideoDisplayActivity.newIntent(this@MusicService, index, playlist,
                    true, player.playbackParameters.speed, player.repeatMode)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun createPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(PlayerListenerImpl())
            }
        mediaSessionConnector.setPlayer(player)
    }

    inner class PlayerListenerImpl  : Player.Listener {

        override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
            super.onTracksInfoChanged(tracksInfo)
            Log.d(TAG, "onTracksInfoChanged() called with: tracksInfo = $tracksInfo")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged() called with:  playbackState = $playbackState")

        }
    }


    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean) {
            Log.d(TAG, "onNotificationPosted() called with: notificationId = $notificationId, " +
                    "isForegroundService = $isForegroundService, ongoing = $ongoing")
            if (ongoing && !isForegroundService) {
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled() called with: notificationId = $notificationId, " +
                    "dismissedByUser = $dismissedByUser")
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    inner class MusicServiceBinderImpl : IMusicService.Stub() {

        private val service = this@MusicService

        override fun setPlaylist(position: Int, playlist: MutableList<Video>) {
            Log.d(TAG,
                "setPlaylist() called with: position = $position, playlist.size = ${playlist.size}")
            service.position = position
            service.playlist = ArrayList(playlist)
            speed = 1.0f
            repeatMode = Player.REPEAT_MODE_OFF
        }

        override fun setSpeed(speed: Float) {
            Log.d(TAG, "setSpeed() called with: speed = $speed")
            service.speed = speed
        }

        override fun setRepeat(repeatMode: Int) {
            Log.d(TAG, "setRepeat() called with: repeatMode = $repeatMode")
            service.repeatMode = repeatMode
        }

        override fun setKeepPlaying(isKeepPlaying: Boolean) {
            Log.d(TAG, "setKeepPlaying() called with: isKeepPlaying = $isKeepPlaying")
            service.isKeepPlaying = isKeepPlaying
        }

        override fun play() {
            Log.d(TAG, "play() called speed=$speed, repeate=$repeatMode")
            release()
            createPlayer()
            player?.apply {
                clearMediaItems()
                addMediaItems(playlist.map { video ->  MediaItem.fromUri(video.path) })
                if(isKeepPlaying) {
                    seekTo(position, playlist[position].playedTime)
                    isKeepPlaying = false
                } else {
                    seekTo(position, 0L)
                }
                prepare()
                playWhenReady = true
                notificationManager.showNotificationForPlayer(this, playlist)
                setPlaybackSpeed(speed)
                repeatMode = this@MusicService.repeatMode
            }
        }


        override fun next() {
            Log.d(TAG, "next() called position=$position")
            if (position < playlist.size - 1) {
                position++
                player?.seekToNextMediaItem()
            }
        }

        override fun previous() {
            Log.d(TAG, "previous() called position=$position")
            if (position > 0) {
                position--
                player?.seekToPreviousMediaItem()
            }
        }

        override fun pause() {
            Log.d(TAG, "pause() called")
            player?.pause()
        }

        override fun release() {
            Log.d(TAG, "release() called")
            player?.release()
            notificationManager.hideNotification()
        }

    }
}