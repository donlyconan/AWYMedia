package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.databinding.FragmentDetailedPlaylistBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideoFragment
import javax.inject.Inject


/**
 * Show all videos included in the playlist
 */
class DetailedPlaylistFragment : ListVideoFragment(), OnItemClickListener {

    val binding by lazy { FragmentDetailedPlaylistBinding.inflate(layoutInflater) }
    val args by navArgs<DetailedPlaylistFragmentArgs>()
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    @Inject lateinit var videoDao: VideoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setHasOptionsMenu(true)
        applicationComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: ")
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
        binding.title.text = null

        adapter = VideoAdapter(requireContext(), arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        playlistWithVideosDao.getPlaylist(args.playlistId).observe(this) { videoPl ->
            Log.d(TAG, "onViewCreated() called video.size=" + videoPl.videos.size)
            binding.title.text = videoPl.playlist.title
            adapter.submit(videoPl.videos)
        }
    }

    companion object {
        val TAG = DetailedPlaylistFragment::class.simpleName
    }
}