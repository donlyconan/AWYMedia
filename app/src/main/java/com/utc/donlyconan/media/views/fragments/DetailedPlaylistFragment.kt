package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.FragmentDetailedPlaylistBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideoFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


/**
 * Show all videos included in the playlist
 */
class DetailedPlaylistFragment : ListVideoFragment(), OnItemClickListener {

    val binding by lazy { FragmentDetailedPlaylistBinding.inflate(layoutInflater) }
    val args by navArgs<DetailedPlaylistFragmentArgs>()
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    @Inject lateinit var playlistRepo: PlaylistRepository


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
        lBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
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
        binding.title.text = "Untitled"

        adapter = VideoAdapter(requireContext(), arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        showLoadingScreen()
        playlistWithVideosDao.getPlaylist(args.playlistId).observe(this) { videoPl ->
            Log.d(TAG, "onViewCreated() called video.size=" + videoPl.videos.size)
            binding.title.text = videoPl.playlist.title
            if(videoPl.videos.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            adapter.submit(videoPl.videos)
            binding.title.text = videoPl.playlist.title
        }
    }


    override fun onItemClick(v: View, position: Int) {
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)
        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
                when (view.id) {
                    R.id.btn_play -> {
                        val intent = VideoDisplayActivity.newIntent(requireContext(), position,  adapter.videoList)
                        startActivity(intent)
                    }
                    R.id.btn_play_music -> {
                        application.iMusicalService()?.apply {
                            setPlaylist(position, adapter.videoList)
                            play()
                        }
                    }
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        videoRepo.update(video)
                        adapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {
                        playlistRepo.deleteFromPlaylist(video.videoId, args.playlistId)
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
                .setOnInitialView(object : MenuMoreOptionFragment.OnInitialView {

                    override fun onInitial(v: View) {
                        Log.d(TAG, "onInitial() called with: v = $v")
                        v.findViewById<TextView>(R.id.btn_delete)
                            .apply {
                                text = "Xoá khỏi danh sách phát"
                            }
                    }

                })
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            val intent = VideoDisplayActivity.newIntent(requireContext(), position, adapter.videoList)
            sharedViewModel.playlist.value = adapter.videoList
            startActivity(intent)
        }
    }

    companion object {
        val TAG = DetailedPlaylistFragment::class.simpleName
    }
}