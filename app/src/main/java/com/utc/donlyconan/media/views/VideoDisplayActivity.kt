package com.utc.donlyconan.media.views

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.views.fragments.options.SpeedOptionFragment
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreFragment


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
class VideoDisplayActivity : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private val bindingScrim by lazy { PlayerControlViewBinding.bind(findViewById(R.id.scrim_view)) }
    private val bindingExtView by lazy {
        CustomOptionPlayerControlViewBinding.bind(bindingScrim.layoutPlayerControlView.rootView)
    }
    private var player: ExoPlayer? = null
    private val viewModel by viewModels<VideoDisplayViewModel>()
    private val videoDao by lazy { (applicationContext as AwyMediaApplication).videoDao }

    private val systemFlags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: This Activity is created on portrait screen " +
                "[${resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT}]")
        setContentView(binding.root)
        viewModel.video.observe(this) { video ->
            bindingExtView.headerTv.text = video.title
            val nextVideo = videoDao.getNextVideo(video.videoId)
            nextVideo?.let {
                val item = MediaItem.fromUri(Uri.parse(it.path))
                player?.addMediaItem(item)
            }
        }
        if(viewModel.video.value == null) {
            viewModel.video.value = intent.getParcelableExtra(KEY_VIDEO)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initialize(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

    private fun  initialize(isLandscapeScreen: Boolean) {
        Log.d(TAG, "initialize() called with: isLandscapeScreen = $isLandscapeScreen")
        if(isLandscapeScreen) {
            bindingExtView.exoLoop?.setOnClickListener(this)
            bindingExtView.exoLock?.setOnClickListener(this)
            bindingExtView.exoSubtitle?.setOnClickListener(this)
            bindingExtView.exoPlaybackSpeed?.setOnClickListener(this)
            bindingExtView.exoNext?.setOnClickListener(this)
        } else{
            bindingExtView.exoOption?.setOnClickListener(this)
        }
        bindingScrim.exoUnlock.setOnClickListener(this)
        bindingExtView.exoRotate.setOnClickListener(this)
        bindingExtView.exoBack.setOnClickListener(this)
        initializePlayer(viewModel.video.value!!)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        enterPictureInPictureMode()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d(TAG, "onUserLeaveHint() called")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged() called with: newConfig = $newConfig")
        hideSystemUi()
    }

    override fun onResume() {
        hideSystemUi()
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.stop()
    }

    override fun onDestroy() {
        releasePlayer()
        viewModel.saveVideoIfNeed()
        super.onDestroy()
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
                    bindingScrim.layoutPlayerControlView.rootView.visibility = View.VISIBLE
                    bindingScrim.layoutScrim.visibility = View.INVISIBLE
                }
                R.id.exo_lock -> {
                    bindingScrim.layoutPlayerControlView.rootView.visibility = View.INVISIBLE
                    bindingScrim.layoutScrim.visibility = View.VISIBLE
                }
                R.id.exo_option -> {
                    val enabled = player?.repeatMode != ExoPlayer.REPEAT_MODE_OFF
                    VideoMenuMoreFragment.newInstance(this@VideoDisplayActivity, enabled)
                        .show(supportFragmentManager, TAG)
                }
                R.id.exo_loop -> {
                    val enabled = !(bindingExtView.exoLoop?.isSelected ?: true)
                    bindingExtView.exoLoop?.isSelected = enabled
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
                    val nextVideo = videoDao.getNextVideo(viewModel.video.value!!.videoId)
                    viewModel.video.value = nextVideo
                    player?.seekToNextWindow()
                }
                else -> {
                    Log.d(TAG, "onClick: not found ${msg.what}")
                }
            }
        }
    }

    private val listener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                    "playbackState = $playbackState")
            if(playbackState == Player.STATE_ENDED) {
                viewModel.video.value?.playedTime = 0
            }
        }
    }

    private fun initializePlayer(video: Video) {
        Log.d(TAG, "initializePlayer() called with: video = $video")
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(Uri.parse(video.path))
                exoPlayer.setMediaItem(mediaItem, false)
                exoPlayer.seekTo(video.playedTime)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false
                exoPlayer.playWhenReady = viewModel.playWhenReady
            }
        player?.addListener(listener)
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
        val KEY_VIDEO = "key_video"
    }


}