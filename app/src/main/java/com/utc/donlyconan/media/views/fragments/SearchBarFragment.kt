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
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import javax.inject.Inject


/**
 * This class is search screen of the app. It allows user who can find video on user's app
 */
class SearchBarFragment : BaseFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private lateinit var videoAdapter: VideoAdapter
    private val viewModel by viewModels<SearchViewModel>()
    @Inject lateinit var playlistRepo: PlaylistRepository
    @Inject lateinit var videoRepo: VideoRepository
    private val args by navArgs<SearchBarFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        lsBinding = LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer)
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
        videoAdapter = VideoAdapter(requireContext(), arrayListOf(), showOptionMenu = false)
        videoAdapter.setOnItemClickListener(this)
        binding.recyclerView.adapter = videoAdapter
        binding.searchBar.setOnQueryTextListener(onQueryTextListener)
        viewModel.commonData.observe(this)  { data ->
            Log.d(TAG, "searchViewModel.commonData: data = $data")
            runOnWorkerThread {
                data.filterIsInstance<Playlist>().forEach { playlist: Playlist ->
                    playlist.itemSize = playlistRepo.countVideos(playlist.playlistId!!)
                    playlist.firstVideo = playlistRepo.getFirstVideo(playlist.playlistId!!)
                }
                if (data.isEmpty()) {
                    showNoDataScreen()
                } else {
                    hideLoading()
                }
                runOnUIThread {
                    videoAdapter.submit(data)
                }
            }
        }

    }


    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(TAG, "onQueryTextChange() called with: newText = $newText")
            if(!newText.isNullOrEmpty() && newText.trim().isNotEmpty()) {
                showLoadingScreen()
                runOnWorkerThread {
                    if(args.directFrom == SEARCH_FOR_PLAYLIST) {
                        viewModel.searchForPlaylist(newText.lowercase())
                    } else {
                        viewModel.searchForVideo(newText.lowercase())
                    }
                }
            } else {
                hideLoading()
                videoAdapter.submit(listOf())
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
            else -> null
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Logs.d( "onItemClick() called with: v = $v, position = $position")
        val item = videoAdapter.getItem(position)
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view!!.windowToken, 0)
        if (item is Playlist) {
            val action = SearchBarFragmentDirections
                .actionSearchBarFragmentToDetailedPlaylistFragment(item.playlistId!!)
            findNavController().navigate(action)
        }
        if (item is Video) {
            startVideoDisplayActivity(item.videoId, item.videoUri)
        }
    }

    companion object {
        val SEARCH_FOR_PLAYLIST = 1
        val SEARCH_FOR_VIDEO = 2
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}