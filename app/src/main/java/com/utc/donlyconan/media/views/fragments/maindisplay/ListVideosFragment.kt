package com.utc.donlyconan.media.views.fragments.maindisplay

import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject

abstract class ListVideosFragment : BaseFragment(), OnItemClickListener {
    
    protected lateinit var videoAdapter: VideoAdapter
    protected var audioService: AudioService? = null
    protected var unlockMode = false
    @Inject lateinit var fileManager: FileManager
    @Inject lateinit var videoRepo: VideoRepository

    protected val intentSenderForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->

    }


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
                        videoRepo.moveToTrash(video)
                    }
                    R.id.btn_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUri))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
                        startActivity(Intent.createChooser(intent, "Share File"))
                    }
                    R.id.btn_lock -> {
                        fileManager.saveIntoInternal(video.videoUri.toUri(), video.title ?: "no_name") { uri ->
                            deleteVideoFromExternalStorage(video.videoUri.toUri())
                            val newVideo = video.copy(isSecured = true, videoUri = uri.toString())
                            videoRepo.update(newVideo)
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

    private fun deleteVideoFromExternalStorage(uri: Uri) {
        Log.d(TAG, "deleteVideoFromExternalStorage() called with: uri = $uri")
        try {
            requireContext().contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            val intentSender: IntentSender? = when {
                Build.VERSION.SDK_INT >= VERSION_CODES.R -> {
                    MediaStore
                        .createDeleteRequest(requireContext()?.contentResolver, listOf(uri))
                        .intentSender
                }
                Build.VERSION.SDK_INT >= VERSION_CODES.Q -> {
                    val exception = e as? RecoverableSecurityException
                    exception?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender?.let { intent ->
                intentSenderForResult.launch(
                    IntentSenderRequest.Builder(intent).build()
                )
            }
        }
    }

    companion object {

        val TAG = ListVideosFragment::class.simpleName

    }

}