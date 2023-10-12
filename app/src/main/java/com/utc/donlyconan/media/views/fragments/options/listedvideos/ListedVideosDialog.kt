package com.utc.donlyconan.media.views.fragments.options.listedvideos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.dagger.components.ApplicationComponent
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.ListedVideosDialogBinding
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import javax.inject.Inject

/**
 * Responsible for showing a Bottom Dialog which will provide some options to the user
 */
class ListedVideosDialog: DialogFragment(), OnItemClickListener {
    lateinit var binding: ListedVideosDialogBinding
    var listener: OnSelectedChangeListener? = null
    lateinit var adapter: ListedVideosAdapter
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
        (requireContext().applicationContext as EGMApplication).applicationComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListedVideosDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logs.d("onViewCreated: ")
        val playlistId = arguments?.getInt(EXTRA_PLAYLIST_ID)
        if(playlistId != null) {
            val videos = playlistWithVideosDao.get(playlistId)?.videos
            adapter = ListedVideosAdapter(requireContext(), videos)
            adapter.setOnItemClickListener(this)
            binding.rcvVideos.adapter = adapter
            binding.tvSize.text = "(${videos.size})"
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Logs.d("onItemClick() called with: v = $v, position = $position")
        val video = adapter.getItem(position) as Video
        listener?.onSelectionChanged(video.videoId)
    }

    companion object {
        const val EXTRA_PLAYLIST_ID = "media.playlist_id"
        
        fun newInstance(playlistId: Int, listener: OnSelectedChangeListener? = null) = ListedVideosDialog().apply {
            this.listener = listener
            arguments = bundleOf(EXTRA_PLAYLIST_ID to playlistId)
        }
        
    }

}
interface OnSelectedChangeListener {
    fun onSelectionChanged(videoId: Int)
}