package com.utc.donlyconan.media.views.fragments.maindisplay

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.localinteraction.Client
import com.utc.donlyconan.media.app.localinteraction.EGPMediaClient
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.app.services.MediaPlayerListener
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.AlertDialogManager
import com.utc.donlyconan.media.app.utils.compareUri
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
import com.utc.donlyconan.media.app.utils.toBannerNumber
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.OnItemLongClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.InteractionManagerFragment
import java.net.InetAddress


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : ListVideosFragment(), View.OnClickListener, OnItemClickListener,
    OnItemLongClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>{
        viewModelFactory {
            initializer {
                PersonalVideoViewModel(
                    appComponent.getVideoRepository(),
                    appComponent.getTrashRepository()
                )
            }
        }
    }
    override val listView: RecyclerView
        get() = binding.recyclerView

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
        videoAdapter.setOnLongClickListener(this)
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
                    .apply {
                        application.getFileService()?.getDownloadingItems()?.let { videos ->
                            filterIsInstance<Video>().forEach { video ->
                                video.available = !videos.any { it.videoUri == video.videoUri }
                            }
                        }
                    }
                videoAdapter.submit(data)
            }
        }

        requestPermissions()
        binding.fab.setOnTouchListener(onTouchEvent)
        viewModel.numberOfTrash.observe(this) { quantity ->
            binding.fabTrash.isSelected = quantity > 0
            binding.tvTrashCount.text = quantity.toBannerNumber()
        }
        binding.fabTrash.setOnClickListener {
            val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToTrashFragment()
            findNavController().navigate(action)
        }
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions() called")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intentLanucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d(TAG, "requestPermissions() called with: result = $result")
               if(Environment.isExternalStorageManager()) {
                   application.getFileService()?.syncAllVideos()
               } else if(!isDetached) {
                   Snackbar.make(view!!, "You need to allow permissions before using.", Snackbar.LENGTH_INDEFINITE)
                       .setAction(R.string.OK) {
                           requestPermissions()
                       }.show()
               }
            }
            if(!Environment.isExternalStorageManager()) {
                AlertDialogManager.createDeleteAlertDialog(
                    context = requireContext(),
                    title = getString(R.string.app_name),
                    msg = "We are need you to provide file access permission for the app. Let's tab ${getString(R.string.app_name)}'s  checkbox.",
                    onAccept = {
                        val permissionIntent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        intentLanucher.launch(permissionIntent)
                    },
                    onDeny = {
                        requireActivity().finish()
                    }
                ).show()
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
                    requestPermissions()
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
            runOnUIThread {
                binding.fab.visibility = if(available) View.VISIBLE else View.GONE
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged() called with: isPlaying = $isPlaying")
            binding.fab.isSelected = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            Log.d(TAG, "onMediaItemTransition() called with: mediaItem = $mediaItem, reason = $reason")
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            Log.d(TAG, "onPlayerErrorChanged() called with: error = $error")
            runOnUIThread {
                binding.fab.visibility = View.GONE
            }
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        application.getAudioService()?.let { audioService ->
            audioService.registerPlayerListener(listener)
            binding.fab.isSelected = audioService.getPlayer()?.isPlaying == true
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
        application.getAudioService()?.removePlayerListener(listener)
    }


    companion object {
        val TAG = PersonalVideoFragment::class.simpleName
        fun newInstance() = PersonalVideoFragment()
    }

    override fun onItemLongClick(v: View, position: Int) {
        Log.d(TAG, "onItemLongClick() called with: v = $v, position = $position")
        (videoAdapter.getItem(position) as? Video)?.let { video ->
            openMenuMore(video, position)
        }
    }

    override fun onEgpConnectionChanged(isConnected: Boolean, isGroupOwner: Boolean) {
        Log.d(TAG, "onEgpConnectionChanged() called with: isConnected = $isConnected, isGroupOwner = $isGroupOwner")
        super.onEgpConnectionChanged(isConnected, isGroupOwner)
        runOnUIThread {
            binding.llPairGroup.visibility = if(isConnected) View.VISIBLE else View.GONE
        }
    }

    override fun onDeviceNameUpdated(deviceName: String?) {
        updateDeviceName(deviceName)
    }

    private fun updateDeviceName(deviceName: String?) {
        Log.d(TAG, "updateDeviceName() called with: deviceName = $deviceName")
        runOnUIThread {
            binding.tvDeviceName.text = deviceName ?: getString(R.string.unknown)
        }
    }

    override fun onClientConnectionChanged(clients: List<Client>) {
        Log.d(TAG, "onClientConnectionChanged() called with: clients = $clients")
        super.onClientConnectionChanged(clients)
        application.getFileService()?.egmSystem?.listClients?.lastOrNull()?.name.let { name ->
            updateDeviceName(name)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(InteractionManagerFragment.TAG, "onActivityResult() called with: requestCode = $requestCode, resultCode = $resultCode, data = $data")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val ipAddress = result?.contents?.substringAfterLast('/')
        if(ipAddress != null) {
            Log.d(InteractionManagerFragment.TAG, "onActivityResult: IPAddress=$ipAddress")
            application.getFileService()?.let { service ->
                service.closeEgpSystem()
                service.openEgmService(EGPMediaClient::class, InetAddress.getByName(ipAddress))
            }
        }
    }

}