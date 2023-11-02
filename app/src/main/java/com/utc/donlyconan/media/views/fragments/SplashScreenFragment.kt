package com.utc.donlyconan.media.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.FragmentSplashScreenBinding
import com.utc.donlyconan.media.views.BaseFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * This represent for First screen when starting app until displayed app
 */
class SplashScreenFragment : BaseFragment() {

    val binding by lazy { FragmentSplashScreenBinding.inflate(layoutInflater) }
    @Inject lateinit var videoRepo: VideoRepository
    @Inject lateinit var listVideoRepo: ListVideoRepository
    @Inject lateinit var trashRepo: TrashRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        (context?.applicationContext as EGMApplication).applicationComponent()
            .inject(this)
        lifecycleScope.launch(Dispatchers.IO) {
            // Load all data from the device
            launch(Dispatchers.IO + CoroutineExceptionHandler { _, e ->
                Log.e(TAG, "onResume: ", e)
            }) {
                videoRepo.sync()
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(600)
            val action =
                SplashScreenFragmentDirections.actionSplashScreenFragmentToMainDisplayFragment()
            val navOptions: NavOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashScreenFragment, true)
                .build()
            findNavController().navigate(action, navOptions)
            if(!settings.isWellcome) {
                settings.isWellcome = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    private suspend fun deleteDataIfNeed() {
        Log.d(TAG, "deleteDataIfNeed() called with erasureCycle=${settings.erasureCycle}")
        val deleteTimePeriod = System.currentTimeMillis() - settings.previousDeletionDate
        // convert to date
        val numberDay = deleteTimePeriod / 86400000.0
        Log.d(TAG, "deleteDataIfNeed: deleteTimePeriod=$deleteTimePeriod, numberDay=$numberDay")
        if (numberDay >= settings.erasureCycle.toDouble()) {
            deleteData()
            settings.previousDeletionDate = System.currentTimeMillis()
        }
    }

    private suspend fun deleteData() {
        Log.d(TAG, "deletingData() called deleteFromStorage=${settings.deleteFromStorage}")
        val trashes = trashRepo.getAllTrashes()
        val contentResolver = application.contentResolver
        if(settings.deleteFromStorage) {
            trashes.forEach { video ->
                contentResolver.delete(Uri.parse(video.videoUri), null, null)
            }
        }
        trashRepo.removeAll()
    }

    companion object {
        val TAG: String = SplashScreenFragment::class.java.simpleName
        const val LIMITED_FOR_SPLASH_SCREEN = 500L
    }
}