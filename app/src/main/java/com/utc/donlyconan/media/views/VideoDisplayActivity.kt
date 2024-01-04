package com.utc.donlyconan.media.views

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.MimeTypes
import com.google.common.collect.ImmutableList
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.app.utils.gone
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.app.utils.setEnabledState
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.views.fragments.options.SpeedOptionFragment
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreFragment
import com.utc.donlyconan.media.views.fragments.options.listedvideos.ListedVideosDialog
import com.utc.donlyconan.media.views.fragments.options.listedvideos.OnSelectedChangeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
class VideoDisplayActivity : BaseActivity(), View.OnClickListener,
    OnTouchListener, GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        const val EXTRA_VIDEO_ID = "media.EXTRA_VIDEO_ID"
        const val EXTRA_VIDEO_URI = "media.EXTRA_VIDEO_URI"
        const val EXTRA_CONTINUE = "media.EXTRA_CONTINUE"
        const val EXTRA_PLAYLIST = "media.EXTRA_PLAYLIST"
        val TAG: String = VideoDisplayActivity::class.java.simpleName

        fun newIntent(
            context: Context,
            videoId: Int,
            uri: String,
            playlistId: Int = -1,
            continued: Boolean = false
        ): Intent {
            return Intent(context, VideoDisplayActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_VIDEO_URI, uri)
                putExtra(EXTRA_PLAYLIST, playlistId)
                putExtra(EXTRA_CONTINUE, continued)
            }
        }

    }

    private val viewModel by viewModels<VideoDisplayViewModel> {
        viewModelFactory {
            initializer {
                val videoId = intent.getIntExtra(EXTRA_VIDEO_ID, -1)
                val playlistId = intent.getIntExtra(EXTRA_PLAYLIST, -1)
                val continued = intent.getBooleanExtra(EXTRA_CONTINUE, false)
                var videoRepo = appComponent.getVideoRepository()
                var playlistRepository = appComponent.getPlaylistRepository()
                var settings = appComponent.getSettings()
                VideoDisplayViewModel(videoId, playlistId, continued, videoRepo, playlistRepository, settings)
            }
        }
    }
    private val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private lateinit var playerControlBinding: PlayerControlViewBinding
    private lateinit var customBinding: CustomOptionPlayerControlViewBinding
    private val service: AudioService? by lazy { application.getAudioService() }
    private var touchedScreenTime: Long = 0L

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    private val player by lazy {
        val builder = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
        builder.build()
    }

    private val activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "handleMessage() called with: result = ${result.data}")
        if(result.resultCode == RESULT_OK && result.data != null) {
            moveJob?.cancel()
            result.data?.data?.let { uri ->
                viewModel.save(uri.toString())
                viewModel.playingTimeMld.value = player.currentPosition
            }
        }
    }

    lateinit var gestureDetector: GestureDetector
    private var expireBackTime: Long = 0
    private var flinging: Boolean = false
    private var moveJob: Job? = null
    lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check rotation if need before calling onCreate
        (applicationContext as EGMApplication).applicationComponent().inject(this)
        val videoUri = intent.getStringExtra(EXTRA_VIDEO_URI)
        if(videoUri != null && viewModel.settings.autoRotation && viewModel.shouldRotate) {
            requestRotationScreen(videoUri)
        }

        super.onCreate(savedInstanceState)

        // Set content view after checking
        setContentView(binding.root)
        service?.releasePlayer()

        playerControlBinding = PlayerControlViewBinding.bind(binding.root.findViewById(R.id.scrim_view))
        customBinding = CustomOptionPlayerControlViewBinding.bind(playerControlBinding.layoutPlayerControlView.rootView)

        binding.player.player = player
        gestureDetector = GestureDetector(this, this)
        scaleGestureDetector = ScaleGestureDetector(this, this)
        player.addListener(listener)

        with(viewModel) {
            videoMld.observe(this@VideoDisplayActivity) { video ->
                Logs.d(TAG, "video View Model: video=$video")
                binding.progressContainer.visibility = View.VISIBLE
                customBinding.headerTv.text = video.title
                val mediaItem = if(video.subtitleUri == null) {
                    MediaItem.fromUri(video.videoUri)
                } else {
                    generateMediaItem(video.videoUri, video.subtitleUri!!)
                }
                player.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    viewModel.playWhenReadyMld.value = true
                }
                flinging = false
            }
            speedMld.observe(this@VideoDisplayActivity) { speed ->
                Log.d(TAG, "speedMld: speed = $speed")
                player.setPlaybackSpeed(speed)
            }
            repeatModeMld.observe(this@VideoDisplayActivity) { mode ->
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
            playingIndexMld.observe(this@VideoDisplayActivity) { index ->
                val videos = playlistMld.value
                Log.d(TAG, "playingIndexMld: $index, has list = ${videos != null}")
                if(videos == null) {
                    customBinding.btnNext?.setEnabledState(false)
                    customBinding.btnPrev?.setEnabledState(false)
                } else if(index >= videos.size - 1) {
                    customBinding.btnNext?.setEnabledState(false)
                } else if(index <= 0) {
                    customBinding.btnPrev?.setEnabledState(false)
                } else {
                    customBinding.btnNext?.setEnabledState(true)
                    customBinding.btnPrev?.setEnabledState(true)
                }
                moveJob?.cancel()
            }
            playlistMld.observe(this@VideoDisplayActivity) { videos ->
                customBinding.autoPlay.visibility = if(isListMode()) View.VISIBLE else View.GONE
            }
            resizeModeMdl.observe(this@VideoDisplayActivity) {mode ->
                Log.d(TAG, "resizeModeMdl called with: mode = $mode")
                binding.player.resizeMode = mode
                binding.player.hideController()
            }
            events.observe(this@VideoDisplayActivity) { event ->
                when(event) {
                    VideoDisplayViewModel.Result.CanNotMoveNext -> {
                        showMessage(R.string.di_chuy_n_h_t_danh_s_ch)
                        flinging = false
                    }
                    VideoDisplayViewModel.Result.CanNotMovePrevious -> {
                        showMessage(R.string.di_chuy_n_h_t_danh_s_ch)
                        flinging = false
                    }
                    else -> null
                }
            }
        }
        initializeViews()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
        viewModel.isInitialized = false
    }

    // Generate a media item with a subtitle preparing
    private fun generateMediaItem(videoUri: String, subtitleUri: String): MediaItem {
        Log.d(TAG, "generateMediaItem() called with: videoUri = $videoUri, subtitleUri = $subtitleUri")
        val subtitle = subtitleUri.let {
            SubtitleConfiguration.Builder(subtitleUri.toUri())
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }
        val builder = MediaItem.Builder()
            .setUri(videoUri)
        builder.setSubtitleConfigurations(ImmutableList.of(subtitle))
        return builder.build()
    }


    private fun showPlaylist(playlistId: Int) {
        Logs.d(TAG, "showPlaylist() called with: playlistId = $playlistId")
        val currentState = player.playWhenReady
        val dialog = ListedVideosDialog.newInstance(playlistId, viewModel.playingIndexMld.value!!, object : OnSelectedChangeListener {
            override fun onSelectionChanged(videoId: Int) {
                Logs.d(TAG, "onSelectionChanged() called with: videoId = $videoId")
                player.playWhenReady = currentState
                viewModel.replaceVideo(videoId)
            }

            override fun onBackPress() {
                Log.d(TAG, "onBackPress() called")
                requestExist()
            }
        })
        dialog.show(supportFragmentManager, TAG)
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.currentPlayWhenReadyState) {
            player.play()
        }
        Logs.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Logs.d(TAG, "onPause: isInPictureInPictureMode=${isInPictureInPictureMode}")
        viewModel.currentPlayWhenReadyState = player.playWhenReady
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        Logs.d(TAG, "onStop: ")
        val position = player.currentPosition
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.save(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }


    private fun initializeViews() {
        val landscapeMode = requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val listener = this@VideoDisplayActivity
        Log.d(TAG, "initialize() called with: landscapeMode = $landscapeMode")
        with(customBinding) {
            exoLoop?.setOnClickListener(listener)
            exoLock.setOnClickListener(listener)
            btnNext?.setOnClickListener(listener)
            exoPlaybackSpeed?.setOnClickListener(listener)
            btnPrev?.setOnClickListener(listener)
            exoPlayMusic?.setOnClickListener(listener)
            exoSubtitles?.setOnClickListener(listener)
            exoOption?.setOnClickListener(listener)
            playerControlBinding.exoUnlock.setOnClickListener(listener)
            autoPlay.isChecked = settings.autoPlayMode
            exoRotate.setOnClickListener(listener)
            exoBack.setOnClickListener(listener)
            autoPlay.setOnCheckedChangeListener { buttonView, isChecked ->
                settings.autoPlayMode = isChecked
            }
            autoPlay.visibility = if(viewModel.isListMode()) View.VISIBLE else View.GONE
        }
        binding.progressContainer.setOnTouchListener { v, event -> true }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logs.d(TAG, "onConfigurationChanged() called with: newConfig = $newConfig")
    }


    override fun onClick(v: View) {
        Logs.d(TAG, "onClick() called with: v = $v")
        handleMessage(msgId = v.id)
    }

    private fun handleMessage(msgId: Int) {
        Log.d(TAG, "handleMessage() called with: msgId = $msgId")
        when (msgId) {
            R.id.exo_back -> finish()
            R.id.exo_rotate -> {
                viewModel.playingTimeMld.value = player.currentPosition
                viewModel.playWhenReadyMld.value = player.playWhenReady
                requestedOrientation =
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    else
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            R.id.exo_unlock -> {
                playerControlBinding.layoutPlayerControlView.rootView.visibility = View.VISIBLE
                playerControlBinding.layoutScrim.visibility = View.INVISIBLE
                viewModel.lockModeMdl.value = false
            }

            R.id.exo_lock -> {
                playerControlBinding.layoutPlayerControlView.rootView.visibility = View.INVISIBLE
                playerControlBinding.layoutScrim.visibility = View.VISIBLE
                viewModel.lockModeMdl.value = true
            }

            R.id.exo_option -> {
                val enabled = this.player.repeatMode != ExoPlayer.REPEAT_MODE_OFF
                val hasNext = viewModel.hasNext()
                val hasPrev = viewModel.hasPrev()
                if(!supportFragmentManager.isDestroyed) {
                    VideoMenuMoreFragment.newInstance(enabled, hasNext, hasPrev) { v ->
                        handleMessage(v.id)
                    }.show(supportFragmentManager, TAG)
                }
            }

            R.id.exo_loop -> {
                val status = this.player.repeatMode == ExoPlayer.REPEAT_MODE_OFF
                if (status) {
                    viewModel.repeatModeMld.value = ExoPlayer.REPEAT_MODE_ONE
                } else {
                    viewModel.repeatModeMld.value = ExoPlayer.REPEAT_MODE_OFF
                }
            }

            R.id.exo_playback_speed -> {
                if (!supportFragmentManager.isDestroyed) {
                    SpeedOptionFragment.newInstance(this.player?.playbackParameters?.speed ?: 1f,
                        object : SpeedOptionFragment.OnSelectedSpeedChangeListener {
                            override fun onSelectedSpeedChanged(speed: Float) {
                                Log.d(TAG, "onSelectedSpeedChanged() called with: speed = $speed")
                                viewModel.speedMld.value = speed
                            }

                            override fun onReselectedSpeed(speed: Float) {
                            }
                        }).show(supportFragmentManager, TAG)
                }
            }

            R.id.btn_next -> {
                viewModel.moveNext()
            }

            R.id.btn_prev -> {
                viewModel.movePrevious()
            }

            R.id.exo_play_music -> {
                val position = player.currentPosition
                val repeatMode = player.repeatMode
                val speed = viewModel.speedMld.value!!
                val index = viewModel.playingIndexMld.value!!
                lifecycleScope.launch {
                    var playlist = viewModel.playlist?.map { it.videoUri }?.toTypedArray()
                    if (playlist.isNullOrEmpty() || repeatMode == Player.REPEAT_MODE_OFF) {
                        playlist = arrayOf(viewModel.videoMld.value!!.videoUri)
                    }
                    val mediaItems = playlist.map { uri -> MediaItem.fromUri(uri) }
                    application.getAudioService()?.play(mediaItems, index, repeatMode, speed, position)
                    runOnUiThread {
                        finish()
                    }
                }
            }
            R.id.exo_subtitles -> {
                player.pause()
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    data = androidFile(Environment.DIRECTORY_DCIM).toUri()
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/x-subrip"));
                }
                activityResult.launch(intent)
            }
            else -> {
                Log.d(TAG, "onClick: $msgId not found the action, please check again")
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // when loading that every thing can be disabled
        if(binding.progressContainer.isVisible) {
            return true
        }
        // put flag inhere to prevent #onIsPlayingChanged
        val value = super.dispatchTouchEvent(ev)
        if(!viewModel.lockModeMdl.value!!) {
            gestureDetector.onTouchEvent(ev)
            scaleGestureDetector.onTouchEvent(ev)
        }
        touchedScreenTime = now()
        return value
    }

    private val listener = object : Player.Listener {

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            Log.d(TAG, "onMediaMetadataChanged() called with: mediaMetadata = $mediaMetadata")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition: mediaItem=$mediaItem")
            with(viewModel) {
                playWhenReadyMld.value = customBinding.autoPlay.isChecked
            }
            super.onMediaItemTransition(mediaItem, reason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            binding.player.keepScreenOn = isPlaying
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                        "playbackState = $playbackState")
            if(playbackState == Player.STATE_IDLE) {
                if(playWhenReady && player.repeatMode != ExoPlayer.REPEAT_MODE_OFF) {
                    Log.d(TAG, "onPlayerStateChanged: reset by repeat mode.")
                    player.playWhenReady = true
                    player.prepare()
                }
            }
            if(playbackState == Player.STATE_READY) {
                binding.progressContainer.visibility = View.GONE
            }
            viewModel.isFinished = playbackState == Player.STATE_ENDED
            if (playWhenReady && playbackState == Player.STATE_ENDED && !flinging) {
                val autoPlay = customBinding.autoPlay.isChecked && customBinding.autoPlay.isVisible
                Log.d(TAG, "onPlayerStateChanged: move next video? ${autoPlay}!")
                moveJob = lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.save(0L)
                    if (autoPlay) {
                        delay(1000)
                        viewModel.moveNext()
                    }
                }
            }
        }
    }

    private fun requestRotationScreen(uri: String) {
        Log.d(TAG, "requestRotationScreen() called with: uri = $uri")
        viewModel.shouldRotate = false
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri.toUri())
            val orientation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toInt()
                    ?.also { orientation ->
                        if (orientation == 0 && resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
            Log.d(TAG, "initializePlayer: orientation=$orientation")
        } catch (e: Exception) {
            Log.e(TAG, "requestRotationScreen: ", e)
        }
    }

    private fun releasePlayer() {
        Log.d(TAG, "releasePlayer() called")
        player.run {
            viewModel.videoMld.value?.playedTime = currentPosition
            removeListener(listener)
            stop()
            release()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true
    }


    override fun onDown(e: MotionEvent): Boolean {
        Log.d(TAG, "GestureDetector#onDown() called with: e = $e")
        if(!viewModel.isListMode()) {
            return true
        }
        // location in x must be under 50 and time
        val land = requestedOrientation == Configuration.ORIENTATION_LANDSCAPE
        val requestDistance = if(land) 120 else 50
        if(e.x < requestDistance && e.y > 100 && viewModel.isListMode()) {
            showPlaylist(viewModel.playlistId)
            expireBackTime = 0L
            return true
        }
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        Log.d(TAG, "GestureDetector#onShowPress() called with: e = $e")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d(TAG, "GestureDetector#onSingleTapUp() called with: e = $e")
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d(TAG, "GestureDetector#onLongPress() called with: e = $e")
        handleMessage(R.id.exo_option)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "GestureDetector#onFling() called with: e1 = $e1, e2 = $e2, velocityX = $velocityX, velocityY = $velocityY")
        binding.player.hideController()
        // share video if y = 0 and velocityX > 4000
        if(abs(velocityX) > 4000 && e2.x.toInt() >= Resources.getSystem().displayMetrics.widthPixels - 40) {
            if(application.getFileService()?.isReadyService() == true) {
                viewModel.video?.let { video ->
                    video.playedTime = player.currentPosition
                    application.getFileService()?.send(video)
                }
                showMessage(R.string.the_video_was_shared)
            } else {
                showMessage(R.string.the_connection_haven_t_established_yet)
            }
            return true
        }
        if(!viewModel.isListMode() || e1.x < 30) {
            Log.d(TAG, "Fling is not available in the single mode")
            return true
        }
        // velocity is above -4000
        if(abs(velocityX) > 4000 && viewModel.isListMode()) {
            if (e2.x - e1.x > 200) {
                Log.d(TAG, "onFling: Move next")
                flinging = true
                viewModel.continued = true
                viewModel.saveTempState(player.currentPosition)
                viewModel.moveNext()
            }
            if(e1.x - e2.x > 200){
                Log.d(TAG, "onFling: Move down")
                flinging = true
                viewModel.continued = true
                viewModel.saveTempState(player.currentPosition)
                viewModel.movePrevious()
            }

        }
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        Log.d(TAG, "GestureDetector#onSingleTapConfirmed() called with: e = $e")
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        Log.d(TAG, "GestureDetector#onDoubleTap() called with: e = $e")
        if(touchedScreenTime >= now() - 1000) {
            handleMessage(R.id.exo_rotate)
        }
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return true
    }


    override fun onBackPressed() {
        requestExist()
    }

    private fun requestExist() {
        Log.d(TAG, "requestExist() called allowBackPress=$${expireBackTime >= now()}")
        if (expireBackTime >= now()) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            showMessage(getString(R.string.back_again_to_exit))
            expireBackTime = now() + 2000
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if(detector.scaleFactor > 1.0f) {
            viewModel.resizeModeMdl.value = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            viewModel.resizeModeMdl.value = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) { }

    override fun finish() {
        Log.d(TAG, "finish() called main activity is working = ${MainActivity.isWorking}")
        if(!MainActivity.isWorking) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            super.finish()
        }
    }
}