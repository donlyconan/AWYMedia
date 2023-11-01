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
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
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

    override fun onItemClick(v: View, position: Int) {
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = videoAdapter.getItem(position) as Video
        Log.d(TAG, "onItemClick: video=$video")
        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
                when (view.id) {
                    R.id.btn_play -> {
                        val intent = VideoDisplayActivity.newIntent(requireContext(), video.videoId, video.videoUri, continued = true)
                        startActivity(intent)
                    }
                    R.id.btn_play_music -> startPlayMusic(video)
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        videoRepo.update(video)
                        videoAdapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            videoRepo.moveToRecyleBin(video)
                        }
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUri))
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
            val intent = VideoDisplayActivity.newIntent(requireContext(), video.videoId, video.videoUri, continued = true)
            startActivity(intent)
        }
    }

    companion object {
        val TAG = RecentFragment::class.simpleName

        @JvmStatic
        fun newInstance() = RecentFragment()
    }
}