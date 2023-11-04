package com.utc.donlyconan.media.views.fragments.maindisplay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.sortedByUpdatedDate
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentRecentBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.RecentVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  This is Recent screen which will show all video is playing
 */
class RecentFragment : ListVideosFragment() {
    val binding by lazy { FragmentRecentBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<RecentVideoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        hideViews.add(R.id.btn_unlock)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: ")
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        videoAdapter = VideoAdapter(context!!, arrayListOf(), true)
        videoAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = videoAdapter
        showLoadingScreen()
        viewModel.videosLd.observe(this) { videos ->
            if(videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            val sortedData = videos.sortedByUpdatedDate(true)
            videoAdapter.submit(sortedData)
        }
    }

    override fun isContinue(): Boolean {
        return true
    }

    companion object {
        val TAG = RecentFragment::class.simpleName

        @JvmStatic
        fun newInstance() = RecentFragment()
    }
}