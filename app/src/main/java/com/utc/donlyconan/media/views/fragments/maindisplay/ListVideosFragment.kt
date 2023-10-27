package com.utc.donlyconan.media.views.fragments.maindisplay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.FileManager
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.VideoDisplayActivity
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.VideoTask
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject

abstract class ListVideosFragment : BaseFragment(), OnItemClickListener {
    
    protected lateinit var videoAdapter: VideoAdapter
    protected var audioService: AudioService? = null
    protected var unlockMode = false
    @Inject lateinit var fileManager: FileManager
    @Inject lateinit var videoRepo: VideoRepository
    protected var handlingVideoTask: VideoTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        audioService = application.getAudioService()
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(PersonalVideoFragment.TAG, "onItemClick() called with: v = $v, position = $position")
        val video = videoAdapter.getVideo(position)

        if (v.id == R.id.img_menu_more) {
            MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
                when (view.id) {
                    R.id.btn_play -> VideoDisplayActivity.newIntent(requireContext(), video.videoId).let { startActivity(it) }
                    R.id.btn_play_music -> audioService?.let { service ->
                        service.play(MediaItem.fromUri(video.videoUri))
                    }
                    R.id.btn_favorite -> {
                        video.isFavorite = !video.isFavorite
                        videoRepo.update(video)
                        videoAdapter.notifyItemChanged(position)
                    }
                    R.id.btn_delete -> {
                        if(video.isSecured) {
                            videoRepo.moveToRecyleBin(video)
                        } else {
                            fileManager.saveIntoInternal(video.videoUri.toUri(), video.title!!) { uri, name ->
                                val newVideo = video.copy(videoUri = uri.toString(), title = name)
                                deleteVideoFromExternalStorage(video.videoUri.toUri())
                                handlingVideoTask = VideoTask(listOf(newVideo), succeed = {
                                    Log.d(TAG, "handle succeeded items")
                                    videoRepo.moveToRecyleBin(video)
                                }, error = {
                                    Log.d(TAG, "handle error items")
                                    context?.deleteFile(newVideo.videoUri)
                                })
                            }
                        }
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUri))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    R.id.btn_lock -> {
                        fileManager.saveIntoInternal(video.videoUri.toUri(), video.title ?: "no_name") { uri, newName ->
                            try {
                                deleteVideoFromExternalStorage(video.videoUri.toUri())
                                val newVideo = video.copy(isSecured = true, videoUri = uri.toString(), title = newName)
                                videoRepo.update(newVideo)
                            } catch (e: Exception) {
                                showToast(R.string.toast_when_failed_user_action)
                            }
                        }
                    }
                    R.id.btn_unlock -> {
                        fileManager.removeFromInternal(video.title!!) {uri ->
                            val newVideo = video.copy(videoUri =  uri.toString(), isSecured = false)
                            videoRepo.update(newVideo)
                        }
                    }
                    else -> {
                        Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                    }
                }
            }
                .setVisibility(if(unlockMode) R.id.btn_lock else R.id.btn_unlock)
                .setViewState(R.id.btn_favorite, video.isFavorite)
                .show(parentFragmentManager, PersonalVideoFragment.TAG)
        } else {
            VideoDisplayActivity.newIntent(requireContext(), video.videoId).let { startActivity(it) }
        }
    }

    override fun onDeletedResult(result: ActivityResult) {
        Log.d(TAG, "onDeletedResult() called with: result = $result")
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