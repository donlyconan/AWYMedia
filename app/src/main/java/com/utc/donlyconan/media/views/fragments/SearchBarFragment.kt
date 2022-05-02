package com.utc.donlyconan.media.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.viewmodels.SearchViewModel
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideoFragment

/**
 * This class is search screen of the app. It allows user who can find video on user's app
 */
class SearchBarFragment : Fragment(), View.OnClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }
    private lateinit var adapter: VideoAdapter
    private val searchViewModel by viewModels<SearchViewModel>()

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
        showNoDataScreen()
        adapter = VideoAdapter(requireContext(), arrayListOf())
        binding.recyclerView.adapter = adapter
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
                searchViewModel.searchAllVideos("%$newText%").observe(this@SearchBarFragment) { videos ->
                    if(videos.isEmpty()) {
                        showNoDataScreen()
                    } else {
                        hideLoading()
                    }
                    adapter.submit(videos)
                }
            } else {
                adapter.submit(arrayListOf())
            }
            return true
        }
    }

    val lBinding by lazy { LoadingDataScreenBinding.bind(binding.icdLoading.frameContainer) }

    fun showLoadingScreen() {
        Log.d(ListVideoFragment.TAG, "showLoadingScreen() called")
        lBinding.llLoading.visibility = View.VISIBLE
        lBinding.tvNoData.visibility = View.INVISIBLE
        lBinding.frameContainer.visibility = View.VISIBLE
    }

    fun showNoDataScreen() {
        Log.d(ListVideoFragment.TAG, "showNoDataScreen() called")
        lBinding.llLoading.visibility = View.INVISIBLE
        lBinding.tvNoData.visibility = View.VISIBLE
        lBinding.frameContainer.visibility = View.VISIBLE
    }

    fun hideLoading() {
        Log.d(ListVideoFragment.TAG, "hideLoading() called")
        lBinding.frameContainer.visibility = View.INVISIBLE
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