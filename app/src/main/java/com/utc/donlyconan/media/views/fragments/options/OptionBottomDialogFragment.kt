package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.databinding.FragmentOptionalSelectionBinding

class OptionBottomDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private val binding by lazy { FragmentOptionalSelectionBinding.inflate(layoutInflater) }
    private var listener: View.OnClickListener? = null
    private lateinit var video: Video

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
        video = arguments?.getParcelable(KEY_VIDEO)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                savedInstanceState)
        binding.btnPlay.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        binding.btnFavorite.setOnClickListener(this)
        binding.btnShare.setOnClickListener(this)
        binding.btnPlayMusic.setOnClickListener(this)
        binding.btnFavorite.isSelected = video.isFavorite
    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick() called with: view = $view")
        listener?.onClick(view)
        dismiss()
    }

    companion object {
        const val TAG = "ActionBottomDialog"
        val KEY_VIDEO = "KEY_VIDEO"
        fun newInstance(video: Video, listener: View.OnClickListener) = OptionBottomDialogFragment().apply {
            this.listener = listener
            arguments = bundleOf(KEY_VIDEO to video)
        }
    }
}