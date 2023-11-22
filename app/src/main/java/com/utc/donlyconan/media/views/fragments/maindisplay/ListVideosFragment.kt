package com.utc.donlyconan.media.views.fragments.maindisplay

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.app.utils.AlertDialogManager
import com.utc.donlyconan.media.app.utils.compareUri
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.OnItemClickListener
import com.utc.donlyconan.media.views.adapter.VideoAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class ListVideosFragment : BaseFragment(), OnItemClickListener,  FileService.OnFileServiceListener  {
    
    protected lateinit var videoAdapter: VideoAdapter
    protected var audioService: AudioService? = null
    @Inject lateinit var videoRepo: VideoRepository
    protected val hideViews by lazy { mutableSetOf<Int>() }
    abstract val listView: RecyclerView?
    protected var downloadingAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        audioService = application.getAudioService()
        hideViews.add(R.id.btn_unlock)
        hideViews.add(R.id.btn_quick_share)
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val video = videoAdapter.getVideo(position)

        if (v.id == R.id.img_menu_more) {
            openMenuMore(video, position)
        } else {
            startVideoDisplayActivity(video.videoId, video.videoUri, getPlaylistId(), isContinue())
        }
    }

    protected fun openMenuMore(video: Video, position: Int) {
        Log.d(TAG, "openMenuMore() called with: video = $video, position = $position")
        MenuMoreOptionFragment.newInstance(R.layout.fragment_personal_option) { view ->
            when (view.id) {
                R.id.btn_play -> startVideoDisplayActivity(video.videoId, video.videoUri, getPlaylistId(), isContinue())
                R.id.btn_play_music -> {
                    if(getPlaylistId() == -1) {
                        playMusic(video)
                    } else {
                        val videos = videoAdapter.getData().filterIsInstance<Video>()
                        val index = videos.indexOfFirst { vd -> vd.videoId == video.videoId }
                        val items = videos.map { MediaItem.fromUri(it.videoUri) }
                        startPlayMusic(items, index)
                    }
                }
                R.id.btn_favorite -> {
                    video.isFavorite = !video.isFavorite
                    videoRepo.update(video)
                    videoAdapter.notifyItemChanged(position)
                }

                R.id.btn_delete -> lifecycleScope.launch(Dispatchers.IO) {
                    deleteVideo(video, videoRepo)
                }

                R.id.btn_share -> share(video)
                R.id.btn_quick_share -> handleQuickShare(video)
                R.id.btn_lock -> lockVideo(video, videoRepo, appComponent.getPlaylistWithVideoDao())
                R.id.btn_unlock -> unlockVideo(video, videoRepo)
                else -> {
                    Log.d(PersonalVideoFragment.TAG, "onClick: actionId hasn't found!")
                }
            }
        }.setGoneViews(hideViews)
            .setViewState(R.id.btn_favorite, video.isFavorite)
            .show(parentFragmentManager, PersonalVideoFragment.TAG)
    }

    open fun getPlaylistId(): Int {
        return -1
    }

    open fun isContinue(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        application.getFileService()?.registerOnFileServiceListener(this)
    }

    override fun onStop() {
        super.onStop()
        application.getFileService()?.unregisterOnFileServiceListener(this)
    }

    override fun onDownloadingProgress(uri: String, progress: Long, total: Long) {
        val index = videoAdapter.getData().indexOfFirst { item ->  item is Video && item.videoUri.compareUri(uri) }
        if(index != -1) {
            val video = videoAdapter.getVideo(index)
            video.available = progress == total
            runOnUIThread {
                val holder = listView?.findViewHolderForAdapterPosition(index) as? VideoAdapter.VideoHolder
                holder?.setProgress(progress, total)
                holder?.setBlockMode(progress != total)
            }
            if(progress == total) {
                runOnUIThread {
                    if(downloadingAlert != null) {
                        downloadingAlert?.cancel()
                    }
                    downloadingAlert = AlertDialogManager.createDeleteAlertDialog(
                        context = requireContext(),
                        title = getString(R.string.app_name),
                        msg = getString(R.string.the_file_is_downloaded_do_you_want_to_open, video.title),
                        onAccept = {
                            startVideoDisplayActivity(video.videoId, video.videoUri, continued=true)
                        })
                    downloadingAlert?.show()
                }
            }
        }
    }

    override fun onSendingFileStatus(videos: List<Video>, status: Int) {
        Log.d(TAG, "onSendingFileStatus() called with: video.size = ${videos.size}, status = $status")
        videos.forEach { video ->
            val index = videoAdapter.getData().indexOfFirst { item ->  item is Video && item.videoId == video.videoId }
            if(index != -1) {
                runOnUIThread {
                    videoAdapter.getVideo(index).isSending = status == FileService.FILE_SENDING
                    videoAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    override fun onEgpConnectionChanged(isConnected: Boolean, isGroupOwner: Boolean) {
        Log.d(PersonalVideoFragment.TAG, "onEgpConnectionChanged() called with: isConnected = $isConnected, isGroupOwner = $isGroupOwner")
        if(isConnected) {
            hideViews.remove(R.id.btn_quick_share)
        } else {
            hideViews.add(R.id.btn_quick_share)
        }
    }

    companion object {
        val TAG = ListVideosFragment::class.simpleName

    }

}