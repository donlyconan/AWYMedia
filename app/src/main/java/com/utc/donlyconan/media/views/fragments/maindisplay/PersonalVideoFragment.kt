package com.utc.donlyconan.media.views.fragments.maindisplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import kotlin.math.log


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : ListVideosFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>{
        viewModelFactory {
            initializer { PersonalVideoViewModel(appComponent.getVideoRepository(), context!!.contentResolver) }
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                val data = videos.sortedByCreatedDate(true)
                videoAdapter.submit(data)
            }
        }

        // Check permission of your app
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.d(TAG, "onViewCreated: loading...")
            viewModel.importVideos()
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            requestPermissionIfNeed(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        }
        application.getAudioService()?.let { audioService ->
            audioService.registerPlayerListener(listener)
            binding.fab.isSelected = audioService.getPlayer()?.isPlaying == true
        }
    }


    override fun onPermissionResult(result: Map<String, Boolean>) {
        Log.d(TAG, "onPermissionResult() called with: result = $result")
        if (result.values.isNotEmpty()) {
            viewModel.importVideos()
        } else {
            activity.finish()
        }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            binding.fab.isSelected = isPlaying
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Log.d(TAG, "onMediaMetadataChanged() called with: mediaMetadata = $mediaMetadata")
            Glide.with(requireContext())
                .load(mediaMetadata.mediaUri)
                .into(binding.fab)

        }
    }


    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when (v.id) {
            R.id.fab -> {
                application.getAudioService()?.let { service ->
                    if(binding.fab.isSelected) {
                        service.stop()
                    } else {
                        service.start()
                    }
                }
            }
            R.id.btn_sort_by_name -> {
                settings.sortBy = Settings.SORT_BY_NAME
//                adapter.getData().sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                videoAdapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_creation -> {
                settings.sortBy = Settings.SORT_BY_CREATION
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                videoAdapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_recent -> {
                settings.sortBy = Settings.SORT_BY_RECENT
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                videoAdapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_duration -> {
                settings.sortBy = Settings.SORT_BY_DURATION
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                videoAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.getAudioService()?.removePlayerListener(listener)
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach() called")
    }

    companion object {
        val TAG = PersonalVideoFragment::class.simpleName
        fun newInstance() = PersonalVideoFragment()
    }

}