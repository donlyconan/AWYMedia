package com.utc.donlyconan.media.views

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.utc.donlyconan.media.databinding.ActivityVideoDisplayBinding
import com.utc.donlyconan.media.viewmodels.VideoDisplayViewModel
import com.utc.donlyconan.media.widget.createDataSource

class VideoDisplayActivity : AppCompatActivity() {

    private val binding by lazy { ActivityVideoDisplayBinding.inflate(layoutInflater) }
    private var player: SimpleExoPlayer? = null
    private val viewModel by viewModels<VideoDisplayViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.videoUri = Uri.parse(intent.extras!!.getString("videoUri"))
    }

    private fun initializePlayer() {
        Log.d(TAG, "initializePlayer() called videModel=$viewModel")
        binding.videoView.player = player
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                val mediaSource = viewModel.videoUri?.createDataSource(this)
                exoPlayer.seekTo(viewModel.currentWindow, viewModel.playbackPosition)
                exoPlayer.prepare(mediaSource!!, false, true)
                exoPlayer.playWhenReady = viewModel.playWhenReady
            }
    }

    private fun releasePlayer() {
        Log.d(TAG, "releasePlayer() called viewModel=$viewModel")
        player?.run {
            viewModel.playbackPosition = this.currentPosition
            viewModel.currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            stop()
            release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }


    companion object {
        val TAG = VideoDisplayActivity::class.java.simpleName
    }
}