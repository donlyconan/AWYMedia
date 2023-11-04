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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.OnItemLongClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.MainDisplayFragmentDirections
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PlaylistFragment
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * This class is search screen of the app. It allows user who can find video on user's app
 */
class SearchBarFragment : BaseFragment(), View.OnClickListener, OnItemClickListener, OnItemLongClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoAdapter
    private val searchViewModel by viewModels<SearchViewModel>()
    @Inject lateinit var playlistRepo: PlaylistRepository
    @Inject lateinit var videoRepo: VideoRepository
    private var func: (suspend (String) -> Any?)? = null
    private var currentJob: Job? = null

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
        adapter = VideoAdapter(requireContext(), arrayListOf(), showOptionMenu = true)
        adapter.setOnItemClickListener(this)
        adapter.setOnLongClickListener(this)
        binding.recyclerView.adapter = adapter
        binding.searchBar.setOnQueryTextListener(onQueryTextListener)
        searchViewModel.commonData.observe(this)  { data ->
            Log.d(TAG, "searchViewModel.commonData: data = $data")
            if(data.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            adapter.submit(data)
        }

        lifecycleScope.launch {
            sharedFlow.collectLatest { keyword ->
                Log.d(TAG, "onViewCreated() called with: keyword = $keyword")
                currentJob?.cancel()
                currentJob = launch {
                    searchViewModel.search(keyword)
                }
            }
        }
    }

    private val sharedFlow = callbackFlow<String> {
        func = { newText ->
            trySend(newText)
        }
        awaitClose()
    }.debounce(400)
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)


    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(TAG, "onQueryTextChange() called with: newText = $newText")
            if(!newText.isNullOrEmpty() && newText.trim().isNotEmpty()) {
                showLoadingScreen()
                lifecycleScope.launch {
                    func?.invoke(newText)
                }
            } else {
                hideLoading()
                adapter.submit(listOf())
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
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val item = adapter.getItem(position)
        when(v.id) {
            R.id.img_menu_more -> {
                if(item is Video) {
                    MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
                        when (view.id) {
                            R.id.btn_play -> startVideoDisplayActivity(item.videoId, item.videoUri)
                            R.id.btn_play_music -> playMusic(item)
                            R.id.btn_favorite -> {
                                item.isFavorite = !item.isFavorite
                                videoRepo.update(item)
                                adapter.notifyItemChanged(position)
                            }

                            R.id.btn_delete -> lifecycleScope.launch(Dispatchers.IO) {
                                deleteVideo(item, videoRepo)
                            }

                            R.id.btn_share -> share(item)
                            R.id.btn_lock -> lockVideo(item, videoRepo, appComponent.getPlaylistWithVideoDao())
                            R.id.btn_unlock -> unlockVideo(item, videoRepo)
                            else -> {
                                Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                            }
                        }
                    }.setGoneViews(listOf(R.id.btn_unlock))
                        .setViewState(R.id.btn_favorite, item.isFavorite)
                        .show(parentFragmentManager, PersonalVideoFragment.TAG)
                }
            }
            else -> {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view!!.windowToken, 0)
                val item = adapter.getItem(position)
                if(item is Playlist) {
                    val action = SearchBarFragmentDirections
                        .actionSearchBarFragmentToDetailedPlaylistFragment(item.playlistId!!)
                    findNavController().navigate(action)
                }
                if(item is Video) {
                    startVideoDisplayActivity(item.videoId, item.videoUri)
                }
            }
        }
    }

    override fun onItemLongClick(v: View, position: Int) {
        Log.d(TAG, "onItemLongClick() called with: v = $v, position = $position")
        val item = adapter.getItem(position)
        if(v.id == R.id.img_menu_more && item is Playlist) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_playlist_option) {
                when(it.id) {
                    R.id.btn_open -> lifecycleScope.launch(Dispatchers.IO) {
                        item.firstVideo?.let { video ->
                            startVideoDisplayActivity(video.videoId, video.videoUri, item.playlistId!!)
                        }

                    }
                    R.id.btn_play_music -> lifecycleScope.launch(Dispatchers.IO) {
                        playlistRepo.playlistWithVideosDao .get(item.playlistId!!)
                            .videos.map { MediaItem.fromUri(it.videoUri) }
                            .let { uris ->
                                withContext(Dispatchers.Main) {
                                    startPlayMusic(uris)
                                }
                            }
                    }
                    R.id.btn_add -> {
                        val action = MainDisplayFragmentDirections
                            .actionMainDisplayFragmentToExpendedPlaylistFragment(item.playlistId!!)
                        findNavController().navigate(action)
                    }
                    R.id.btn_delete -> {
                        playlistRepo.delete(item)
                        playlistRepo.removePlaylist(item.playlistId!!)
                    }
                    R.id.btn_rename -> {
                        PlaylistFragment.AddedPlaylistDialog(true, item.title) { text, _ ->
                            val item2 = Playlist(item.playlistId, text)
                            playlistRepo.update(item2)
                        }.show(supportFragmentManager, PlaylistFragment.TAG)
                    }
                }
            }.show(supportFragmentManager, PlaylistFragment.TAG)
        }
    }

    companion object {
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}