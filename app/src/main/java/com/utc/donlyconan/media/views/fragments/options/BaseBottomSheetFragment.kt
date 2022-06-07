package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.extension.widgets.hideSystemUi

open class BaseBottomSheetFragment: BottomSheetDialogFragment() {

    protected var isFullscreen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(if(isFullscreen) STYLE_NO_FRAME else STYLE_NORMAL, R.style.SheetDialogFullScreen)
    }

    override fun onResume() {
        Log.d(TAG, "onResume() called isFullscreen=$isFullscreen")
        super.onResume()
        if(isFullscreen) {
            dialog?.window?.apply {
                setBackgroundDrawableResource(R.color.transparent)
                dialog?.window?.decorView?.hideSystemUi()
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
    }

    companion object {
        val TAG: String = BaseBottomSheetFragment::class.java.simpleName
    }

}