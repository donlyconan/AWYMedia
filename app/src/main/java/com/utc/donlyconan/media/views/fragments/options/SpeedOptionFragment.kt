package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.SpeedOptionFragmentBinding
import com.utc.donlyconan.media.extension.widgets.OnItemClickListener
import com.utc.donlyconan.media.extension.widgets.hideSystemUi
import com.utc.donlyconan.media.views.adapter.SpeedOptionAdapter


class SpeedOptionFragment : DialogFragment(), OnItemClickListener {

    private val binding by lazy { SpeedOptionFragmentBinding.inflate(layoutInflater) }
    private var onSelectedSpeedChangeListener: OnSelectedSpeedChangeListener? = null
    private val currentSpeed by lazy { arguments?.getFloat(KEY_CURRENT_SPEED) ?: -1f }
    private val speedList = listOf(0.25f, 0.5f, 1f, 1.25f, 1.5f, 2f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SheetDialogFullScreen)
        dialog?.window?.decorView?.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                    savedInstanceState)
        val adapter = SpeedOptionAdapter(requireContext(), speedList)
        adapter.onItemClickListener = this
        adapter.selectedItem = currentSpeed
        binding.recyclerView.adapter = adapter
    }

    override fun onItemClick(v: View, position: Int) {
        Log.d(TAG, "onItemClick() called with: v = $v, position = $position")
        val speed = speedList[position]
        if(currentSpeed != speed) {
            onSelectedSpeedChangeListener?.onSelectedSpeedChanged(speed)
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.decorView?.hideSystemUi()
    }


    interface OnSelectedSpeedChangeListener {
        fun onSelectedSpeedChanged(speed: Float)
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