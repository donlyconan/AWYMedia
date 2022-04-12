package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSortMusicOptionBinding
import com.utc.donlyconan.media.extension.widgets.TAG

class ArrangementVideoBottomDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private val binding by lazy { FragmentSortMusicOptionBinding.inflate(layoutInflater) }
    private var listener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                savedInstanceState)
        binding.btnSortByName.setOnClickListener(this)
        binding.btnSortByCreation.setOnClickListener(this)
        binding.btnSortByRecent.setOnClickListener(this)
        binding.btnSortBySize.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick() called with: view = $view")
        listener?.onClick(view)
        dismiss()
    }

    companion object {
        fun newInstance(listener: View.OnClickListener?) = ArrangementVideoBottomDialogFragment().apply {
            this.listener = listener
        }
    }
}