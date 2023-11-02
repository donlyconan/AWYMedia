package com.utc.donlyconan.media.app.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.data.models.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val NOW_PLAYING_CHANNEL_ID = "com.utc.donlyconan.media.app.services.NOW_PLAYING_CHANNEL_ID"
const val NOW_PLAYING_NOTIFICATION_ID = 100

internal class AudioNotificationManager(
    private val context: Context,
    private val sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private var listMode: Boolean = false) {

    private val notificationManager: PlayerNotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val videoDao by lazy {
        (context.applicationContext as EGMApplication)
            .applicationComponent()
            .getVideoDao()
    }

    init {
        val builder = PlayerNotificationManager.Builder(context, NOW_PLAYING_NOTIFICATION_ID, NOW_PLAYING_CHANNEL_ID)
        with (builder) {
            setMediaDescriptionAdapter(DescriptionAdapter())
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.notification_channel)
            setChannelDescriptionResourceId(R.string.notification_channel_description)
            setSmallIconResourceId(R.drawable.ic_logo)
        }
        notificationManager = builder.build()
        with(notificationManager) {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_logo)
            setUseNextAction(listMode)
            setUsePreviousAction(listMode)
            setUseChronometer(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
        }
    }

    fun setListMode(listMode: Boolean) {
        Log.d(TAG, "setListMode() called with: listMode = $listMode")
        this.listMode = listMode
        with(notificationManager) {
            setUseNextAction(listMode)
            setUsePreviousAction(listMode)
        }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player){
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter() :
        PlayerNotificationManager.MediaDescriptionAdapter {

        private var bitmap: Bitmap? = null
        private var uri: Uri? = null
        private var curVideo: Video? = null


        override fun createCurrentContentIntent(player: Player): PendingIntent {
            val intent = Intent(AudioService.ACTION_REQUEST_OPEN_ACTIVITY)
            return PendingIntent.getBroadcast(context, AudioService.REQUEST_OPEN_DISPLAY_ACTIVITY, intent, PendingIntent.FLAG_MUTABLE)
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return "The music is playing by Easy Guard Player"
        }

        override fun getCurrentContentTitle(player: Player): CharSequence {
            updateVideo(player.currentMediaItem?.localConfiguration?.uri)
            return curVideo?.title.toString()
        }

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            Log.d(TAG, "getCurrentLargeIcon() called with: player = ${player.currentMediaItem}, callback = $callback")
            val newUri = player.currentMediaItem?.localConfiguration?.uri
            updateVideo(newUri) { uri ->
                coroutineScope.launch {
                    val newBitmap = Glide.with(context)
                        .applyDefaultRequestOptions(
                            RequestOptions()
                                .fallback(R.drawable.ic_baseline_error_24)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        )
                        .asBitmap()
                        .centerCrop()
                        .load(uri)
                        .error(R.drawable.ic_baseline_error_24)
                        .submit(NOTIFICATION_MEDIUM_ICON_SIZE, NOTIFICATION_MEDIUM_ICON_SIZE)
                        .get()
                    bitmap.let { callback.onBitmap(newBitmap) }
                }
            }
            return bitmap
        }

        fun updateVideo(newUri: Uri?, onUpdatedListener: (newUri: Uri) -> Unit = {}) {
            if(newUri != null && uri?.path != newUri.path) {
                curVideo = videoDao.get(newUri.toString().trim())
                uri = newUri
                onUpdatedListener(newUri)
            }
            Log.d(TAG, "updateVideo() called with: newUri = ${newUri?.toString()}, curVideo=$curVideo")
        }
    }
}

val TAG = AudioNotificationManager::class.simpleName
const val NOTIFICATION_MEDIUM_ICON_SIZE = 64