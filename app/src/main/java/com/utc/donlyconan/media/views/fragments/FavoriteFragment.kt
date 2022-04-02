package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentFavoriteBinding
import com.utc.donlyconan.media.databinding.FragmentRecentBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.viewmodels.FavoriteVideoViewModel
import com.utc.donlyconan.media.viewmodels.RecentVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.OptionBottomDialogFragment


class FavoriteFragment : Fragment(), OnItemClickListener, View.OnClickListener {

    val binding by lazy { FragmentFavoriteBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<FavoriteVideoViewModel>()
    private lateinit var adapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MainDisplayFragment.TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(
            MainDisplayFragment.TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            MainDisplayFragment.TAG, "onViewCreated() called with: view = $view, savedInstanceState = " +
                "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter(context!!, ArrayList(), VideoAdapter.MODE_NORMAL)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.videoList.observe(this, adapter::submit)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(MainDisplayFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.videosList[position]
        if(v.id == R.id.img_menu_more) {
            OptionBottomDialogFragment.newInstance(video, this)
                .show(fragmentManager!!, MainDisplayFragment.TAG)
        } else {
            val item = viewModel.videoList.value?.get(position)
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