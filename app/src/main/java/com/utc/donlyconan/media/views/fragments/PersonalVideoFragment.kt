package com.utc.donlyconan.media.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : Fragment(), OnItemClickListener, View.OnClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>()
    private lateinit var adapter: VideoAdapter
    private val application by lazy { context?.applicationContext as AwyMediaApplication }
    @Inject lateinit var settings: Settings
    @Inject lateinit var videoDao: VideoDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        application.applicationComponent().inject(this)
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
                        .collectLatest(adapter::submitData)
                }
            }
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            requestPermissionResult.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }



    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        viewModel.selectedVideo = video
        if (v.id == R.id.cb_selected) {
            VideoMenuMoreDialogFragment.newInstance(video, onItemClickListener)
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
                    application.iMusicalService()?.apply {
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
                    viewModel.selectedVideo?.let { video ->
                        video.deletedAt = System.currentTimeMillis() + MS_TO_30DAY
                        videoDao.update(video)
                    }
                }
                R.id.btn_share -> {
                    val video = viewModel.selectedVideo
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "video/*"
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video?.path))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                    startActivity(Intent.createChooser(intent, "Share File"))
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
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_creation -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_recent -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .collectLatest(adapter::submitData)
                    }
                }
            }
            R.id.btn_sort_by_size -> {
                viewModel.apply {
                    viewModelScope.launch {
                        adapter.submitData(PagingData.empty())
                        sortBy(Settings.SORT_BY_DURATION)
                            .collectLatest(adapter::submitData)
                    }
                }
            }
        }
    }

    companion object {
        const val REQ_READ_WRITE_PERMISSION = 100
        const val MS_TO_30DAY = 30 * 24 * 60 * 60 * 1000 // day * hour * min * sec * ms
        fun newInstance() = PersonalVideoFragment()
    }

}