package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding

//import com.utc.donlyconan.media.databinding.FragmentVideoDisplayBinding


class VideoDisplayFragment : Fragment() {

    val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private var player: ExoPlayer? = null
    private val args by navArgs<VideoDisplayFragmentArgs>()
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initializePlayer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            TAG,
            "onViewCreated() called with: view = $view, savedInstanceState = $savedInstanceState"
        )
    }

    private fun initializePlayer() {
        Log.d(TAG, "initializePlayer() called uri=" + args.videoUri)
        player = ExoPlayer.Builder(context!!)
            .build()
            .also { exoPlayer ->
//                binding.videoView.player = exoPlayer
//                val mediaItem = MediaItem.fromUri(args.videoUri)
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.seekTo(currentWindow, playbackPosition)
//                exoPlayer.prepare()
//                exoPlayer.playWhenReady = playWhenReady
            }
    }


    private fun releasePlayer() {
        Log.d(TAG, "releasePlayer() called player=$player")
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            stop()
            release()
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        Log.d(TAG, "hideSystemUi() called")
        binding.videoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }


    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        initializePlayer()
    }

    override fun onPause() {
        player?.stop()
        super.onPause()
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    companion object {
        val TAG = VideoDisplayFragment::class.java.simpleName
    }
}