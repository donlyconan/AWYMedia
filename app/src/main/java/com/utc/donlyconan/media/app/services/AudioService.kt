package com.utc.donlyconan.media.app.services

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.views.VideoDisplayActivity


/**
 * This is service which will play media sound
 */
class AudioService : Service() {

    companion object {
        val TAG: String = AudioService::class.java.simpleName
        const val ACTION_PLAY_MUSIC = "com.utc.donlyconan.media.app.services.ACTION_PLAY_MUSIC"
        const val ACTION_REQUEST_OPEN_ACTIVITY = "com.utc.donlyconan.media.app.services.ACTION_REQUEST_OPEN_ACTIVITY"
        const val REQUEST_OPEN_DISPLAY_ACTIVITY = 11
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val EXTRA_PLAYLIST = "EXTRA_PLAYLIST"
        const val EXTRA_REPEAT_MODE = "EXTRA_REPEATE_MODE"
        const val EXTRA_SPEED = "EXTRA_SPEED"
        const val EXTRA_POSITION = "EXTRA_POSITION"

        fun createIntent(
            playlist: Array<String>,
            index: Int = 0,
            repeatMode: Int = Player.REPEAT_MODE_OFF,
            speed: Float = 1.0f,
            position: Long = 0,
        ) = Intent(ACTION_PLAY_MUSIC).apply {
            putExtra(EXTRA_INDEX, index)
            putExtra(EXTRA_PLAYLIST, playlist)
            putExtra(EXTRA_REPEAT_MODE, repeatMode)
            putExtra(EXTRA_POSITION, position)
        }

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
    private val mediaListeners by lazy { ArrayList<MediaPlayerListener>() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        (applicationContext as EGMApplication).applicationComponent()
            .inject(this)
        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.isActive = true
        notificationManager = AudioNotificationManager(this, mediaSession.sessionToken, PlayerNotificationListener())
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(ACTION_REQUEST_OPEN_ACTIVITY)
            addAction(ACTION_PLAY_MUSIC)
        })


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

    private fun setupPlayer() {
        Log.d(TAG, "setupPlayer() called")
       player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    fun play(item: MediaItem, repeatMode: Int = ExoPlayer.REPEAT_MODE_OFF) {
        Log.d(TAG, "play() with player is available = ${isAvailable()}")
        play(listOf(item), 0 , repeatMode)
    }

    fun play(
        items: List<MediaItem>,
        index: Int = 0,
        repeatMode: Int = ExoPlayer.REPEAT_MODE_OFF,
        speed: Float = 1.0f,
        position: Long = 0,
    ) {
        Log.d(TAG, "playlist() with player is available = ${isAvailable()}")
        if(index >= items.size) {
            Log.d(TAG, "playlist() called with: check index again.")
            return
        }
        if(player != null) {
            releasePlayer()
        }
        setupPlayer()
        mediaSessionConnector.setPlayer(player)
        player?.apply {
            setMediaItems(items)
            seekTo(index, position)
            this.repeatMode = repeatMode
            setPlaybackSpeed(speed)
            addListener(mediaStateListener)
            prepare()
            playWhenReady = true
            notificationManager.setListMode(items.size > 1)
            notificationManager.showNotificationForPlayer(this)
        }
        mediaStateListener.onAudioServiceAvailable(true)
        mediaStateListener.onInitialVideo(items[index].localConfiguration?.uri!!)
    }

    fun releasePlayer() {
        player?.apply {
            stop()
            release()
        }
        notificationManager.hideNotification()
        mediaStateListener.onAudioServiceAvailable(false)
    }

    private val broadcastReceiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: action = ${intent?.action}, player is available [${player != null}]")
            when(intent?.action) {
                ACTION_PLAY_MUSIC -> {
                    val index = intent.getIntExtra(EXTRA_INDEX, 0)
                    val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                    val repeatMode = intent.getIntExtra(EXTRA_REPEAT_MODE, Player.REPEAT_MODE_OFF)
                    val position = intent.getLongExtra(EXTRA_POSITION, 0)
                    intent.getStringArrayExtra(EXTRA_PLAYLIST)?.let { playlist ->
                        val items = playlist.map { MediaItem.fromUri(it) }
                        if(items.isNotEmpty()) {
                            play(items, index, repeatMode, speed, position)
                        }
                    }

                }
                ACTION_REQUEST_OPEN_ACTIVITY -> {
                    openVideoDisplay(context)
                }
                else -> {}
            }
        }
    }

    fun openVideoDisplay(context: Context?) {
        player?.currentMediaItem?.localConfiguration?.uri?.let { uri ->
            val position = player!!.currentPosition
            val videoRepo = (context!!.applicationContext as EGMApplication).applicationComponent()
                .getVideoRepository()
            videoRepo.get(uri.toString())?.let { video ->
                video.playedTime = position
                videoRepo.update(video)
                startActivity(
                    VideoDisplayActivity.newIntent(
                        context,
                        video.videoId,
                        video.videoUri,
                        continued = true
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
            }
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

    private val mediaStateListener = object : MediaPlayerListener {

        override fun onAudioServiceAvailable(available: Boolean) {
            Log.d(TAG, "onAudioServiceAvailable() called with: available = $available")
            mediaListeners.forEach { it.onAudioServiceAvailable(available) }
        }

        override fun onInitialVideo(uri: Uri) {
            Log.d(TAG, "onInitialVideo() called with: uri = $uri")
            mediaListeners.forEach { it.onInitialVideo(uri) }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            mediaListeners.forEach { it.onIsPlayingChanged(isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            Log.d(TAG, "onMediaItemTransition() called with: mediaItem = $mediaItem, reason = $reason")
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Log.d(TAG, "onMediaMetadataChanged() called with: mediaMetadata = $mediaMetadata")
            mediaListeners.forEach { it.onMediaMetadataChanged(mediaMetadata) }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.d(TAG, "onPlayerError() called with: error = $error")
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            Log.d(TAG, "onPlayerErrorChanged() called with: error = $error")
        }

        override fun onMetadata(metadata: Metadata) {
            super.onMetadata(metadata)
            Log.d(TAG, "onMetadata() called with: metadata = $metadata")
        }

    }

    fun registerPlayerListener(listener: MediaPlayerListener) {
        Log.d(TAG, "registerPlayerListener() called with: listener = $listener")
        mediaListeners.add(listener)
        mediaListeners.forEach { e ->
            e.onAudioServiceAvailable(isAvailable())
            player?.currentMediaItem?.localConfiguration?.uri?.let {uri ->
                e.onInitialVideo(uri)
            }
        }
    }

    fun removePlayerListener(listener: MediaPlayerListener) {
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
        player?.let { player ->
            if (player.playbackState == Player.STATE_ENDED ) {
                Log.d(TAG, "start: prepare service. state = ${player.playbackState}")
                player.prepare()
                player.seekTo(0L)
            }
            player.play()
        }
    }

    fun getPlayer(): ExoPlayer? {
        return player
    }

    fun isAvailable(): Boolean = player != null


    inner class EGMBinder : Binder() {

        /** @returns local service */
        fun getService() : AudioService {
            return this@AudioService
        }

    }

}