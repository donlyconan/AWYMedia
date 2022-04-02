package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentRecentBinding
import com.utc.donlyconan.media.viewmodels.RecentVideoViewModel
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.MainDisplayFragment.Companion.TAG
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.fragments.options.OptionBottomDialogFragment


class RecentFragment : Fragment(), OnItemClickListener, View.OnClickListener {
    val binding by lazy { FragmentRecentBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<RecentVideoViewModel>()
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
        adapter = VideoAdapter(context!!, ArrayList(), VideoAdapter.MODE_RECENT)
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        viewModel.videoList.observe(this, adapter::submit)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.videosList[position]
        if(v.id == R.id.img_menu_more) {
            OptionBottomDialogFragment.newInstance(video, this)
                .show(fragmentManager!!, TAG)
        } else {
            val item = viewModel.videoList.value?.get(position)
            val intent = Intent(context, VideoDisplayActivity::class.java)
            intent.putExtra(VideoDisplayActivity.KEY_VIDEO,item)
            startActivity(intent)
        }
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when(v.id) {
            R.id.fab -> {
                val intent = Intent().apply {
                    type = "video/*"
                    action = Intent.ACTION_GET_CONTENT
                }
                startActivityForResult(intent, 0)
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = RecentFragment()
    }
}