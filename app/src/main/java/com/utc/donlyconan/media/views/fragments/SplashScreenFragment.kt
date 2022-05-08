package com.utc.donlyconan.media.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.FragmentSplashScreenBinding
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SplashScreenFragment : BaseFragment() {

    val binding by lazy { FragmentSplashScreenBinding.inflate(layoutInflater) }
    @Inject lateinit var videoRepo: VideoRepository
    @Inject lateinit var listVideoRepo: ListVideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        (context?.applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        Log.d(TAG, "onResume() called")
        super.onResume()
        lifecycleScope.launch {
            delay(500L)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onViewCreated: loading[${settings.autoDownload}]...")
                if (!settings.autoDownload) {
                    Log.d(TAG, "insertDataIntoDbIfNeed: Database had been loaded!")
                } else {
                    val videoList = listVideoRepo.loadAllVideos()
                    Log.d(TAG, "insertDataIntoDb: loaded size = " + videoList.size)
                    videoRepo.insert(*videoList.toTypedArray())
                }
            }
            activity.runOnUiThread {
                val action = SplashScreenFragmentDirections.actionSplashScreenFragmentToMainDisplayFragment()
                val navOptions: NavOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashScreenFragment, true)
                    .build()
                findNavController().navigate(action, navOptions)
            }
            if(!settings.isWellcome) {
                settings.isWellcome = true
            }
        }
    }

    companion object {
        val TAG: String = SplashScreenFragment::class.java.simpleName
    }
}