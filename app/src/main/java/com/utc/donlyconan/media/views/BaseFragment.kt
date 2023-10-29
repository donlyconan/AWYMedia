package com.utc.donlyconan.media.views

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.MediaItem
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.views.fragments.MainDisplayFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    protected val intentSenderForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        onDeletedResult(result)
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
        lsBinding?.apply {
            frameContainer.visibility = View.INVISIBLE
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

    fun startPlayingVideo(videoId: Int, videoUri: String, playlist: Int = -1)  {
        Log.d(ListVideosFragment.TAG, "startPlayingVideo() called with: videoId = $videoId, videoUri = $videoUri")
        lifecycleScope.launch {
            showLoadingScreen()
            launch(Dispatchers.IO) {
                VideoDisplayActivity.newIntent(requireContext(), videoId, videoUri, playlist).let { startActivity(it) }
                hideLoading()
            }
        }
    }

    fun startPlayMusic(video: Video) {
        Logs.d( "startPlayMusic() called with: video = $video")
        application.getAudioService()?.play(MediaItem.fromUri(video.videoUri))
    }

}