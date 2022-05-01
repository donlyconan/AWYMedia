package com.utc.donlyconan.media.views

import android.content.Context
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.app.AwyMediaApplication


/**
 * This is basic class that will provide some properties for children class
 */
open class BaseFragment : Fragment() {
    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as AwyMediaApplication }
    protected val applicationComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

}