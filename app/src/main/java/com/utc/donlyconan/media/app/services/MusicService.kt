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
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.VideoDisplayActivity


const val ACTION_MUSIC_SERVICE_RECEIVE = "com.utc.donlyconan.media.app.services.ACTION_MUSIC_SERVICE_RECEIVE"

/**
 * This is service which will play media sound
 */
class MusicService : Service() {
    private var isForegroundService: Boolean = false
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: AWYNotificationManager
    private val binder = MusicBinder()
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    private lateinit var player: Player
    private lateinit var mediaSessionConnector: MediaSessionConnector


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.isActive = true
        val playerNotificationManager = PlayerNotificationListener()
        notificationManager = AWYNotificationManager(this, mediaSession.sessionToken,
            playerNotificationManager)
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        registerReceiver(receiver, IntentFilter(ACTION_MUSIC_SERVICE_RECEIVE))
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, " +
                "flags = $flags, startId = $startId")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        release()
        unregisterReceiver(receiver)
    }

    private val receiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: player is available [${player == null}]")
            player.let { player ->
//                val index = player.currentWindowIndex
//                playlist[index].playedTime = player.currentPosition
//                val intent = VideoDisplayActivity.newIntent(this@MusicService, index, playlist,
//                    true, player.playbackParameters.speed, player.repeatMode)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
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
        override fun onNotificationPosted(notificationId: Int, notification: Notification,
                                          ongoing: Boolean) {
            Log.d(TAG, "onNotificationPosted() called with: " +
                    "notificationId = $notificationId, " +
                    "isForegroundService = $isForegroundService, " +
                    "ongoing = $ongoing")
            if (ongoing && !isForegroundService) {
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled() called with: " +
                    "notificationId = $notificationId, " +
                    "dismissedByUser = $dismissedByUser")
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    /**
     * Returns current player that playing music and it can be null
     * @return player or null
     */
    fun getPlayer(): Player {
        return player
    }

    fun setPlaylist(position: Int, playlist: MutableList<Video>) {
        Log.d(TAG, "setPlaylist() called with: position = $position, playlist.size = ${playlist.size}")
        with(player) {
            setPlaybackSpeed(1.0f)
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun setSpeed(speed: Float) {
        Log.d(TAG, "setSpeed() called with: speed = $speed")
        player.setPlaybackSpeed(1.0f)
    }

    fun setRepeat(repeatMode: Int) {
        Log.d(TAG, "setRepeat() called with: repeatMode = $repeatMode")
        player.repeatMode = repeatMode
    }

    fun play() {
        Log.d(TAG, "play() called speed=$speed, repeate=$repeatMode")
        release()
        createPlayer()
        with(player) {
            prepare()
            playWhenReady = true
            notificationManager.showNotificationForPlayer(this, playlist)
            setPlaybackSpeed(speed)
            repeatMode = this@MusicService.repeatMode
        }
    }


    fun next() {
        Log.d(TAG, "next() called position=$position")
        if (position < playlist.size - 1) {
            position++
            player.seekToNextMediaItem()
        }
    }

    fun previous() {
        Log.d(TAG, "previous() called position=$position")
        if (position > 0) {
            position--
            player.seekToPreviousMediaItem()
        }
    }

    fun pause() {
        Log.d(TAG, "pause() called")
        player.pause()
    }

    /**
     * It will perform ExoPlayer.release() and hideNotification()
     */
    fun release() {
        Log.d(TAG, "release() called")
        player.release()
        notificationManager.hideNotification()
    }


    /**
     * Allows hide notification when player is playing
     */
    fun hideNotification() {
        notificationManager.hideNotification()
    }

    inner class MusicBinder : Binder() {
        fun getMusicService(): MusicService = this@MusicService
    }

    companion object {
        val TAG: String = MusicService::class.java.simpleName
    }
}