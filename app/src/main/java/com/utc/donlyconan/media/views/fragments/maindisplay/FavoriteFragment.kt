package com.utc.donlyconan.media.views.fragments.maindisplay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentFavoriteBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.viewmodels.FavoriteVideoViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.launch


/**
 * Represent for screen that will be contains all favorite user files
 */
class FavoriteFragment : ListVideoFragment(), OnItemClickListener {

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            RecentFragment.TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, arrayListOf(), false)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.lstVideos.observe(this) { videos ->
            adapter.submit(videos)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }



}