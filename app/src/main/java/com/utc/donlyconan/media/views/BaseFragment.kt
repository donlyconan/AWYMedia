package com.utc.donlyconan.media.views

import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.databinding.LoadingDataScreenBinding
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideoFragment


/**
 * This is basic class that will provide some properties for children class
 */
open class BaseFragment : Fragment() {
    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as AwyMediaApplication }
    protected val applicationComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }
    protected var lBinding: LoadingDataScreenBinding? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun showLoadingScreen() {
        Log.d(ListVideoFragment.TAG, "showLoadingScreen() called")
        lBinding?.apply {
            llLoading.visibility = View.VISIBLE
            tvNoData.visibility = View.INVISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun showNoDataScreen() {
        Log.d(ListVideoFragment.TAG, "showNoDataScreen() called")
        lBinding?.apply {
            llLoading.visibility = View.INVISIBLE
            tvNoData.visibility = View.VISIBLE
            frameContainer.visibility = View.VISIBLE
        }
    }

    fun hideLoading() {
        Log.d(ListVideoFragment.TAG, "hideLoading() called")
        lBinding?.apply {
            frameContainer.visibility = View.INVISIBLE
        }
    }


}