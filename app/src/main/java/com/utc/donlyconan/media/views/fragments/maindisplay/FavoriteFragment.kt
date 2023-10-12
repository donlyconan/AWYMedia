package com.utc.donlyconan.media.views.fragments.maindisplay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.utc.donlyconan.media.databinding.FragmentFavoriteBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.FavoriteVideoViewModel
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter


/**
 * Represent for screen that will be contains all favorite user files
 */
class FavoriteFragment : ListVideosFragment(), OnItemClickListener {

    val binding by lazy { FragmentFavoriteBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<FavoriteVideoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            RecentFragment.TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, arrayListOf(), false)
        adapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = adapter
        showLoadingScreen()
        viewModel.lstVideos.observe(this) { videos ->
            if(videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            videos.sortedWith {u,v -> (v.updatedAt - u.updatedAt).toInt() }
            adapter.submit(videos)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }



}