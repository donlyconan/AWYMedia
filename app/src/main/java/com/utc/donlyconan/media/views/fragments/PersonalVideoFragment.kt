package com.utc.donlyconan.media.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.IncludeLoadingDataBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.OptionBottomDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import wseemann.media.FFmpegMediaMetadataRetriever


class PersonalVideoFragment : Fragment(), OnItemClickListener, View.OnClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>()
    private val loadingBinder by lazy { IncludeLoadingDataBinding.bind(view!!.findViewById(R.id.ll_loading)) }
    private lateinit var adapter: VideoAdapter
    private val application by lazy { context?.applicationContext as? AwyMediaApplication }
    val settings by lazy { Settings.getInstance(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                    "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)

        // Check permission of your app
        if(ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onViewCreated: loading...")
            viewModel.let { model ->
                model.insertDataIntoDbIfNeed()
                model.viewModelScope.launch {
                    model.sortBy(settings.sortBy)
                    model.videoList
                        .onCompletion {
                            showHideProgress()
                        }
                        .collectLatest(adapter::submitData)
                }
            }
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            requestPermissionResult.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun showHideProgress() {
        Log.d(TAG, "showHideProgress() called")
        loadingBinder.frameContainer.visibility = View.VISIBLE
        if(adapter.snapshot().isEmpty()) {
            loadingBinder.tvNoData.visibility = View.VISIBLE
            loadingBinder.llLoading1.visibility = View.INVISIBLE
        } else {
            loadingBinder.tvNoData.visibility = View.VISIBLE
            loadingBinder.llLoading1.visibility = View.VISIBLE
            loadingBinder.frameContainer.visibility = View.GONE
        }
    }

    private fun loadVideoInfo(url: String): Video {
        val fFmpegMediaMetadataRetriever = MediaMetadataRetriever()
        fFmpegMediaMetadataRetriever.setDataSource(url)
        val title = fFmpegMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val duration = fFmpegMediaMetadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
        val type = fFmpegMediaMetadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        return Video(-1L, title, url, duration, 0L, type, 0L, System.currentTimeMillis())
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        viewModel.selectedVideo = video
        if (v.id == R.id.img_menu_more) {
            OptionBottomDialogFragment.newInstance(video, onItemClickListener)
                .show(parentFragmentManager, TAG)
        } else {
            playVideo(video)
        }
    }

    private fun playVideo(video: Video) {
        Log.d(TAG, "playVideo() called with: video = $video")
        val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToVideoDisplayFragment(video)
        findNavController().navigate(action)
    }

    private val onItemClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d(TAG, "onClick() called with: v = $v")
            when (v.id) {
                R.id.btn_play -> playVideo(viewModel.selectedVideo!!)
                R.id.btn_play_music -> {
                    application?.iMusicalService?.apply {
                        setVideoId(viewModel.selectedVideo?.videoId!!)
                        play()
                    }
                }
                R.id.btn_favorite -> {
                    viewModel.apply {
                        selectedVideo?.let { video ->
                            video.isFavorite = !v.isSelected
                            videoRepo.update(video)
                        }
                    }
                }
                R.id.btn_delete -> {
                    viewModel.apply {
                        viewModelScope.launch {
                            videoRepo.delete(viewModel.selectedVideo!!.videoId)
                        }
                    }
                }
                R.id.btn_share -> {

                }
                else -> {
                }
            }
        }
    }

    private val requestPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.isNotEmpty()) {
                viewModel.let { model ->
                    model.insertDataIntoDbIfNeed()
                    model.viewModelScope.launch {
                        model.sortBy(settings.sortBy)
                        model.videoList
                            .onCompletion { showHideProgress() }
                            .collectLatest(adapter::submitData)
                    }
                }
            } else {
                activity?.finish()
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.data?.let { data ->
                    Log.d(TAG, "activityResultLauncher() called with: data = $data")
                    val videoId = data.toString().let {
                        it.substring(it.lastIndexOf('A') + 1).toInt()
                    }
                    viewModel.viewModelScope.launch {
                        val video = context?.contentResolver?.getAllVideos(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            "${MediaStore.Video.Media._ID}=$videoId"
                        )?.first()
                        if (video != null) {
                            Log.d(TAG, "activityResultLauncher() called video=$video")
                            if (viewModel.videoRepo.countPath(video.path) != 0) {
                                context?.showMessage("Video đã tồn tại.")
                            } else {
                                viewModel.videoRepo.insert(video)
                            }
                        }
                    }

                }
            }
        }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when (v.id) {
            R.id.fab -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "video/*"
                    action = Intent.ACTION_GET_CONTENT
                }
                activityResultLauncher.launch(intent)
            }
            R.id.btn_sort_by_name -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .onEach {
                                showHideProgress()
                            }
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_creation -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .onCompletion {
                                showHideProgress()
                            }
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_recent -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .onEach {
                                showHideProgress()
                            }
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_size -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .onEach {
                                showHideProgress()
                            }
                            .collectLatest(adapter::submitData)
                    }
                }
            }
        }
    }

    companion object {
        const val REQ_READ_WRITE_PERMISSION = 100

        fun newInstance() = PersonalVideoFragment()
    }

}