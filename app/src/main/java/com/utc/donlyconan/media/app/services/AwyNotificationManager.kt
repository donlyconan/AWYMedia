package com.utc.donlyconan.media.app.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.VideoDisplayActivity
import kotlinx.coroutines.*

const val NOW_PLAYING_CHANNEL_ID = "com.utc.donlyconan.media.app.services.NOW_PLAYING_CHANNEL_ID"
const val NOW_PLAYING_NOTIFICATION_ID = 100

internal class AWYNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener) {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: PlayerNotificationManager
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var playlist: ArrayList<Video> = arrayListOf()
    private val settings by lazy { Settings.getInstance(context) }

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        val builder = PlayerNotificationManager.Builder(context, NOW_PLAYING_NOTIFICATION_ID, NOW_PLAYING_CHANNEL_ID)
        with (builder) {
            setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.notification_channel)
            setChannelDescriptionResourceId(R.string.notification_channel_description)
            setSmallIconResourceId(R.drawable.ic_logo)
        }
        notificationManager = builder.build()
        with(notificationManager) {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_logo)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setUseNextAction(true)
            setUseChronometer(true)
            setUsePreviousAction(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
        }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player, playlist: ArrayList<Video>){
        notificationManager.setPlayer(player)
        this.playlist = playlist
    }

    private inner class DescriptionAdapter(val mediaControllerCompat: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent {
            Log.d(TAG, "createCurrentContentIntent: index=${player.currentWindowIndex}")
            val intent = Intent(ACTION_MUSIC_SERVICE_RECEIVE)
            return PendingIntent.getBroadcast(context, 100, intent, 0)
        }

        override fun getCurrentContentText(player: Player) = null

        override fun getCurrentContentTitle(player: Player): String {
            Log.d(TAG, "getCurrentContentTitle: index=${player.currentWindowIndex}")
            return playlist[player.currentWindowIndex].title.toString()
        }

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val iconUri = Uri.parse(playlist[player.currentWindowIndex].path)
            Log.d(TAG, "getCurrentLargeIcon: uri=$iconUri")
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return withContext(Dispatchers.IO) {
                try {
                    Glide.with(context).applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .centerCrop()
                        .load(uri)
                        .submit(NOTIFICATION_MEDIUM_ICON_SIZE, NOTIFICATION_MEDIUM_ICON_SIZE)
                        .get()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}

val TAG = AWYNotificationManager::class.simpleName
const val NOTIFICATION_MEDIUM_ICON_SIZE = 64
private val glideOptions = RequestOptions()
    .fallback(R.drawable.video_play_demo)
    .diskCacheStrategy(DiskCacheStrategy.DATA)

