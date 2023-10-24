package com.utc.donlyconan.media.views.fragments.maindisplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : ListVideosFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>()

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
        adapter = VideoAdapter(requireContext(), arrayListOf())
        adapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)
        showLoadingScreen()
        viewModel.lstVideos.observe(this) { videos ->
            Log.d(TAG, "onViewCreated() called with: video size = ${videos.size}")
            if(videos.isEmpty()) {
                showNoDataScreen()
                val data = viewModel.sortedByTime(videos)
                adapter.submit(data)
            } else {
                hideLoading()
                val data = videos.sortedByCreatedDate(true)
                adapter.submit(data)
            }
        }

        // Check permission of your app
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onViewCreated: loading...")
            viewModel.insertVideoIfNeed()
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                requestPermissionResult.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            } else {
                requestPermissionResult.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_VIDEO,
                    )
                )
            }
        }
    }

    private val requestPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.isNotEmpty()) {
                viewModel.let { model ->
                    model.insertVideoIfNeed()
                }
            } else {
                activity.finish()
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.clipData?.let { listUri ->
                    val listItems = arrayListOf<String>()
                    for (i in 0 until listUri.itemCount) {
                        listItems.add(listUri.getItemAt(i).uri.toString())
                    }
                    try {
                        importData(listItems)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        context?.showMessage(e.message.toString())
                    }
                }
                intent?.data?.let { uri ->
                    try {
                        importData(arrayListOf(uri.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        context?.showMessage(e.message.toString())
                    }
                }
            }
        }

    private fun importData(videoIdStrs: ArrayList<String>) {
        Log.d(TAG, "importData() called with: videoIds = $videoIdStrs")
        var count = 0
        for (idStr in videoIdStrs) {
            val videoId = idStr.let {
                val start = it.lastIndexOf('A') + 1
                var end = it.lastIndexOf(' ')
                if(end == -1) {
                    end = it.length
                }
                it.substring(start, end).toInt()
            }
            val video = context?.contentResolver?.getAllVideos(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Video.Media._ID}=$videoId"
            )?.first()
            if (video != null) {
                Log.d(TAG, "activityResultLauncher() called video=$video")
                if (viewModel.hasVideo(video.path)) {
                    count++
                } else {
                    viewModel.videoRepo.insert(video)
                }
            }
        }
        if (count == videoIdStrs.size) {
            context?.showMessage(R.string.video_exist_des)
        }
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when (v.id) {
            R.id.fab -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "video/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                activityResultLauncher.launch(intent)
            }
            R.id.btn_sort_by_name -> {
                settings.sortBy = Settings.SORT_BY_NAME
//                adapter.getData().sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_creation -> {
                settings.sortBy = Settings.SORT_BY_CREATION
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_recent -> {
                settings.sortBy = Settings.SORT_BY_RECENT
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_duration -> {
                settings.sortBy = Settings.SORT_BY_DURATION
//                adapter.videos.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
        }
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