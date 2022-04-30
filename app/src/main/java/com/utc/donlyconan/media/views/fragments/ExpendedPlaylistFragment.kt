package com.utc.donlyconan.media.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.data.dao.ListVideoDao
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.VideoPlaylistCrossRef
import com.utc.donlyconan.media.databinding.FragmentExpendedPlaylistBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.VideoChoiceAdapter
import javax.inject.Inject


class ExpendedPlaylistFragment : BaseFragment(), View.OnClickListener, OnItemClickListener {

    private val binding by lazy { FragmentExpendedPlaylistBinding.inflate(layoutInflater) }
    private val args by navArgs<ExpendedPlaylistFragmentArgs>()
    lateinit var adapter: VideoChoiceAdapter
    @Inject lateinit var listVideoDao: ListVideoDao
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DetailedPlaylistFragment.TAG, "onCreate: ")
        setHasOptionsMenu(true)
        (context?.applicationContext as AwyMediaApplication).applicationComponent()
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
        val editText = binding.searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(Color.WHITE)
        editText.setHintTextColor(resources.getColor(R.color.search_bar_hint))
        binding.searchBar.run {
            findViewById<View>(androidx.appcompat.R.id.search_mag_icon).setOnClickListener(this@ExpendedPlaylistFragment)
        }
        adapter = VideoChoiceAdapter(requireContext(), arrayListOf())
        adapter.onItemClickListener = this
        binding.recyclerView.adapter = adapter
        listVideoDao.getAllVideosNotInPlaylist(args.playlistId).observe(this) { videos ->
            adapter.submit(videos)
        }
        binding.btnDone.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick() called with: v = $v")
        when(v?.id) {
            androidx.appcompat.R.id.search_mag_icon -> {
                findNavController().navigate(ExpendedPlaylistFragmentDirections
                    .actionExpendedPlaylistFragmentToMainDisplayFragment())
            }
            R.id.btn_done -> {
                adapter.videos.forEach { video ->
                    if(video.isSelected) {
                        val item = VideoPlaylistCrossRef(video.videoId, args.playlistId)
                        playlistWithVideosDao.insert(item)
                    }
                }
                findNavController().navigate(ExpendedPlaylistFragmentDirections
                    .actionExpendedPlaylistFragmentToMainDisplayFragment())
            }
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val item = adapter.videos[position]
        item.isSelected = !item.isSelected
        adapter.notifyItemChanged(position)
    }

    companion object {
        val TAG = ExpendedPlaylistFragment::class.java.simpleName
    }


}