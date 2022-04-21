package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.VideoMenuMoreDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * Represent for screen that will be contains all favorite user files
 */
class FavoriteFragment : Fragment(), OnItemClickListener, View.OnClickListener {

    val binding by lazy { FragmentFavoriteBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<FavoriteVideoViewModel>()
    private lateinit var adapter: VideoAdapter

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
        Log.d(TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, VideoAdapter.MODE_NORMAL)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.apply{
            viewModelScope.launch {
                videoList.collectLatest(adapter::submitData)
            }
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        if(v.id == R.id.cb_selected) {
            VideoMenuMoreDialogFragment.newInstance(video, this)
                .show(fragmentManager!!, TAG)
        } else {
            val item = adapter.getVideo(position)
            val intent = Intent(context, VideoDisplayActivity::class.java)
            intent.putExtra(VideoDisplayActivity.KEY_VIDEO,item)
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {

    }


    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }



}