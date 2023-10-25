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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
    private val viewModel by viewModels<PersonalVideoViewModel>{
        viewModelFactory {
            initializer { PersonalVideoViewModel(appComponent.getVideoDao(), context!!.contentResolver) }
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
                val data = videos.sortedByCreatedDate(true)
                videoAdapter.submit(data)
            }
        }

        // Check permission of your app
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onViewCreated: loading...")
            viewModel.importVideos()
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
                    model.importVideos()
                }
            } else {
                activity.finish()
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

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

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach() called")
    }

    companion object {
        val TAG = PersonalVideoFragment::class.simpleName
        fun newInstance() = PersonalVideoFragment()
    }

}