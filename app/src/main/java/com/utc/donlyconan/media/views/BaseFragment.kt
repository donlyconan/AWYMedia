package com.utc.donlyconan.media.views

import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.views.MainActivity


/**
 * This is basic class that will provide some properties for children class
 */
open class BaseFragment : Fragment() {
    protected val activity by lazy { requireActivity() as MainActivity }
    protected val application by lazy { requireContext().applicationContext as AwyMediaApplication }
    protected val applicationComponent by lazy { application.applicationComponent() }
    protected val supportFragmentManager by lazy { activity.supportFragmentManager }
}