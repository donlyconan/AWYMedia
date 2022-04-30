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
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.viewmodels.ListVideoViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Represent for Main Screen of app where app will shows all video list has on it
 */
class PersonalVideoFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ListVideoViewModel>()
    private lateinit var adapter: VideoAdapter
    @Inject lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        applicationComponent.inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                    "$savedInstanceState"
        )
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener(this)
        viewModel.getVideoList(settings.sortBy).observe(this) { videos ->
            adapter.submit(videos)
        }

        // Check permission of your app
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onViewCreated: loading...")
//            viewModel.let { model ->
//                model.insertDataIntoDbIfNeed()
//                model.viewModelScope.launch {
//                    model.sortBy(settings.sortBy)
//                    model.videoList
//                        .collectLatest(adapter::submitData)
//                }
//            }
        } else {
            Log.d(TAG, "onViewCreated: register permission!")
            requestPermissionResult.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }


    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)

        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) {
                when (v.id) {
                    R.id.btn_play -> {
                       val intent = VideoDisplayActivity.newIntent(requireContext(), video, false)
                        startActivity(intent)
                    }
                    R.id.btn_play_music -> {
                        application.iMusicalService()?.apply {
                            setVideoId(video.videoId!!)
                            play()
                        }
                    }
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        viewModel.update(video)
                        adapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {
                        viewModel.moveToTrash(video)
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video?.path))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    else -> {
                        Log.d(TAG, "onClick: actionId hasn't found!")
                    }
                }
            }
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, TAG)
        } else {
            val intent = VideoDisplayActivity.newIntent(requireContext(), video, false)
            startActivity(intent)
        }
    }


    private val requestPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.isNotEmpty()) {
//                viewModel.let { model ->
//                    model.insertDataIntoDbIfNeed()
////                    model.viewModelScope.launch {
////                        model.sortBy(settings.sortBy)
////                        model.videoList
////                            .collectLatest(adapter::submitData)
////                    }
//                }
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
                            if (viewModel.hasVideo(video.path)) {
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
//            R.id.btn_sort_by_name -> {
//                viewModel.apply {
//                    viewModelScope.launch {
//                        adapter.submitData(PagingData.empty())
//                        sortBy(Settings.SORT_BY_DURATION)
//                            .collectLatest(adapter::submitData)
//                    }
//                }
//            }
//            R.id.btn_sort_by_creation -> {
//                viewModel.apply {
//                    viewModelScope.launch {
//                        adapter.submitData(PagingData.empty())
//                        sortBy(Settings.SORT_BY_DURATION)
//                            .collectLatest(adapter::submitData)
//                    }
//                }
//            }
//            R.id.btn_sort_by_recent -> {
//                viewModel.apply {
//                    viewModelScope.launch {
//                        adapter.submitData(PagingData.empty())
//                        sortBy(Settings.SORT_BY_DURATION)
//                            .collectLatest(adapter::submitData)
//                    }
//                }
//            }
//            R.id.btn_sort_by_size -> {
//                viewModel.apply {
//                    viewModelScope.launch {
//                        adapter.submitData(PagingData.empty())
//                        sortBy(Settings.SORT_BY_DURATION)
//                            .collectLatest(adapter::submitData)
//                    }
//                }
//            }
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