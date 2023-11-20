package com.utc.donlyconan.media.views.fragments.privatefolder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.sortedByCreatedDate
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.databinding.FragmentPrivateFolderBinding
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment

class PrivateFolderFragment : ListVideosFragment() {

    companion object {
        fun newInstance() = PrivateFolderFragment()
    }

    private val viewModel by viewModels<PrivateFolderViewModel> {
        viewModelFactory {
            initializer {
                PrivateFolderViewModel(application.applicationComponent().getVideoDao())
            }
        }
    }
    private val binding by lazy { FragmentPrivateFolderBinding.inflate(layoutInflater) }
    override val listView: RecyclerView
        get() = binding.recyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appCompat = activity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setHasOptionsMenu(true)
        hideViews.clear()
        hideViews.add(R.id.btn_lock)
        hideViews.add(R.id.btn_share)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoAdapter = VideoAdapter(requireContext(), arrayListOf())
        videoAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = videoAdapter
        showLoadingScreen()
        viewModel.videosLd.observe(this) { videos ->
            Logs.d("onViewCreated() called with: video size = ${videos.size}")
            if(videos.isEmpty()) {
                showNoDataScreen()
                videoAdapter.submit(videos)
            } else {
                hideLoading()
                val data = videos.sortedByCreatedDate(true)
                videoAdapter.submit(data)
            }
        }
    }

    override fun getPlaylistId(): Int {
        return Playlist.PRIVATE_PLAYLIST_FOLDER
    }

//    @SuppressLint("RestrictedApi")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        Log.d(RecycleBinFragment.TAG, "onCreateOptionsMenu: ")
//        if (menu is MenuBuilder) {
//            menu.setOptionalIconsVisible(true)
//        }
//        inflater.inflate(R.menu.menu_trash_bar, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }



}