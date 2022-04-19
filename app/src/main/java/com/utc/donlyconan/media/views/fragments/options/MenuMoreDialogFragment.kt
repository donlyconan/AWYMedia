package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentOptionalSelectionBinding

class MenuMoreDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private val binding by lazy { FragmentOptionalSelectionBinding.inflate(layoutInflater) }
    private var listener: OnItemClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                savedInstanceState)
    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick() called with: view = $view")
        dismiss()
        listener?.onItemClick(view)
    }

    interface OnItemClickListener {
        fun onItemClick(v: View)
    }

    companion object {
        const val TAG = "ActionBottomDialog"
        fun newInstance(): MenuMoreDialogFragment {
            return MenuMoreDialogFragment()
        }
    }
}