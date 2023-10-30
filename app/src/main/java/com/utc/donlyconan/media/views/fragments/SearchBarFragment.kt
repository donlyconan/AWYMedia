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
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * This class is search screen of the app. It allows user who can find video on user's app
 */
class SearchBarFragment : BaseFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoAdapter
    private val searchViewModel by viewModels<SearchViewModel>()
    @Inject lateinit var playlistRepo: PlaylistRepository

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
        adapter = VideoAdapter(requireContext(), arrayListOf(), showOptionMenu = false)
        adapter?.setOnItemClickListener(this)
        binding.recyclerView.adapter = adapter
        binding.searchBar.setOnQueryTextListener(onQueryTextListener)
        searchViewModel.commonData.observe(this)  { data ->
            if(data.isEmpty()) {
                showNoDataScreen()
            } else {
                hideLoading()
            }
            adapter.submit(data)
        }

        lifecycleScope.launch(Dispatchers.Default) {
            searchViewModel.search("20")
        }
    }

    private val sharedFlow = MutableSharedFlow<String>()
        .debounce(400)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)


    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if(!newText.isNullOrEmpty() && newText.trim().isNotEmpty()) {
                showLoadingScreen()
//                searchViewModel.search(newText)
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

    companion object {
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}