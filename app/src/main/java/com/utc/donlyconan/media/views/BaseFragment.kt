package com.utc.donlyconan.media.views

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.snackbar.Snackbar
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.MainDisplayFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * This is basic class that will provide some properties for children class
 */
abstract class BaseFragment : Fragment() {

    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as EGMApplication }
    protected val appComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }
    // loading screen
    protected var lsBinding: LoadingDataScreenBinding? = null
    protected val settings by lazy { appComponent.getSettings() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val icdLoading = view.findViewById<View>(R.id.icd_loading)
        if(icdLoading != null) {
            Logs.d("onViewCreated: LoadingDataScreenBinding is setup.")
            lsBinding = LoadingDataScreenBinding.bind(icdLoading)
        }
    }

    private val requestPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        onPermissionResult(result)
    }

    fun showLoadingScreen() {
        Log.d(MainDisplayFragment.TAG, "showLoadingScreen() called")
        lifecycleScope.launch(Dispatchers.Main) {
            lsBinding?.apply {
                llLoading.visibility = View.VISIBLE
                tvNoData.visibility = View.INVISIBLE
                frameContainer.setBackgroundResource(R.color.loading_dim_color)
                frameContainer.visibility = View.VISIBLE
                lsBinding!!.frameContainer.setOnTouchListener { v, event ->  true }
            }
        }
    }

    fun showNoDataScreen() {
        Log.d(ListVideosFragment.TAG, "showNoDataScreen() called")
        lifecycleScope.launch(Dispatchers.Main) {
            lsBinding?.apply {
                llLoading.visibility = View.INVISIBLE
                tvNoData.visibility = View.VISIBLE
                frameContainer.visibility = View.VISIBLE
                frameContainer.setBackgroundResource(R.color.transparent)
                frameContainer.setOnTouchListener(null)
            }
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

    open fun onPermissionResult(result: Map<String, Boolean>) {

    }

    fun startVideoDisplayActivity(videoId: Int, videoUri: String, playlist: Int = -1, continued: Boolean = false)  {
        Log.d(ListVideosFragment.TAG, "startPlayingVideo() called with: videoId = $videoId, videoUri = $videoUri")
        lifecycleScope.launch {
            showLoadingScreen()
            launch(Dispatchers.IO) {
                startActivity(VideoDisplayActivity.newIntent(requireContext(), videoId, videoUri, playlist, continued))
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
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shared_file))
            startActivity(Intent.createChooser(intent,  getString(R.string.share_file)))
        } else {
            showToast("The video is being protected, You cannot share it!")
        }
    }


    fun lockVideo(video: Video, repository: VideoRepository, playlistWithVideosDao: PlaylistWithVideosDao) {
        Logs.d("lockVideo() called with: video = $video")
        val videoUri = video.videoUri.toUri()
        executeOnFileService {
            val (newName, uri) = saveIntoInternal(videoUri, video.title ?: "no_name")
            requestDeletingFile(videoUri)
            val newVideo = video.copy(isSecured = true, videoUri = uri.toString(), title = newName)
            playlistWithVideosDao.deleteByVideoId(video.videoId)
            repository.update(newVideo)
        }?.invokeOnCompletion {
            showSnackBar("The file is stored in the app, you can remove it from the external storage.")
        }
    }

    fun unlockVideo(video: Video, videoRepository: VideoRepository) {
        Logs.d("unlockVideo() called with: video = $video")
        executeOnFileService {
            saveIntoExternal(video.title!!) { result, file, uri ->
                Log.d(ListVideosFragment.TAG, "onItemClick: file = ${file?.absolutePath}, uri=$uri")
                val newVideo = video.copy(videoUri = uri.toString(), isSecured = false)
                Log.d(ListVideosFragment.TAG, "onItemClick() file is locked = ${newVideo.videoUri}")
                videoRepository.update(newVideo)
            }
        }
    }

    suspend fun deleteVideo(video: Video, repository: VideoRepository) {
        Logs.d("deleteVideo() called with: video = $video")
        if(video.isSecured) {
            repository.moveToRecycleBin(video)
        } else {
            val videoUri = video.videoUri.toUri()
            executeOnFileService {
                val (filename, uri) = saveIntoInternal(videoUri, video.title!!)
                val newVideo = video.copy(title = filename, videoUri = uri.toString())
                repository.moveToRecycleBin(newVideo)
                requestDeletingFile(videoUri)
                showSnackBar("The file is moved into Recycle Bin, let's remove it from the external storage.")
            }

        }
    }

    fun playMusic(video: Video) {
        Logs.d( "playMusic() called with: video = $video")
        application.getAudioService()?.play(MediaItem.fromUri(video.videoUri))
    }


    fun executeOnFileService(func: suspend FileService.() -> Unit): Job? {
        val instance = application.getFileService()
        if(instance != null) {
            return instance.runIO(func)
        } else {
            showToast(R.string.file_service_is_not_available)
        }
        return null
    }


    fun showSnackBar(msgId: Int) = showSnackBar(getString(msgId))
    fun showSnackBar(msg: String) = lifecycleScope.launch(Dispatchers.Main) {
        Snackbar.make(view!!, msg, Snackbar.LENGTH_SHORT)
    }
}