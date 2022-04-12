package com.utc.donlyconan.media.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchBarFragment : Fragment(), View.OnClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoAdapter
    private val searchViewModel by viewModels<SearchViewModel>()
    private val args by navArgs<SearchBarFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
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
        adapter = VideoAdapter(requireContext())
        binding.recyclerView.adapter = adapter
        setupSearchText(args.directFrom)
    }

    private fun setupSearchText(directFrom: Int) {
        Log.d(TAG, "setupSearchText() called")
        when(directFrom) {
            MainDisplayFragment.RECENT_FRAGMENT -> {
                binding.searchBar.queryHint = "Tìm kiếm trong mục đang phát..."
            }
            MainDisplayFragment.SHARED_FRAGMENT -> {
                binding.searchBar.queryHint = "Tìm kiếm trong mục được chia sẻ..."
            }
            MainDisplayFragment.FAVORITE_FRAGMENT -> {
                binding.searchBar.queryHint = "Tìm kiếm trong mục yêu thích..."
            }
            else -> {
                binding.searchBar.queryHint = "Tìm kiếm video của bạn..."
            }
        }
    }

    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "onQueryTextSubmit() called with: query = $query")
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            Log.d(TAG, "onQueryTextChange() called with: newText = $newText")
            searchViewModel.apply {
                viewModelScope.launch {
                    searchAllVideos("%$newText%").collectLatest(adapter::submitData)
                }
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

    companion object {
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}