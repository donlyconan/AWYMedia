package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.databinding.FragmentVideoMenuMoreBinding


class VideoMenuMoreFragment : EGMBaseSheetFragment(), View.OnClickListener {

    val binding by lazy { FragmentVideoMenuMoreBinding.inflate(layoutInflater) }
    var listener: View.OnClickListener ?= null
    var isSelected = false
    var hasNext = false
    var hasPrev = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: isSelected=$isSelected")
        super.onViewCreated(view, savedInstanceState)
        binding.exoLock.setOnClickListener(this)
        binding.exoLoop.setOnClickListener(this)
        binding.btnPrev.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.exoPlayMusic.setOnClickListener(this)
        binding.exoPlaybackSpeed.setOnClickListener(this)
        binding.exoLoop.isSelected = isSelected

        if(!hasPrev) {
            binding.btnPrev.alpha = 0.3f
            binding.btnPrev.isClickable = false
        }
        if(!hasNext) {
            binding.btnNext.alpha = 0.3f
            binding.btnNext.isClickable = false
        }
    }

    override fun onClick(v: View?) {
        listener?.onClick(v)
        dismiss()
    }

    companion object {
        val TAG: String = VideoMenuMoreFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(isSelected: Boolean, hasNext: Boolean, hasPrev: Boolean ,listener:View.OnClickListener) =
            VideoMenuMoreFragment().apply {
                this.isSelected = isSelected
                this.listener = listener
                this.hasNext = hasNext
                this.hasPrev = hasPrev
        }
    }
}