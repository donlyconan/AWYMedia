package com.utc.donlyconan.media.views.fragments.options

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.extension.widgets.hideSystemUi


open class EGMBaseSheetFragment: BottomSheetDialogFragment() {

    var isFullscreen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(if(isFullscreen) STYLE_NO_FRAME else STYLE_NORMAL, R.style.SheetDialogFullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        })
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
        val TAG: String = EGMBaseSheetFragment::class.java.simpleName
    }

}