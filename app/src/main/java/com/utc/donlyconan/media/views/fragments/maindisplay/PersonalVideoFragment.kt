package com.utc.donlyconan.media.views.fragments.maindisplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import javax.inject.Inject


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : ListVideoFragment(), View.OnClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>()
    @Inject lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        applicationComponent.inject(this)
        lBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                    "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(requireContext(), arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)
        showLoadingScreen()
        viewModel.lstVideos.observe(this) { videos ->
            if(videos.isEmpty()) {
                showNoDataScreen()
                adapter.submit(videos)
            } else {
                val sortedVideos = ArrayList(videos).apply {
                    sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                }
                hideLoading()
                adapter.submit(sortedVideos)
            }
        }

        // Check permission of your app
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onViewCreated: loading...")
            viewModel.insertVideoIfNeed()
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            requestPermissionResult.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
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
                    importData(listItems)
                }
                intent?.data?.let { uri ->
                    importData(arrayListOf(uri.toString()))
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
            context?.showMessage("Video đã tồn tại.")
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
                adapter.videoList.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_creation -> {
                settings.sortBy = Settings.SORT_BY_CREATION
                adapter.videoList.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_recent -> {
                settings.sortBy = Settings.SORT_BY_RECENT
                adapter.videoList.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
                adapter.notifyDataSetChanged()
            }
            R.id.btn_sort_by_duration -> {
                settings.sortBy = Settings.SORT_BY_DURATION
                adapter.videoList.sortWith { u, v -> u.compareTo(v, settings.sortBy) }
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