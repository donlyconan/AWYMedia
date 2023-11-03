package com.utc.donlyconan.media.views.fragments.options

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.SpeedOptionFragmentBinding
import com.utc.donlyconan.media.extension.widgets.hideSystemUi


class SpeedOptionFragment : EGMBaseSheetFragment(), OnClickListener {

    private val binding by lazy { SpeedOptionFragmentBinding.inflate(layoutInflater) }
    private var onSelectedSpeedChangeListener: OnSelectedSpeedChangeListener? = null
    private val currentSpeed by lazy { arguments?.getFloat(KEY_CURRENT_SPEED) ?: 1.0f }
    private val speedLevel = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireActivity(), R.style.SheetDialogFullScreen).apply {
            isCancelable = true
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                    savedInstanceState)
        var indexOfSpeed = speedLevel.indexOf(currentSpeed)
        if(indexOfSpeed < 0) {
            indexOfSpeed = 2
        }
        binding.container.children.filterIsInstance<TextView>()
            .forEachIndexed { ind, v ->
                v.setOnClickListener(this)
                if (indexOfSpeed == ind) {
                    v.isSelected = true
                }
            }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.decorView?.hideSystemUi()
    }


    override fun onClick(v: View) {
        val indexOfView = binding.container.children.filterIsInstance<TextView>()
            .indexOfFirst { v.id == it.id }
        val speed = speedLevel[indexOfView]
        if(speed == currentSpeed) {
            onSelectedSpeedChangeListener?.onReselectedSpeed(speed)
        } else {
            onSelectedSpeedChangeListener?.onSelectedSpeedChanged(speed)
        }
        dismiss()
    }


    interface OnSelectedSpeedChangeListener {
        fun onSelectedSpeedChanged(speed: Float)

        fun onReselectedSpeed(speed: Float)
    }

    companion object {
        const val TAG = "SpeedOptionFragment"
        const val KEY_CURRENT_SPEED = "KEY_CURRENT_SPEED"

        fun newInstance(currentSpeed: Float, listener: OnSelectedSpeedChangeListener) =
            SpeedOptionFragment().apply {
                onSelectedSpeedChangeListener = listener
                arguments = bundleOf(KEY_CURRENT_SPEED to currentSpeed)
            }
    }
}