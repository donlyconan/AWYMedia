package com.utc.donlyconan.media.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.services.MusicalService
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.DialogDisplayAgainBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.views.fragments.options.SpeedOptionFragment
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreFragment


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
class VideoDisplayActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by viewModels<VideoDisplayViewModel>()
    private val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private lateinit var bindingOverlay: PlayerControlViewBinding
    private lateinit var beView: CustomOptionPlayerControlViewBinding
    private var player: ExoPlayer? = null


    private val systemFlags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // stop media service
        (applicationContext as AwyMediaApplication).iMusicalService()
            ?.release()

        setContentView(binding.root)
        (applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        bindingOverlay = PlayerControlViewBinding.bind(binding.root.findViewById(R.id.scrim_view))
        beView = CustomOptionPlayerControlViewBinding
            .bind(bindingOverlay.layoutPlayerControlView.rootView)
        initialize(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

        viewModel.video.observe(this) { video ->
            Log.d(TAG, "onCreate: position=${viewModel.position}, video=$video")
            beView.headerTv.text = video.title
            if(viewModel.isResetPosition) {
                video.playedTime = 0L
            }
            initializePlayer(video)
            viewModel.isResetPosition = false
        }
        if(viewModel.isInitial) {
            viewModel.playlist = intent.getParcelableArrayListExtra(EXTRA_PLAYLIST) ?: arrayListOf()
            viewModel.position = intent.getIntExtra(EXTRA_POSITION, 0)
            viewModel.isContinue = intent.getBooleanExtra(EXTRA_CONTINUE, false)
            viewModel.playWhenReady = settings.autoPlay
        }
        if (viewModel.canShowDialog()) {
            // Show dialog to restore state of video when restoreState from setting equals true
            if(settings.restoreState) {
                val binding = DialogDisplayAgainBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(this)
                    .setView(binding.root)
                    .setCancelable(false)
                    .create()
                binding.btnNo.setOnClickListener {
                    viewModel.video.value = viewModel.currentVideo().apply {
                        playedTime = 0L
                    }
                    dialog.dismiss()
                }
                binding.btnYes.setOnClickListener {
                    viewModel.video.value = viewModel.currentVideo()
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                viewModel.video.value = viewModel.currentVideo().apply {
                    playedTime = 0L
                }
            }
            viewModel.isContinue = true
        } else {
            viewModel.video.value = if(viewModel.video.value == null)
                viewModel.currentVideo()
             else viewModel.video.value?.copy(updatedAt = System.currentTimeMillis())
        }
    }

    private fun initialize(isLScreen: Boolean) {
        Log.d(TAG, "initialize() called with: isLandscapeScreen = $isLScreen")
        if (isLScreen) {
            beView.exoLoop?.setOnClickListener(this)
            beView.exoLock?.setOnClickListener(this)
            beView.exoPrev?.setOnClickListener(this)
            beView.exoPlaybackSpeed?.setOnClickListener(this)
            beView.exoNext?.setOnClickListener(this)
        } else {
            beView.exoOption?.setOnClickListener(this)
        }
        bindingOverlay.exoUnlock.setOnClickListener(this)
        beView.exoRotate.setOnClickListener(this)
        beView.exoBack.setOnClickListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged() called with: newConfig = $newConfig")
        hideSystemUi()
    }

    override fun onStart() {
        hideSystemUi()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: isInPictureInPictureMode=${isInPictureInPictureMode}")
        player?.stop()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        viewModel.save()
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        handler.sendEmptyMessage(v.id)
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d(TAG, "handleMessage() called with: msg = $msg")
            when (msg.what) {
                R.id.exo_back -> {
                    finish()
                }
                R.id.exo_rotate -> {
                    requestedOrientation =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                R.id.exo_unlock -> {
                    bindingOverlay.layoutPlayerControlView.rootView.visibility = View.VISIBLE
                    bindingOverlay.layoutScrim.visibility = View.INVISIBLE
                }
                R.id.exo_lock -> {
                    bindingOverlay.layoutPlayerControlView.rootView.visibility = View.INVISIBLE
                    bindingOverlay.layoutScrim.visibility = View.VISIBLE
                }
                R.id.exo_option -> {
                    val enabled = player?.repeatMode != ExoPlayer.REPEAT_MODE_OFF
                    val hasNext = viewModel.hasNext()
                    val hasPrev = viewModel.hasPrev()
                    VideoMenuMoreFragment.newInstance(enabled, hasNext, hasPrev) { v->
                        sendEmptyMessage(v.id)
                    }
                        .show(supportFragmentManager, TAG)
                }
                R.id.exo_loop -> {
                    val enabled = !(beView.exoLoop?.isSelected ?: true)
                    beView.exoLoop?.isSelected = enabled
                    player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                }
                R.id.exo_playback_speed -> {
                    SpeedOptionFragment.newInstance(player?.playbackParameters?.speed ?: 1f,
                        object : SpeedOptionFragment.OnSelectedSpeedChangeListener {
                            override fun onSelectedSpeedChanged(speed: Float) {
                                Log.d(TAG, "onSelectedSpeedChanged() called with: speed = $speed")
                                player?.setPlaybackSpeed(speed)
                            }
                        }
                    ).show(supportFragmentManager, TAG)
                }
                R.id.exo_next -> {
                    releasePlayer()
                    viewModel.next()
                    beView.exoPrev?.apply {
                        isClickable = true
                        setTextColor(resources.getColor(R.color.white))
                    }
                }
                R.id.exo_prev -> {
                    releasePlayer()
                    viewModel.previous()
                    if(!viewModel.hasPrev()) {
                        beView.exoPrev?.apply {
                            isClickable = false
                            setTextColor(resources.getColor(R.color.gray_20))
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "onClick: not found ${msg.what}")
                }
            }
        }
    }

    private val listener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(
                TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                        "playbackState = $playbackState"
            )
            if (playbackState == Player.STATE_ENDED) {
                viewModel.endVideo()
            } else if (playbackState == Player.STATE_READY) {
                viewModel.isFinished = false
            }
        }
    }

    private fun initializePlayer(video: Video) {
        Log.d(TAG, "initializePlayer() called with: video = $video, playlist=${viewModel.playlist}")
        if (viewModel.isInitial && settings.autoRotate) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, video.path.toUri())
            val orientation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toInt()
                    ?.also { orientation ->
                        if (orientation == 0 && resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    }
            Log.d(TAG, "initializePlayer: orientation=$orientation")
        }
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                exoPlayer.addMediaItems(viewModel.playlist.map { video ->  MediaItem.fromUri(video.path) })
                val index = viewModel.playlist.indexOfFirst { v -> video.videoId == v.videoId }
                Log.d(TAG, "initializePlayer: index=$index")
                exoPlayer.seekTo(index, video.playedTime)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = viewModel.playWhenReady
            }
        player?.addListener(listener)
        viewModel.isContinue = true
        viewModel.isInitial = false
    }

    private fun releasePlayer() {
        player?.run {
            viewModel.video.value?.playedTime = currentPosition
            playWhenReady = this.playWhenReady
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
            binding.videoView.player = null
            Log.d(TAG, "releasePlayer() called video=${viewModel.video.value}")
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        Log.d(TAG, "hideSystemUi() called")
        window.decorView.systemUiVisibility = systemFlags
        window.decorView.setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener)
    }


    private val onSystemUiVisibilityChangeListener =
        View.OnSystemUiVisibilityChangeListener { visibility ->
            Log.d(TAG, "onSystemUiVisibilityChange() called with: visibility = $visibility")
            if (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0) {
                Log.d(TAG, "onSystemUiVisibilityChange() called hide systemui")
                window.decorView.systemUiVisibility = systemFlags
            }
        }

    companion object {
        const val EXTRA_POSITION = "EXTRA_VIDEO"
        const val EXTRA_CONTINUE = "EXTRA_CONTINUE"
        const val EXTRA_PLAYLIST = "EXTRA_PLAYLIST"
        val TAG: String = VideoDisplayActivity::class.java.simpleName

        fun newIntent(context: Context, position: Int, playlist: ArrayList<Video>, isContinue: Boolean = false) =
            Intent(context, VideoDisplayActivity::class.java).apply {
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_PLAYLIST, playlist)
                putExtra(EXTRA_CONTINUE, isContinue)
            }
    }
}