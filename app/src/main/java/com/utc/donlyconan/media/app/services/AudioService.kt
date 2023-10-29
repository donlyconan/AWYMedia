package com.utc.donlyconan.media.app.services

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.utc.donlyconan.media.app.EGMApplication


/**
 * This is service which will play media sound
 */
class AudioService : Service() {

    companion object {
        val TAG: String = AudioService::class.java.simpleName
        const val ACTION_MUSIC_SERVICE_RECEIVE = "com.utc.donlyconan.media.app.services.ACTION_MUSIC_SERVICE_RECEIVE"
        const val REQUEST_OPEN_DISPLAY_ACTIVITY = 11
    }
    private var isForegroundService: Boolean = false
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: AudioNotificationManager

    private val binder = EGMBinder()
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    private var player: ExoPlayer? = null
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private val mediaListeners by lazy { ArrayList<Player.Listener>() }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        (applicationContext as EGMApplication).applicationComponent()
            .inject(this)
        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.isActive = true
        notificationManager = AudioNotificationManager(this, mediaSession.sessionToken, PlayerNotificationListener())
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
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        releasePlayer()
        unregisterReceiver(broadcastReceiver)
    }

    fun setupPlayer() {
        Log.d(TAG, "setupPlayer() called")
       player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    fun play(item: MediaItem, repeatMode: Int = ExoPlayer.REPEAT_MODE_OFF) {
        Log.d(TAG, "play() called with: item = $item")
        if(player != null) {
            player?.apply {
                stop()
                setMediaItem(item)
                prepare()
                play()
                notificationManager.showNotificationForPlayer(this)
            }
        } else {
            setupPlayer()
            mediaSessionConnector.setPlayer(player)
            player?.apply {
                setMediaItem(item)
                this.repeatMode = repeatMode
                addListener(mediaStateListener)
                prepare()
                playWhenReady = true
                notificationManager.showNotificationForPlayer(this)
            }
        }
    }

    fun releasePlayer() {
        notificationManager.hideNotification()
        player?.apply {
            stop()
            release()
        }
    }

    private val broadcastReceiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: player is available [${player == null}]")
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
            if(ongoing && !isForegroundService) {
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled() called with: notificationId = $notificationId, " +
                    "dismissedByUser = $dismissedByUser")
            if(dismissedByUser) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                isForegroundService = false
                stopSelf()
            }
        }
    }

    private val mediaStateListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            mediaListeners.forEach { it.onIsPlayingChanged(isPlaying) }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            mediaListeners.forEach { it.onMediaMetadataChanged(mediaMetadata) }
        }
    }

    fun registerPlayerListener(listener: Player.Listener) {
        Log.d(TAG, "registerPlayerListener() called with: listener = $listener")
        mediaListeners.add(listener)
    }

    fun removePlayerListener(listener: Player.Listener) {
        Log.d(TAG, "removePlayerListener() called with: listener = $listener")
        mediaListeners.remove(listener)
    }

    /**
     * Stop playing musics
     */
    fun stop() {
        player?.pause()
    }

    /**
     * Start to play musics
     */
    fun start() {
        player?.play()
    }

    fun getPlayer(): ExoPlayer? {
        return player
    }


    inner class EGMBinder : Binder() {

        /** @returns local service */
        fun getService() : AudioService {
            return this@AudioService
        }

    }

}