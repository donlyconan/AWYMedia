package com.utc.donlyconan.media.views.fragments.maindisplay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.utc.donlyconan.media.databinding.FragmentRecentBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.RecentVideoViewModel
import com.utc.donlyconan.media.views.adapter.VideoAdapter

/**
 *  This is Recent screen which will show all video is playing
 */
class RecentFragment : ListVideoFragment() {
    val binding by lazy { FragmentRecentBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<RecentVideoViewModel>()
    override val lBinding by lazy { LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: ")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, arrayListOf(), true)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        showLoadingScreen()
        viewModel.lstVideos.observe(this) { videos ->
            if(videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            adapter.submit(videos)
        }
    }

    companion object {
        val TAG = RecentFragment::class.simpleName

        @JvmStatic
        fun newInstance() = RecentFragment()
    }
}