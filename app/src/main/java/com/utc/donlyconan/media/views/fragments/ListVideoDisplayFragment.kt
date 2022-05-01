package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import androidx.recyclerview.widget.RecyclerView
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


/**
 * Lớp cung cấp các phương tiện chức năng hỗ trợ cho việc phát video
 */
abstract class ListVideoDisplayFragment : BaseFragment(), OnItemClickListener {

    abstract val recyclerView: RecyclerView
    abstract val adapter: VideoAdapter
    @Inject lateinit var settings: Settings
    @Inject lateinit var videoRepo: VideoRepository


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getVideo(position)

        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) {
                when (v.id) {
                    R.id.btn_play -> {
                        val intent = VideoDisplayActivity.newIntent(requireContext(), video, false)
                        startActivity(intent)
                    }
                    R.id.btn_play_music -> {
                        application.iMusicalService()?.apply {
                            setVideoId(video.videoId)
                            play()
                        }
                    }
                    R.id.btn_favorite -> {
//                        video.isFavorite = !video.isFavorite
//                        videoRepo.update(video)
//                        adapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {

                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.path))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    else -> {
                        Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                    }
                }
            }
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            val intent = VideoDisplayActivity.newIntent(requireContext(), video, false)
            startActivity(intent)
        }
    }

    companion object {
        val TAG: String = ListVideoDisplayFragment::class.java.simpleName
    }

}