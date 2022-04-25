package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.databinding.FragmentDetailedPlaylistBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.MainActivity
import com.utc.donlyconan.media.views.adapter.NVideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


/**
 * Show all videos included in the playlist
 */
class DetailedPlaylistFragment : BaseFragment(), OnItemClickListener {

    val binding by lazy { FragmentDetailedPlaylistBinding.inflate(layoutInflater) }
    val args by navArgs<DetailedPlaylistFragmentArgs>()
    lateinit var adapter: NVideoAdapter
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
        val appCompat = activity as MainActivity
        appCompat.setSupportActionBar(binding.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            val action = DetailedPlaylistFragmentDirections
                .actionDetailedPlaylistFragmentToMainDisplayFragment()
            findNavController().navigate(action)
        }
        binding.title.text = null

        adapter = NVideoAdapter(requireContext(), emptyList())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        playlistWithVideosDao.getPlaylist(args.playlistId).observe(this) { videoPl ->
            Log.d(TAG, "onViewCreated() called video.size=" + videoPl.videos.size)
            binding.title.text = videoPl.playlist.title
            adapter.submit(videoPl.videos)
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.videos[position]
        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_detailed_playlist_option) { v ->
                Log.d(TAG, "onClick() called with: v = $v")
                when (v.id) {
                    R.id.btn_play -> {
//                        val action = DetailedPlaylistFragmentDirections
//                            .actionDetailedPlaylistFragmentToVideoDisplayFragment2(video)
//                        findNavController().navigate(action)
                    }
                    R.id.btn_play_music -> {
                        application.iMusicalService()?.apply {
                            setVideoId(video.videoId)
                            play()
                        }
                    }
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        videoDao.update(video)
                        adapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {
                        playlistWithVideosDao.deleteFromPlaylist(video.videoId, args.playlistId)
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.path))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    else -> {
                        Log.d(TAG, "onClick: actionId hasn't found!")
                    }
                }
            }
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(supportFragmentManager, TAG)
        } else {
            val video = adapter.videos[position]
            val action = DetailedPlaylistFragmentDirections
                .actionDetailedPlaylistFragmentToVideoDisplayFragment(video)
        }
    }

    companion object {
        val TAG = DetailedPlaylistFragment::class.simpleName
    }
}