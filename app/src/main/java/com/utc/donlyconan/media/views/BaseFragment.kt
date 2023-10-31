package com.utc.donlyconan.media.views

import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.extension.components.getMediaUri
import com.utc.donlyconan.media.views.fragments.MainDisplayFragment
import com.utc.donlyconan.media.views.fragments.VideoTask
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This is basic class that will provide some properties for children class
 */
abstract class BaseFragment : Fragment() {

    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as EGMApplication }
    protected val appComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }
    protected val fileManager by lazy { appComponent.getFileManager() }
    // loading screen
    protected var lsBinding: LoadingDataScreenBinding? = null
    protected val settings by lazy { appComponent.getSettings() }
    protected var handlingTask: VideoTask? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val icdLoading = view.findViewById<View>(R.id.icd_loading)
        if(icdLoading != null) {
            Logs.d("onViewCreated: LoadingDataScreenBinding is setup.")
            lsBinding = LoadingDataScreenBinding.bind(icdLoading)
        }
    }

    protected val intentSenderForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        onDeletedResult(result)
    }

    protected val requestPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        onPermissionResult(result)
    }

    fun deleteVideoFromExternalStorage(vararg uris: Uri) {
        Log.d(ListVideosFragment.TAG, "deleteVideoFromExternalStorage() called with: uri = $uris")
        val contentResolver = requireContext().contentResolver
        lifecycleScope.launch(Dispatchers.IO +
                CoroutineExceptionHandler {_, e -> showToast(R.string.toast_when_failed_user_action) }
        ) {
            try {
                uris.forEach { uri ->
                    contentResolver.delete(uri, null, null) > 0
                }
            } catch (e: Exception) {
                val intentSender: IntentSender? = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore
                            .createDeleteRequest(contentResolver, uris.toList())
                            .intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
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
    }

    fun showLoadingScreen() {
        Log.d(MainDisplayFragment.TAG, "showLoadingScreen() called")
        lsBinding?.apply {
            llLoading.visibility = View.VISIBLE
            tvNoData.visibility = View.INVISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun showNoDataScreen() {
        Log.d(ListVideosFragment.TAG, "showNoDataScreen() called")
        lsBinding?.apply {
            llLoading.visibility = View.INVISIBLE
            tvNoData.visibility = View.VISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun hideLoading() {
        Log.d(ListVideosFragment.TAG, "hideLoading() called")
        lifecycleScope.launch(Dispatchers.Main) {
            lsBinding?.apply {
                frameContainer.visibility = View.INVISIBLE
            }
        }
    }

    fun showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) = activity.runOnUiThread {
        Toast.makeText(requireContext(), msg, duration).show()
    }

    fun showToast(msgId: Int, duration: Int = Toast.LENGTH_SHORT) = activity.runOnUiThread {
        showToast(getString(msgId), duration)
    }

    open fun onDeletedResult(result: ActivityResult) {
        Logs.d( "onDeletedResult() called with: result = $result")
    }

    open fun onPermissionResult(result: Map<String, Boolean>) {

    }

    fun startVideoDisplayActivity(videoId: Int, videoUri: String, playlist: Int = -1)  {
        Log.d(ListVideosFragment.TAG, "startPlayingVideo() called with: videoId = $videoId, videoUri = $videoUri")
        lifecycleScope.launch {
            showLoadingScreen()
            launch(Dispatchers.IO) {
                startActivity(VideoDisplayActivity.newIntent(requireContext(), videoId, videoUri, playlist))
                hideLoading()
            }
        }
    }

    fun startPlayMusic(video: Video) {
        Logs.d( "startPlayMusic() called with: video = $video")
        application.getAudioService()?.play(MediaItem.fromUri(video.videoUri))
    }

    fun startPlayMusic(playlist: List<MediaItem>, index: Int = 0) {
        Logs.d( "startPlayMusic() called with: playlist = $playlist, index = $index")
        application.getAudioService()?.play(playlist, index)
    }


    fun checkPermission(vararg permissions: String): Boolean {
        var result = true
        for (permission in permissions) {
            result = result and (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED)
        }
        return result
    }

    fun requestPermissionIfNeed(vararg permissions: String) {
        if(!checkPermission(*permissions)) {
            requestPermissionResult.launch(permissions.toList().toTypedArray())
        }
    }

    fun share(video: Video) {
        if(!video.isSecured) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUri))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File")
            startActivity(Intent.createChooser(intent, "Share File"))
        } else {
            showToast("The video is being protected, You cannot share it!")
        }
    }


    fun lockVideo(video: Video, repository: VideoRepository) {
        Logs.d("lockVideo() called with: video = $video")
        fileManager.saveIntoInternal(video.videoUri.toUri(), video.title ?: "no_name") { uri, newName ->
            try {
                Log.d(ListVideosFragment.TAG, "onItemClick: MediaUri=${video.videoUri.toUri()}")
                deleteVideoFromExternalStorage(video.videoUri.toUri())
                val newVideo = video.copy(isSecured = true, videoUri = uri.toString(), title = newName)
                handlingTask = VideoTask.from(newVideo, succeed = {
                    Log.d(ListVideosFragment.TAG, "onItemClick() file is locked = ${newVideo.videoUri}")
                    // Update the video in the database
                    repository.update(newVideo)
                }, error = {
                    showToast(R.string.request_action_again)
                })
            } catch (e: Exception) {
                showToast(R.string.toast_when_failed_user_action)
            }
        }
    }

    fun unlockVideo(video: Video, videoRepository: VideoRepository) {
        Logs.d("unlockVideo() called with: video = $video")
        fileManager.removeFromInternal(video.title!!) { file ->
            Log.d(ListVideosFragment.TAG, "onItemClick: file = ${file.absolutePath}")
            file.getMediaUri(requireContext()) { uri ->
                val newVideo = video.copy(videoUri =  uri.toString(), isSecured = false)
                Log.d(ListVideosFragment.TAG, "onItemClick() file is locked = ${newVideo.videoUri}")
                videoRepository.update(newVideo)
            }
        }
    }

    fun playMusic(video: Video) {
        Logs.d( "playMusic() called with: video = $video")
        application.getAudioService()?.play(MediaItem.fromUri(video.videoUri))
    }

    suspend fun deleteVideo(video: Video, repository: VideoRepository) {
        Logs.d("deleteVideo() called with: video = $video")
        if(video.isSecured) {
            repository.moveToRecyleBin(video)
        } else {
            val videoUri = video.videoUri.toUri()
            fileManager.saveIntoInternal(videoUri, video.title!!) { uri, name ->
                deleteVideoFromExternalStorage(videoUri)
                val newVideo = video.copy(videoUri = uri.toString(), title = name)
                handlingTask = VideoTask(listOf(newVideo), succeed = {
                    Log.d(ListVideosFragment.TAG, "handle succeeded items")
                    showToast("The file is moved into the Recycle Bin!")
                    lifecycleScope.launch(Dispatchers.IO) {
                        repository.moveToRecyleBin(video)
                    }
                }, error = {
                    Log.d(ListVideosFragment.TAG, "handle error items")
                    showToast("Can't delete the file!")
                    context?.deleteFile(newVideo.videoUri)
                })
            }
        }
    }

}