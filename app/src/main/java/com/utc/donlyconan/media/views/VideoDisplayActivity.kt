package com.utc.donlyconan.media.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.os.*
import android.util.Log
import android.view.ActionMode
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.services.EGMService
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.DialogDisplayAgainBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.views.fragments.options.SpeedOptionFragment
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreFragment
import com.utc.donlyconan.media.views.fragments.options.listedvideos.ListedVideosDialog
import com.utc.donlyconan.media.views.fragments.options.listedvideos.OnSelectedChangeListener


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
class VideoDisplayActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by viewModels<VideoDisplayViewModel>()
    private val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private lateinit var playerControlBinding: PlayerControlViewBinding
    private lateinit var customBinding: CustomOptionPlayerControlViewBinding
    private val service: EGMService? by lazy { application.getEgmService() }
    private var _player: ExoPlayer? = null
    private val player by lazy<ExoPlayer> { _player!! }
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

//        // stop media service
//        (applicationContext as EGMApplication).getEgmService()
//            ?.release()

        setContentView(binding.root)
        (applicationContext as EGMApplication).applicationComponent().let { com ->
            com.inject(this)
            com.inject(viewModel)
        }
        playerControlBinding = PlayerControlViewBinding.bind(binding.root.findViewById(R.id.scrim_view))
        customBinding = CustomOptionPlayerControlViewBinding
            .bind(playerControlBinding.layoutPlayerControlView.rootView)

        service?.setupPlayer()
        _player = service?.getPlayer()

        if(service == null || _player == null){
            Logs.d(TAG, "onCreate: service.(${service==null}) or player.(${player==null}) is not available!")
            // TODO show errors when player is invalid
        } else {
            val videoId = intent.getIntExtra(EXTRA_VIDEO_ID, -1)
            val playlistId = intent.getIntExtra(EXTRA_PLAYLIST, -1)
            val continued = intent.getBooleanExtra(EXTRA_CONTINUE, false)

            with(viewModel) {
                initialize(videoId, playlistId, continued)

                videoMld.observe(this@VideoDisplayActivity) { video ->
                    Logs.d(TAG, "video View Model: video=$video")
                    customBinding.headerTv.text = video.title
                    player.apply {
                        setMediaItem(MediaItem.fromUri(video.path))
                        binding.player.player = player
                        prepare()
                        addListener(listener)
                    }
                }

                speedMld.observe(this@VideoDisplayActivity){ speed ->
                    Log.d(TAG, "speedMld: speed = $speed")
                    player.setPlaybackSpeed(speed)
                }
                repeatModeMld.observe(this@VideoDisplayActivity){ mode ->
                    Log.d(TAG, "repeatModeMld: mode = $mode")
                    player.repeatMode = mode
                    customBinding.exoLoop?.isSelected = mode == ExoPlayer.REPEAT_MODE_ONE
                }
                playWhenReadyMld.observe(this@VideoDisplayActivity) { enabled ->
                    Log.d(TAG, "playWhenReadyMld: enabled = $enabled")
                    player.playWhenReady = enabled
                }

                playingTimeMld.observe(this@VideoDisplayActivity) { position ->
                    Log.d(TAG, "playingTimeMld position: $position")
                    player.seekTo(position)
                }
            }

            initialize(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
        viewModel.isInitialized = false
    }


    fun showPlaylist(playlistId: Int) {
        Logs.d(TAG, "showPlaylist() called with: playlistId = $playlistId")
        val dialog = ListedVideosDialog.newInstance(playlistId, object : OnSelectedChangeListener {
            override fun onSelectionChanged(videoId: Int) {
                Logs.d(TAG, "onSelectionChanged() called with: videoId = $videoId")
                viewModel.changeVideo(videoId)
            }
        })
        dialog.show(supportFragmentManager, TAG)
    }

//    fun loadingVideo() {
//        Log.d(TAG, "loadingVideo() called isInit = ${viewModel.isInitialized}")
////        if (!viewModel.isInitialized) {
////            Log.d(TAG, "loadingVideo: video is loaded!")
////            if(!viewModel.isRestoredState && settings.restoreState) {
////                showDialogToRestoreState(viewModel.getVideo())
////            }
////            return
////        }
//        viewModel.playlist =  arrayListOf()
//        viewModel.videoId = intent.getIntExtra(EXTRA_VIDEO_ID, -1)
//        viewModel.continued = intent.getBooleanExtra(EXTRA_CONTINUE, false)
//        viewModel.playWhenReadyMld.value = settings.autoPlay
//
////        // Reset position of video time down to zero and save the current position of video
////        val video = viewModel.getVideo()
////
////        if (viewModel.canShowDialog()) {
////            // Show dialog to restore state of video when restoreState from setting equals true
////            if(settings.restoreState) {
////                viewModel.videoMld.value = video.copy(playedTime = -1)
////                showDialogToRestoreState(video)
////            } else {
////                viewModel.videoMld.value = viewModel.getVideo().copy(playedTime = -1)
////            }
////            viewModel.isContinue = true
////        } else {
////            viewModel.videoMld.value = if(viewModel.videoMld.value == null)
////                viewModel.getVideo()
////            else viewModel.videoMld.value?.copy(updatedAt = System.currentTimeMillis())
////        }
//    }

    /**
     * Show a dialog and it purpose to restore video state
     * @param video Video info
     */
    private fun showDialogToRestoreState(video: Video) {
        Log.d(TAG, "showDialogToRestoreState() called")
        val binding = DialogDisplayAgainBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        binding.btnNo.setOnClickListener {
            viewModel.isRestoredState = true
            dialog.dismiss()
        }
        binding.btnYes.setOnClickListener {
            Log.d(TAG, "loadingVideo() playedTime=${video.playedTime}")
            viewModel.isRestoredState = true
            flagPlayingChanged = true
            player?.seekTo(video.playedTime)
            flagPlayingChanged = false
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            Log.d(TAG, "showDialogToRestoreState: setOnCancelListener")
            viewModel.isRestoredState = true
        }
        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        dialog.show()
    }

    private fun initialize(lanscapeScreen: Boolean) {
        Log.d(TAG, "initialize() called with: isLandscapeScreen = $lanscapeScreen")
        if (lanscapeScreen) {
            customBinding.exoLoop?.setOnClickListener(this)
            customBinding.exoLock?.setOnClickListener(this)
            customBinding.exoPrev?.setOnClickListener(this)
            customBinding.exoPlaybackSpeed?.setOnClickListener(this)
            customBinding.exoNext?.setOnClickListener(this)
            customBinding.exoPlayMusic?.setOnClickListener(this)
        } else {
            customBinding.exoOption?.setOnClickListener(this)
        }
        playerControlBinding.exoUnlock.setOnClickListener(this)
        customBinding.autoPlay.isChecked = settings.autoPlayMode
        customBinding.exoRotate.setOnClickListener(this)
        customBinding.exoBack.setOnClickListener(this)
        customBinding.autoPlay.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.autoPlayMode = isChecked
        }
        customBinding.btExpand.setOnClickListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logs.d(TAG, "onConfigurationChanged() called with: newConfig = $newConfig")
        hideSystemUi()
    }

    override fun onStart() {
        hideSystemUi()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        Logs.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Logs.d(TAG, "onPause: isInPictureInPictureMode=${isInPictureInPictureMode}")
        player?.stop()
    }

    override fun onStop() {
        super.onStop()
        Logs.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        viewModel.save()
    }

    override fun onClick(v: View) {
        Logs.d(TAG, "onClick() called with: v = $v")
        handler.sendEmptyMessage(v.id)
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Logs.d(TAG, "handleMessage() called with: msg = $msg")
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
                    playerControlBinding.layoutPlayerControlView.rootView.visibility = View.VISIBLE
                    playerControlBinding.layoutScrim.visibility = View.INVISIBLE
                }
                R.id.exo_lock -> {
                    playerControlBinding.layoutPlayerControlView.rootView.visibility = View.INVISIBLE
                    playerControlBinding.layoutScrim.visibility = View.VISIBLE
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
                        viewModel.repeatModeMld.value = ExoPlayer.REPEAT_MODE_ONE
                    } else {
                        viewModel.repeatModeMld.value = ExoPlayer.REPEAT_MODE_OFF
                    }
                }
                R.id.exo_playback_speed -> {
                    SpeedOptionFragment.newInstance(player?.playbackParameters?.speed ?: 1f,
                        object : SpeedOptionFragment.OnSelectedSpeedChangeListener {
                            override fun onSelectedSpeedChanged(speed: Float) {
                                Log.d(TAG, "onSelectedSpeedChanged() called with: speed = $speed")
                                viewModel.speedMld.value = speed
                            }
                        }
                    ).show(supportFragmentManager, TAG)
                }
                R.id.exo_next -> {
                    releasePlayer()
                    viewModel.next()
                    customBinding.exoPrev?.apply {
                        isClickable = true
                        setTextColor(resources.getColor(R.color.white))
                    }
                }
                R.id.exo_prev -> {
                    releasePlayer()
                    viewModel.previous()
                    if(!viewModel.hasPrev()) {
                        customBinding.exoPrev?.apply {
                            isClickable = false
                            setTextColor(resources.getColor(R.color.gray_20))
                        }
                    }
                }
                R.id.exo_play_music -> {
//                    val application = application as EGMApplication
//                    viewModel.getVideo().playedTime = player?.currentPosition ?: 0L
//                    application.iMusicalService()?.apply {
//                        setPlaylist(viewModel.videoId, viewModel.playlist)
//                        setKeepPlaying(true)
//                        setSpeed(viewModel.mLdSpeed.value!!)
//                        setRepeat(viewModel.mRepeatMode.value!!)
//                        play()
//                        finish()
//                    }
                }
                R.id.btExpand -> {
                    showPlaylist(viewModel.playlistId)
                }
                else -> {
                    Log.d(TAG, "onClick: not found ${msg.what}")
                }
            }
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // put flag inhere to prevent #onIsPlayingChanged
        flagPlayingChanged = true
        val value = super.dispatchTouchEvent(ev)
        flagPlayingChanged = false
        return value
    }

    private val listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition: mediaItem=$mediaItem")
            with(viewModel) {
                player?.stop()
                playWhenReadyMld.value = customBinding.autoPlay.isChecked
                viewModel.isResetPosition = true
                updatePosition(player.currentMediaItemIndex)
//                val lastIndex = player!!.currentMediaItemIndex - 1
//                if(lastIndex >= 0) {
//                    val video = playlist[lastIndex]
//                    video.playedTime = -1L
//                    videoRepo.update(video)
//                }
            }
            super.onMediaItemTransition(mediaItem, reason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            if(!flagPlayingChanged) {
                viewModel.playWhenReadyMld.value = isPlaying
                binding.player.keepScreenOn = isPlaying
                if(!isPlaying) {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                        "playbackState = $playbackState")
            if (playbackState == Player.STATE_BUFFERING) {
                viewModel.finishPlaying()
            } else if (playbackState == Player.STATE_READY) {
                viewModel.isFinished = false
            }
        }
    }

    private fun rotateScreenIfNeed(video: Video) {
        Log.d(TAG, "rotateScreenIfNeed() called with: video = $video")
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

    private fun setupPlayer(video: Video) {
        Log.d(TAG, "setupPlayer() called with: video = $video")
//        if(viewModel.isInitialized && settings.autoRotate) {
//            rotateScreenIfNeed(video)
//        }
        _player = ExoPlayer.Builder(this)

            .build()
            .also { exoPlayer ->

            }
        with(viewModel) {
            continued = true
            isInitialized = false
        }
    }

    private fun releasePlayer() {
        _player?.run {
            viewModel.videoMld.value?.playedTime = currentPosition
            removeListener(listener)
            release()
            binding.player.player = null
            Log.d(TAG, "releasePlayer() called video=${viewModel.videoMld.value}")
        }
        _player = null
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
        const val EXTRA_VIDEO_ID = "media.EXTRA_VIDEO_ID"
        const val EXTRA_CONTINUE = "media.EXTRA_CONTINUE"
        const val EXTRA_PLAYLIST = "media.EXTRA_PLAYLIST"
        val TAG: String = VideoDisplayActivity::class.java.simpleName

        fun newIntent(context: Context, videoId: Int, playlistId: Int = -1, continued: Boolean = false): Intent {
            return Intent(context, VideoDisplayActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_PLAYLIST, playlistId)
                putExtra(EXTRA_CONTINUE, continued)
            }
        }

    }
}