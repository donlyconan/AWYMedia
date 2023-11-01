package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.FragmentDetailedPlaylistBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


/**
 * Show all videos included in the playlist
 */
class DetailedPlaylistFragment : ListVideosFragment(), OnItemClickListener {

    val binding by lazy { FragmentDetailedPlaylistBinding.inflate(layoutInflater) }
    val args by navArgs<DetailedPlaylistFragmentArgs>()
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    @Inject lateinit var playlistRepo: PlaylistRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setHasOptionsMenu(true)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: ")
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        val appCompat = activity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.title.setText(R.string.untitled)

        videoAdapter = VideoAdapter(requireContext(), arrayListOf())
        videoAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = videoAdapter
        showLoadingScreen()
        playlistWithVideosDao.getPlaylist(args.playlistId).observe(this) { videoPl ->
            Log.d(TAG, "onViewCreated() called video.size=" + videoPl.videos.size)
            binding.title.text = videoPl.playlist.title
            if(videoPl.videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            videoAdapter.submit(videoPl.videos)
            binding.title.text = videoPl.playlist.title
        }
        hideViews.add(R.id.btn_unlock)
    }

    override fun getPlaylistId(): Int {
        return args.playlistId
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu() called with: menu = $menu, inflater = $inflater")
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        inflater.inflate(R.menu.menu_detail_list, menu)
        menu.findItem(R.id.it_add).setOnMenuItemClickListener {
            val action = DetailedPlaylistFragmentDirections.actionDetailedPlaylistFragmentToExpendedPlaylistFragment(args.playlistId)
            findNavController().navigate(action)
            true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        val TAG = DetailedPlaylistFragment::class.simpleName
    }
}