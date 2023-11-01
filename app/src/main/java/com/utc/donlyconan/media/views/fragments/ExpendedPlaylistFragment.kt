package com.utc.donlyconan.media.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.models.VideoPlaylistCrossRef
import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.FragmentExpendedPlaylistBinding
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoChoiceAdapter
import javax.inject.Inject


class ExpendedPlaylistFragment : BaseFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentExpendedPlaylistBinding.inflate(layoutInflater) }
    private val args by navArgs<ExpendedPlaylistFragmentArgs>()
    lateinit var adapter: VideoChoiceAdapter
    @Inject lateinit var listVideoRepo: ListVideoRepository
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    @Inject lateinit var playlistRepo: PlaylistRepository
    var playlist = mutableListOf<Video>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DetailedPlaylistFragment.TAG, "onCreate: ")
        setHasOptionsMenu(true)
        (context?.applicationContext as EGMApplication).applicationComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        val editText =
            binding.searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(Color.WHITE)
        editText.setHintTextColor(resources.getColor(R.color.search_bar_hint))
        binding.searchBar.run {
            findViewById<View>(androidx.appcompat.R.id.search_mag_icon).setOnClickListener(this@ExpendedPlaylistFragment)
        }
        adapter = VideoChoiceAdapter(requireContext(), arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        showLoadingScreen()
        listVideoRepo.getAllVideosNotInPlaylist(args.playlistId).observe(this) { videos ->
            playlist = videos.toMutableList()
           if(binding.searchBar.query.isNullOrEmpty()) {
               if(videos.isEmpty()) {
                   showNoDataScreen()
               } else {
                   hideLoading()
               }
               adapter.submit(videos)
           }
        }
        binding.btnDone.setOnClickListener(this)
        binding.searchBar.setOnQueryTextListener(onQueryTextListener)
    }

    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "onQueryTextSubmit() called with: query = $query")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(SearchBarFragment.TAG, "onQueryTextChange() called with: newText = $newText")
            if(!newText.isNullOrEmpty()) {
                showLoadingScreen()
                playlist.filter { it.title!!.contains(newText!!) }.let { videos ->
                    if(videos.isEmpty()) {
                        showNoDataScreen()
                    } else {
                        hideLoading()
                    }
                    adapter.submit(videos)
                }
            } else {
                adapter.submit(playlist)
            }
            return true
        }
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick() called with: v = $v")
        when (v?.id) {
            androidx.appcompat.R.id.search_mag_icon -> {
                findNavController().navigateUp()
            }
            R.id.btn_done -> {
                findNavController().navigateUp()
            }
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.videos[position]
        video.isChecked = !video.isChecked
        playlist.remove(video)
        adapter.videos.remove(video)

        val videoCross = VideoPlaylistCrossRef(video.videoId, args.playlistId, video.videoUri)
        playlistWithVideosDao.insert(videoCross)
        adapter.notifyDataSetChanged()
    }

    companion object {
        val TAG: String = ExpendedPlaylistFragment::class.java.simpleName
    }


}