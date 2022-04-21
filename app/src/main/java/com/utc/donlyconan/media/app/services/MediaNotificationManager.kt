package com.utc.donlyconan.media.app.services

import android.app.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.session.MediaButtonReceiver
import androidx.core.content.ContextCompat
import com.utc.donlyconan.media.R
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.utc.donlyconan.media.views.MainActivity

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
class MediaNotificationManager(private val service: Service) {
    private val playAction: NotificationCompat.Action
    private val pauseAction: NotificationCompat.Action
    private val nextAction: NotificationCompat.Action
    private val previousAction: NotificationCompat.Action
    val notificationManager: NotificationManager =
        service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

    init {
        playAction = NotificationCompat.Action(
            R.drawable.ic_exo_icon_play,
            "Play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, PlaybackStateCompat.ACTION_PLAY
            )
        )
        pauseAction = NotificationCompat.Action(
            R.drawable.ic_exo_icon_pause,
            "Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, PlaybackStateCompat.ACTION_PAUSE
            )
        )
        previousAction = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_previous_24,
            "Previous",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
        nextAction = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_next_24,
            "Next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )
        notificationManager.cancelAll()
    }

    fun getNotification(
        metadata: MediaMetadataCompat,
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token
    ): Notification {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val description = metadata.description
        val builder = buildNotification(state, token, isPlaying, description)
        return builder.build()
    }

    private fun buildNotification(
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token,
        isPlaying: Boolean,
        description: MediaDescriptionCompat
    ): NotificationCompat.Builder {

        // Create the (mandatory) notification channel when running on Android Oreo.
        if (isAndroidOOrHigher) {
            createChannel()
        }
        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0) // For backwards compatibility with Android L and earlier.
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        service,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
        )
            .setColor(ContextCompat.getColor(service, R.color.black))
            .setSmallIcon(R.drawable.ic_exo_icon_play) // Pending intent that is fired when user clicks on notification.
            .setContentIntent(createContentIntent()) // Title - Usually Song name.
            .setContentTitle(description.title) // When notification is deleted (when playback is paused and notification can be
            // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    service, PlaybackStateCompat.ACTION_PAUSE
                )
            )
        builder.addAction(if (isPlaying) pauseAction else playAction)
        return builder
    }

    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            val name: CharSequence = "MediaSession"
            // The user-visible description of the channel.
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            // Configure the notification channel.
            mChannel.description = description
            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(mChannel)
            Log.d(TAG, "createChannel: New channel created")
        } else {
            Log.d(TAG, "createChannel: Existing channel reused")
        }
    }

    private val isAndroidOOrHigher: Boolean
        private get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(service, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            service, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    companion object {
        const val NOTIFICATION_ID = 412
        private val TAG = MediaNotificationManager::class.java.simpleName
        private const val CHANNEL_ID = "com.utc.donlyconan.medial.mediaservice"
        private const val REQUEST_CODE = 501
    }

}