package com.utc.donlyconan.media.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
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
    private var audioService: AudioService? = null
    @Inject lateinit var videoRepo: VideoRepository
    private var lock: Boolean = false



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioService = application.getAudioService()
    }

    override fun onItemClick(v: View, position: Int) {
        if(lock) {
            Log.d(TAG, "onItemClick: The list is locked!")
            return
        }
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = adapter.getItem(position) as Video

        if (v.id == R.id.img_menu_more) {
                    MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) {
                        when (v.id) {
                            R.id.btn_play -> {
                                startVideoDisplayActivity(video.videoId, video.videoUri)
                            }
                            R.id.btn_play_music -> audioService?.let { service ->
                                service.play(MediaItem.fromUri(video.videoUri))
                            }
                            R.id.btn_favorite -> {
                                video.isFavorite = !video.isFavorite
                                videoRepo.update(video)
                                adapter.notifyItemChanged(position)
                            }
                            R.id.btn_delete -> {

                            }
                            R.id.btn_share -> {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "video/*"
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUri))
                                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shared_file))
                                startActivity(Intent.createChooser(intent, getString(R.string.share_file)))
                            }
                            else -> {
                                Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                            }
                }
            }
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            startVideoDisplayActivity(video.videoId, video.videoUri,)
        }
    }



    companion object {
        val TAG: String = ListVideoDisplayFragment::class.java.simpleName
    }

}