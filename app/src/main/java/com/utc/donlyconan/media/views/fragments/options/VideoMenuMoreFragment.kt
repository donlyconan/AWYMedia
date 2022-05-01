package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentVideoMenuMoreBinding


class VideoMenuMoreFragment : BaseBottomSheetFragment(), View.OnClickListener {

    val binding by lazy { FragmentVideoMenuMoreBinding.inflate(layoutInflater) }
    var listener: View.OnClickListener ?= null
    var canNext = false
    var isSelected = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: isSelected=$isSelected")
        super.onViewCreated(view, savedInstanceState)
        binding.exoLock.setOnClickListener(this)
        binding.exoLoop.setOnClickListener(this)
        binding.exoSubtitle.setOnClickListener(this)
        binding.exoNext.setOnClickListener(this)
        binding.exoPlaybackSpeed.setOnClickListener(this)
        if(!canNext) {
            binding.exoNext.isEnabled = false
        }
        binding.exoLoop.isSelected = isSelected
    }

    override fun onClick(v: View?) {
        listener?.onClick(v)
        dismiss()
    }

    companion object {
        val TAG: String = VideoMenuMoreFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(listener:View.OnClickListener, isSelected: Boolean) =
            VideoMenuMoreFragment().apply {
                this.isSelected = isSelected
                this.listener = listener
        }
    }
}