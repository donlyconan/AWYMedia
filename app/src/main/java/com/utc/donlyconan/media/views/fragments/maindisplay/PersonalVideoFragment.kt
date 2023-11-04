package com.utc.donlyconan.media.views.fragments.maindisplay

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.snackbar.Snackbar
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.services.MediaPlayerListener
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : ListVideosFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>{
        viewModelFactory {
            initializer { PersonalVideoViewModel(appComponent.getVideoRepository()) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        appComponent.inject(this)
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                    "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        videoAdapter = VideoAdapter(requireContext(), arrayListOf())
        videoAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = videoAdapter
        binding.fab.setOnClickListener(this)
        showLoadingScreen()
        viewModel.videosLd.observe(this) { videos ->
            Log.d(TAG, "onViewCreated() called with: video size = ${videos.size}")
            if(videos.isEmpty()) {
                showNoDataScreen()
                videoAdapter.submit(videos)
            } else {
                hideLoading()
                val data = videos.sortedByCreatedDate(settings.sortBy == Settings.SORT_VIDEO_BY_CREATION_DOWN)
                videoAdapter.submit(data)
            }
        }

        requestPermissions()
        application.getAudioService()?.let { audioService ->
            audioService.registerPlayerListener(listener)
            binding.fab.isSelected = audioService.getPlayer()?.isPlaying == true
        }
        binding.fab.setOnTouchListener(onTouchEvent)

    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions() called")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(checkPermission(Manifest.permission.READ_MEDIA_VIDEO)) {
                Log.d(TAG, "onViewCreated: loading...")
                application.getFileService()?.syncAllVideos()
            } else {
                requestPermissionIfNeed(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
        } else {
            if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.d(TAG, "onViewCreated: loading...")
                application.getFileService()?.syncAllVideos()
            } else {
                requestPermissionIfNeed(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
        }
    }


    override fun onPermissionResult(result: Map<String, Boolean>) {
        Log.d(TAG, "onPermissionResult() called with: result = $result")
        val granted = result.entries.count { v -> v.value }
        if (granted != 0) {
            application.getFileService()?.syncAllVideos()
        } else {
            Snackbar.make(view!!, "You need to allow permissions before using.", Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.OK) {
                    requestPermissionIfNeed(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                }.show()
        }
    }

    private val listener = object : MediaPlayerListener {

        override fun onInitialVideo(uri: Uri) {
            Log.d(TAG, "onInitialVideo() called with: uri = $uri")
            Glide.with(requireContext().applicationContext)
                .load(uri)
                .circleCrop()
                .into(binding.fab)
        }

        override fun onAudioServiceAvailable(available: Boolean) {
            Log.d(TAG, "onAudioServiceAvailable() called with: available = $available")
            binding.fab.visibility = if(available) View.VISIBLE else View.GONE
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            binding.fab.isSelected = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
        }

    }


    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when (v.id) {
            R.id.btn_sort_by_creation_up -> {
                settings.sortBy = Settings.SORT_VIDEO_BY_CREATION_UP
                viewModel.videosLd.value?.sortedByCreatedDate(false)?.let { data ->
                    videoAdapter.submit(data)
                    videoAdapter.notifyDataSetChanged()
                }
            }
            R.id.btn_sort_by_creation_down -> {
                settings.sortBy = Settings.SORT_VIDEO_BY_CREATION_DOWN
                viewModel.videosLd.value?.sortedByCreatedDate(true)?.let { data ->
                    videoAdapter.submit(data)
                    videoAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    protected val onTouchEvent = object : OnTouchListener {
        var expireTime = now()
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if(event?.action == MotionEvent.ACTION_UP) {
                if(expireTime < now() - 300) {
                    application.getAudioService()?.let { service ->
                        if(binding.fab.isSelected) {
                            service.stop()
                        } else {
                            service.start()
                        }
                    }
                } else {
                    requireActivity().sendBroadcast(Intent(AudioService.ACTION_REQUEST_OPEN_ACTIVITY))
                }
                expireTime = now()
            }
            return true
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        application.getAudioService()?.removePlayerListener(listener)
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach() called")
        application.getAudioService()?.removePlayerListener(listener)
    }

    companion object {
        val TAG = PersonalVideoFragment::class.simpleName
        fun newInstance() = PersonalVideoFragment()
    }

}