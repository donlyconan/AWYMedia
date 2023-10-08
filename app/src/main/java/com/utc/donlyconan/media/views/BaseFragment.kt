package com.utc.donlyconan.media.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.views.fragments.MainDisplayFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment


/**
 * This is basic class that will provide some properties for children class
 */
open class BaseFragment : Fragment() {
    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as EGMApplication }
    protected val applicationComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }
    protected var lBinding: LoadingDataScreenBinding? = null
    protected val settings by lazy { applicationComponent.getSettings() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val icdLoading = view.findViewById<View>(R.id.icd_loading)
        if(icdLoading != null) {
            lBinding = LoadingDataScreenBinding.bind(icdLoading)
        }
    }

    fun showLoadingScreen() {
        Log.d(MainDisplayFragment.TAG, "showLoadingScreen() called")
        lBinding?.apply {
            llLoading.visibility = View.VISIBLE
            tvNoData.visibility = View.INVISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun showNoDataScreen() {
        Log.d(ListVideosFragment.TAG, "showNoDataScreen() called")
        lBinding?.apply {
            llLoading.visibility = View.INVISIBLE
            tvNoData.visibility = View.VISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun hideLoading() {
        Log.d(ListVideosFragment.TAG, "hideLoading() called")
        lBinding?.apply {
            frameContainer.visibility = View.INVISIBLE
        }
    }


}