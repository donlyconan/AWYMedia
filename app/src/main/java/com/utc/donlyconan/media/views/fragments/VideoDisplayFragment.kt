package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.databinding.CustomOptionPlayerControlViewBinding
import com.utc.donlyconan.media.databinding.PlayerControlViewBinding
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.views.MainActivity
import com.utc.donlyconan.media.views.fragments.options.SpeedOptionFragment
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreFragment
import javax.inject.Inject


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
class VideoDisplayFragment : Fragment(), View.OnClickListener, MainActivity.ActivityEventHandler {
    private val viewModel by viewModels<VideoDisplayViewModel>()
    private val args by navArgs<VideoDisplayFragmentArgs>()
    private val activity by lazy { requireActivity() as MainActivity }
    private val window by lazy { requireActivity().window }
    private lateinit var binding: ActivityVideoDisplayBinding
    private lateinit var bindingOverlay: PlayerControlViewBinding
    private lateinit var bindingExtView: CustomOptionPlayerControlViewBinding
    private var player: ExoPlayer? = null
    private val isCrossCheck = false
    @Inject lateinit var settings: Settings
    @Inject lateinit var videoDao: VideoDao


    private val systemFlags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (context?.applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
        activity.registerActivityEventHandler(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        binding = ActivityVideoDisplayBinding.inflate(layoutInflater)
        bindingOverlay = PlayerControlViewBinding.bind(binding.root.findViewById(R.id.scrim_view))
        bindingExtView = CustomOptionPlayerControlViewBinding.bind(bindingOverlay.layoutPlayerControlView.rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onCreate: This Activity is created on portrait screen " +
                    "[${resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT}]")
        viewModel.video.observe(this) { video ->
            bindingExtView.headerTv.text = video.title
            val nextVideo = videoDao.getNextVideo(video.videoId)
            nextVideo?.let {
                val item = MediaItem.fromUri(Uri.parse(it.path))
                player?.addMediaItem(item)
            }
        }
        if (viewModel.video.value == null) {
            viewModel.video.value = args.video
        }
        initialize(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        Log.d(TAG, "onPictureInPictureModeChanged: ")
        if (isInPictureInPictureMode) {
            binding.videoView.hideController()
        } else {
            binding.videoView.showController()
        }
        if(lifecycle.currentState == Lifecycle.State.CREATED) {
            Log.d(TAG, "onPictureInPictureModeChanged: Lifecycle.State.CREATED")
            activity.finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: ")
        activity.enterPictureInPictureMode(createPictureInPictureParams())
        return false
    }


    private fun initialize(isLandscapeScreen: Boolean) {
        Log.d(TAG, "initialize() called with: isLandscapeScreen = $isLandscapeScreen")
        if (isLandscapeScreen) {
            bindingExtView.exoLoop?.setOnClickListener(this)
            bindingExtView.exoLock?.setOnClickListener(this)
            bindingExtView.exoSubtitle?.setOnClickListener(this)
            bindingExtView.exoPlaybackSpeed?.setOnClickListener(this)
            bindingExtView.exoNext?.setOnClickListener(this)
        } else {
            bindingExtView.exoOption?.setOnClickListener(this)
        }
        bindingOverlay.exoUnlock.setOnClickListener(this)
        bindingExtView.exoRotate.setOnClickListener(this)
        bindingExtView.exoBack.setOnClickListener(this)
        initializePlayer(viewModel.video.value!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPictureInPictureParams(): PictureInPictureParams {
        return if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
        } else {
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(9, 16))
                .build()
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
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: isInPictureInPictureMode=${requireActivity().isInPictureInPictureMode}")
        if (requireActivity().isInPictureInPictureMode) {
            player?.playWhenReady = true
        } else {
            player?.stop()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        showSystemUI()
        if (isCrossCheck) {
            releasePlayer()
            requireActivity().finish()
        }
    }

    override fun onDestroy() {
        releasePlayer()
        viewModel.saveVideoIfNeed()
        super.onDestroy()
        activity.unregisterActivityEventHandler(this)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                    if(args.backScreen == MainDisplayFragment.TAG) {
                        findNavController().navigate(
                            VideoDisplayFragmentDirections
                                .actionVideoDisplayFragmentToMainDisplayFragment()
                        )
                    } else {
                        findNavController().navigate(
                            VideoDisplayFragmentDirections
                                .actionVideoDisplayFragmentToDetailedPlaylistFragment(1)
                        )
                    }
                }
                R.id.exo_rotate -> {
                    requireActivity().requestedOrientation =
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
                    VideoMenuMoreFragment.newInstance(this@VideoDisplayFragment, enabled)
                        .show(requireActivity().supportFragmentManager, TAG)
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
                    ).show(requireActivity().supportFragmentManager, TAG)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            binding.videoView.hideController()
        } else {
            binding.videoView.showController()
        }
    }

    private val listener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(
                TAG, "onPlayerStateChanged() called with: playWhenReady = $playWhenReady, " +
                        "playbackState = $playbackState"
            )
            if (playbackState == Player.STATE_ENDED) {
                viewModel.video.value?.playedTime = 0
            }
        }
    }

    private fun initializePlayer(video: Video) {
        Log.d(TAG, "initializePlayer() called with: video = $video")
        player = ExoPlayer.Builder(requireContext())
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

    private fun showSystemUI() {
        Log.d(TAG, "showSystemUI() called")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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
        val TAG = VideoDisplayFragment::class.java.simpleName
    }
}