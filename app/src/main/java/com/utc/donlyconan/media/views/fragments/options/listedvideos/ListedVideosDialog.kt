package com.utc.donlyconan.media.views.fragments.options.listedvideos

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.databinding.ListedVideosDialogBinding
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Responsible for showing a Bottom Dialog which will provide some options to the user
 */
class ListedVideosDialog: DialogFragment(), OnItemClickListener {
    lateinit var binding: ListedVideosDialogBinding
    var listener: OnSelectedChangeListener? = null
    lateinit var adapter: ListedVideosAdapter
    @Inject lateinit var playlistRepository: PlaylistRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialogFullScreen)
        (requireContext().applicationContext as EGMApplication).applicationComponent()
            .inject(this)
        isCancelable = false
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), R.style.SheetDialogFullScreen) {
            override fun onBackPressed() {
                listener?.onBackPress()
            }
        }
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
        val currentIndex = arguments?.getInt(EXTRA_CURRENT_INDEX)
        adapter = ListedVideosAdapter(requireContext(), arrayListOf())
        binding.root.setOnClickListener { dismiss() }
        binding.rcvVideos.adapter = adapter
        adapter.setOnItemClickListener(this)

        if (playlistId != null) {
            lifecycleScope.launch {
                val playlist = playlistRepository.getPlaylistWithVideos(playlistId)
                val videos = playlist?.videos
                videos?.let { videos ->
                    videos.filterIndexed { index, video ->
                        video.setSelected(index == currentIndex)
                        index == currentIndex
                    }
                    adapter.submit(videos)
                }
                withContext(Dispatchers.Main) {
                    binding.tvSize.text = "(${videos?.size ?: 0})"
                    binding.playlistName.text = playlist?.playlist?.title
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.container.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in))
    }

    override fun dismiss() {
        val runnable: Runnable = Runnable { super.dismiss() }
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out).apply {
            setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    Logs.d( "onAnimationEnd() called with: animation = $animation")
                    runnable.run()
                }

                override fun onAnimationRepeat(animation: Animation?) {}

            })
            binding.container.startAnimation(this)
        }
    }

    override fun onItemClick(v: View, position: Int) {
        Logs.d("onItemClick() called with: v = $v, position = $position")
        val video = adapter.getItem(position) as Video
        listener?.onSelectionChanged(video.videoId)
        dismiss()
    }

    companion object {
        const val EXTRA_PLAYLIST_ID = "media.playlist_id"
        const val EXTRA_CURRENT_INDEX = "media.extra_current_index"

        fun newInstance(playlistId: Int, currentIndex: Int  = 0, listener: OnSelectedChangeListener? = null) = ListedVideosDialog().apply {
            this.listener = listener
            arguments = bundleOf(EXTRA_PLAYLIST_ID to playlistId, EXTRA_CURRENT_INDEX to currentIndex)
        }
        
    }

}
interface OnSelectedChangeListener {
    fun onSelectionChanged(videoId: Int)

    fun onBackPress()
}