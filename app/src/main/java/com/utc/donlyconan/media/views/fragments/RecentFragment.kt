package com.utc.donlyconan.media.views.fragments

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
import com.utc.donlyconan.media.databinding.FragmentRecentBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.viewmodels.RecentVideoViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class RecentFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    val binding by lazy { FragmentRecentBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<RecentVideoViewModel>()
    private lateinit var adapter: VideoAdapter

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
        viewModel.apply{

        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        if (v.id == R.id.cb_selected) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) {
                when (v.id) {
                    R.id.btn_play -> {
                        val intent = VideoDisplayActivity.newIntent(requireContext(), video, true)
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
                        viewModel.viewModelScope.launch {

                        }
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.path))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    else -> {
                        Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                    }
                }
            }
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            val intent = VideoDisplayActivity.newIntent(requireContext(), video, true)
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
        val TAG = RecentFragment::class.simpleName

        @JvmStatic
        fun newInstance() = RecentFragment()
    }
}