package com.utc.donlyconan.media.views.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.PlaylistAdapter
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import javax.inject.Inject


/**
 * This class is search screen of the app. It allows user who can find video on user's app
 */
class SearchBarFragment : BaseFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private val args by navArgs<SearchBarFragmentArgs>()
    private var videoAdapter: VideoAdapter? = null
    private var playlistAdapter: PlaylistAdapter? = null
    private val searchViewModel by viewModels<SearchViewModel>()
    @Inject lateinit var playlistRepo: PlaylistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
        lBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText = binding.searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(Color.WHITE)
        editText.setHintTextColor(resources.getColor(R.color.search_bar_hint))
        binding.searchBar.run {
            findViewById<View>(androidx.appcompat.R.id.search_mag_icon).setOnClickListener(this@SearchBarFragment)
            setOnQueryTextListener(onQueryTextListener)
        }
        showNoDataScreen()
        if(args.directFrom == MainDisplayFragment.PLAYLIST_FRAGMENT) {
            playlistAdapter = PlaylistAdapter(requireContext(), arrayListOf(), playlistRepo)
            playlistAdapter?.onItemClickListener = this
            binding.recyclerView.adapter = playlistAdapter
            if(binding.searchBar.query != null) {
                onQueryTextListener.onQueryTextChange(binding.searchBar.query.toString())
            }
        } else {
            videoAdapter = VideoAdapter(requireContext(), arrayListOf())
            videoAdapter?.onItemClickListener = this
            binding.recyclerView.adapter = videoAdapter
        }
    }

    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "onQueryTextSubmit() called with: query = $query")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(TAG, "onQueryTextChange() called with: newText = $newText")
            if(newText != null && newText.isNotEmpty()) {
                showLoadingScreen()
                if(args.directFrom == MainDisplayFragment.PLAYLIST_FRAGMENT) {
                    searchViewModel.searchAllPlaylist("%$newText%").observe(this@SearchBarFragment) { playlists ->
                        if(playlists.isEmpty()) {
                            showNoDataScreen()
                        } else {
                            hideLoading()
                        }
                        playlistAdapter?.submit(playlists)
                    }
                } else {
                    searchViewModel.searchAllVideos("%$newText%").observe(this@SearchBarFragment) { videos ->
                        if(videos.isEmpty()) {
                            showNoDataScreen()
                        } else {
                            hideLoading()
                        }
                        videoAdapter?.submit(videos)
                    }
                }
            } else {
                playlistAdapter?.submit(arrayListOf())
                videoAdapter?.submit(arrayListOf())
            }
            return true
        }
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called with: v = $v")
        when(v.id) {
            androidx.appcompat.R.id.search_mag_icon -> {
                findNavController().navigateUp()
            }
            else -> {
                Log.d(TAG, "onClick haven't been handled yet!")
            }
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        if(args.directFrom == MainDisplayFragment.PLAYLIST_FRAGMENT) {
            playlistAdapter?.let { adapter ->
                val playlist = adapter.playlists[position]
                val action = SearchBarFragmentDirections
                    .actionSearchBarFragmentToDetailedPlaylistFragment(playlist.playlistId!!)
                findNavController().navigate(action)
            }
        } else {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view!!.windowToken, 0)
            videoAdapter?.let { adapter ->
                val videoList = adapter.videoList
                val intent = VideoDisplayActivity.newIntent(requireContext(), position, videoList)
                startActivity(intent)
            }
        }
    }

    companion object {
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}