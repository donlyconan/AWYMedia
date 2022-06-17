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
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.DialogDisplayAgainBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.extension.widgets.showMessage
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
    // tell us that we will prevent playing change listener
    private var flagPlayingChanged = false


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
        bindingOverlay = PlayerControlViewBinding.bind(binding.root.findViewById(R.id.scrim_view))
        beView = CustomOptionPlayerControlViewBinding
            .bind(bindingOverlay.layoutPlayerControlView.rootView)

        viewModel.video.observe(this) { video ->
            Log.d(TAG, "onCreate: position=${viewModel.position}, video=$video")
            beView.headerTv.text = video.title
            if(viewModel.isResetPosition) {
                Log.d(TAG, "onCreate: reset position")
                video.playedTime = 0L
            }
            try {
                initializePlayer(video)
                viewModel.isResetPosition = false
            } catch (e: Exception) {
                showMessage(e.message)
                e.printStackTrace()
            }
        }
        viewModel.speed.observe(this){ speed ->
            Log.d(TAG, "onCreate() called with: speed = $speed")
            player?.setPlaybackSpeed(speed)
        }
        viewModel.repeatMode.observe(this){ mode ->
            Log.d(TAG, "onCreate() called with: mode = $mode")
            player?.repeatMode = mode
            beView.exoLoop?.isSelected = mode == ExoPlayer.REPEAT_MODE_ONE
        }
        viewModel.playWhenReady.observe(this) { enabled ->
            Log.d(TAG, "onCreate() called with: enabled = $enabled")
            player?.playWhenReady = enabled
        }
        loadingVideo()
        initialize(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
        viewModel.isInitial = false
        loadingVideo()
    }

    fun loadingVideo() {
        Log.d(TAG, "loadingVideo() called isInit = ${viewModel.isInitial}")
        if (!viewModel.isInitial) {
            Log.d(TAG, "loadingVideo: video is loaded!")
            return
        }
        viewModel.playlist = intent.getParcelableArrayListExtra(EXTRA_PLAYLIST) ?: arrayListOf()
        viewModel.position = intent.getIntExtra(EXTRA_POSITION, 0)
        viewModel.isContinue = intent.getBooleanExtra(EXTRA_CONTINUE, false)
        viewModel.speed.value = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
        viewModel.repeatMode.value =
            intent.getIntExtra(EXTRA_REPEAT_MODE, ExoPlayer.REPEAT_MODE_OFF)
        viewModel.playWhenReady.value = settings.autoPlay
        if (viewModel.canShowDialog()) {
            // Show dialog to restore state of video when restoreState from setting equals true
            if(settings.restoreState) {
                val binding = DialogDisplayAgainBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(this)
                    .setView(binding.root)
                    .setCancelable(true)
                    .create()
                // Reset position of video time down to zero and save the current position of video
                val video = viewModel.currentVideo()
                val playedTime = video.playedTime
                viewModel.video.value = video.apply {
                    this.playedTime = 0
                }
                binding.btnNo.setOnClickListener {
                    dialog.dismiss()
                }
                binding.btnYes.setOnClickListener {
                    Log.d(TAG, "loadingVideo() playedTime=${playedTime}")
                    flagPlayingChanged = true
                    player?.seekTo(playedTime)
                    flagPlayingChanged = false
                    dialog.dismiss()
                }
                dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                dialog.show()
            } else {
                viewModel.video.value = viewModel.currentVideo().apply {
                    playedTime = -1L
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
            beView.exoPlayMusic?.setOnClickListener(this)
        } else {
            beView.exoOption?.setOnClickListener(this)
        }
        bindingOverlay.exoUnlock.setOnClickListener(this)
        beView.autoPlay.isChecked = settings.autoPlayMode
        beView.exoRotate.setOnClickListener(this)
        beView.exoBack.setOnClickListener(this)
        beView.autoPlay.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.autoPlayMode = isChecked
        }
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
                    flagPlayingChanged = true
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
                    }.show(supportFragmentManager, TAG)
                }
                R.id.exo_loop -> {
                    val status = player?.repeatMode == ExoPlayer.REPEAT_MODE_ONE
                    if(!status) {
                        viewModel.repeatMode.value = ExoPlayer.REPEAT_MODE_ONE
                    } else {
                        viewModel.repeatMode.value = ExoPlayer.REPEAT_MODE_OFF
                    }
                }
                R.id.exo_playback_speed -> {
                    SpeedOptionFragment.newInstance(player?.playbackParameters?.speed ?: 1f,
                        object : SpeedOptionFragment.OnSelectedSpeedChangeListener {
                            override fun onSelectedSpeedChanged(speed: Float) {
                                Log.d(TAG, "onSelectedSpeedChanged() called with: speed = $speed")
                                viewModel.speed.value = speed
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
                R.id.exo_play_music -> {
                    val application = application as AwyMediaApplication
                    viewModel.currentVideo().playedTime = player?.currentPosition ?: 0L
                    application.iMusicalService()?.apply {
                        setPlaylist(viewModel.position, viewModel.playlist)
                        setKeepPlaying(true)
                        setSpeed(viewModel.speed.value!!)
                        setRepeat(viewModel.repeatMode.value!!)
                        play()
                        finish()
                    }
                }
                else -> {
                    Log.d(TAG, "onClick: not found ${msg.what}")
                }
            }
        }
    }

    private val listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition: mediaItem=$mediaItem")
            with(viewModel) {
                player?.stop()
                playWhenReady.value = beView.autoPlay.isChecked
                viewModel.isResetPosition = true
                updatePosition(player!!.currentMediaItemIndex)
                val lastIndex = player!!.currentMediaItemIndex - 1
                if(lastIndex >= 0) {
                    val video = playlist[lastIndex]
                    video.playedTime = -1L
                    videoRepo.update(video)
                }
            }
            super.onMediaItemTransition(mediaItem, reason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            if(!flagPlayingChanged) {
                viewModel.playWhenReady.value = isPlaying
                binding.videoView.keepScreenOn = isPlaying
                if(!isPlaying) {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                        "playbackState = $playbackState")
            if (playbackState == Player.STATE_BUFFERING) {
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
                exoPlayer.playWhenReady = viewModel.playWhenReady.value ?: settings.autoPlay
                exoPlayer.seekTo(index, video.playedTime)
                exoPlayer.prepare()
                exoPlayer.addListener(listener)
            }
        viewModel.apply {
            isContinue = true
            isInitial = false
        }
    }

    private fun releasePlayer() {
        player?.run {
            viewModel.video.value?.playedTime = currentPosition
            removeListener(listener)
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
        const val EXTRA_POSITION = "com.utc.donlyconan.media.EXTRA_POSITION"
        const val EXTRA_CONTINUE = "com.utc.donlyconan.media.EXTRA_CONTINUE"
        const val EXTRA_PLAYLIST = "com.utc.donlyconan.media.EXTRA_PLAYLIST"
        const val EXTRA_REPEAT_MODE = "com.utc.donlyconan.media.EXTRA_REPEAT_MODE"
        const val EXTRA_SPEED = "com.utc.donlyconan.media.EXTRA_SPEED"
        val TAG: String = VideoDisplayActivity::class.java.simpleName

        fun newIntent(context: Context, position: Int, playlist: ArrayList<Video>,
                      isContinue: Boolean = false, speed: Float = 1.0f, repeatMode: Int = ExoPlayer.REPEAT_MODE_OFF): Intent {
            Log.d(TAG, "newIntent() called with: context = $context, position = $position, playlist = $playlist, isContinue = $isContinue")
            return Intent(context, VideoDisplayActivity::class.java).apply {
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_PLAYLIST, playlist)
                putExtra(EXTRA_CONTINUE, isContinue)
                putExtra(EXTRA_REPEAT_MODE, repeatMode)
                putExtra(EXTRA_SPEED, speed)
            }
        }

    }
}