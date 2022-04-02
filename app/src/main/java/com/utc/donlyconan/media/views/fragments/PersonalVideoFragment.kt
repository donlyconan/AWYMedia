package com.utc.donlyconan.media.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding
import com.utc.donlyconan.media.databinding.IncludeLoadingDataBinding
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.viewmodels.PersonalVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.showMessage
import com.utc.donlyconan.media.views.fragments.options.OptionBottomDialogFragment
import kotlinx.coroutines.launch


class PersonalVideoFragment : Fragment(), OnItemClickListener, View.OnClickListener {

    private val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PersonalVideoViewModel>()
    private val bindLoadingData by lazy { IncludeLoadingDataBinding.bind(view!!.findViewById(R.id.ll_loading)) }
    private lateinit var adapter: VideoAdapter
    private val videoDao by lazy {
        (requireContext().applicationContext as AwyMediaApplication).listVideoDao
    }


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
        bindLoadingData.llLoading1.visibility = View.VISIBLE
        adapter = VideoAdapter(context!!, ArrayList())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.videoList.observe(this) {
            Log.d(TAG, "onViewCreated() called observer size: " + it.size)
            adapter.submit(it)
            if (it.isEmpty()) {
                viewModel.insertVideosIntoDbIfNeed()
            } else {
                bindLoadingData.llLoading1.visibility = View.INVISIBLE
            }
        }
        binding.fab.setOnClickListener(this)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.videosList[position]
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
        val intent = Intent(context, VideoDisplayActivity::class.java)
        intent.putExtra(VideoDisplayActivity.KEY_VIDEO, video)
        startActivity(intent)
    }

    private val onItemClickListener =  object : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d(TAG, "onClick() called with: v = $v")
            when(v.id) {
                R.id.btn_play -> playVideo(viewModel.selectedVideo!!)
                R.id.btn_play_music -> {

                }
                R.id.btn_favorite -> {
                    viewModel.selectedVideo?.let { video ->
                        video.isFavorite = !v.isSelected
                        videoDao.updateVideo(video)
                    }
                }
                R.id.btn_delete -> {
                    viewModel.viewModelScope.launch {
                        videoDao.deleteVideo(viewModel.selectedVideo!!.videoId)
                    }
                }
                R.id.btn_share -> {

                }

                else -> {}
            }
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.data?.let { data ->
                    Log.d(TAG, "activityResultLauncher() called with: data = $data")
                    val videoId = data.toString().let {
                        it.substring(it.lastIndexOf('A') + 1).toInt()
                    }
                    if (!viewModel.repository.hasUrl(data.toString())) {
                        viewModel.viewModelScope.launch {
                            context?.contentResolver?.getAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                "${MediaStore.Video.Media._ID}=$videoId")?.first()?.let { video ->
                                Log.d(TAG, "activityResultLauncher() called video=$video")
                                viewModel.repository.insertVideo(video)
                            }
                        }
                    } else {
                        context?.showMessage("Video đã tồn tại.")
                    }
                }
            }
        };

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
        }
    }

    companion object {
        val TAG: String = PersonalVideoFragment.javaClass.simpleName
        fun newInstance() = PersonalVideoFragment()
    }

}