package com.utc.donlyconan.media.views.fragments.maindisplay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.FileManager
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.components.getMediaUri
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.VideoTask
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class ListVideosFragment : BaseFragment(), OnItemClickListener {
    
    protected lateinit var videoAdapter: VideoAdapter
    protected var audioService: AudioService? = null
    protected var unlockMode = false
    @Inject lateinit var videoRepo: VideoRepository
    protected var handlingVideoTask: VideoTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        audioService = application.getAudioService()
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = videoAdapter.getVideo(position)

        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
                when (view.id) {
                    R.id.btn_play -> startVideoDisplayActivity(video.videoId, video.videoUri)
                    R.id.btn_play_music -> playMusic(video)
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        videoRepo.update(video)
                        videoAdapter.notifyItemChanged(position)
                    }

                    R.id.btn_delete -> lifecycleScope.launch(Dispatchers.IO) {
                        deleteVideo(video, videoRepo)
                    }

                    R.id.btn_share -> share(video)
                    R.id.btn_lock -> lockVideo(video, videoRepo)
                    R.id.btn_unlock -> unlockVideo(video, videoRepo)
                    else -> {
                        Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                    }
                }
            }.setVisibility(if (unlockMode) R.id.btn_lock else R.id.btn_unlock)
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            startVideoDisplayActivity(video.videoId, video.videoUri)
        }
    }

    override fun onDeletedResult(result: ActivityResult) {
        Log.d(TAG, "onDeletedResult() called with: result = ${result.resultCode == Activity.RESULT_OK}")
        try {
            if(result.resultCode == Activity.RESULT_OK) {
                handlingVideoTask?.succeed?.run()
            } else {
                handlingVideoTask?.error?.run()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onDeletedResult: ", e)
            showToast(R.string.toast_when_failed_user_action)
        }
        handlingVideoTask = null
    }


    companion object {
        val TAG = ListVideosFragment::class.simpleName

    }

}