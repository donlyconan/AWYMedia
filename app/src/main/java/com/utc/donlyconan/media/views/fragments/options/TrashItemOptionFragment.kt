package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentTrashItemOptionBinding
import com.utc.donlyconan.media.extension.widgets.OnItemLongClickListener
import com.utc.donlyconan.media.views.fragments.TrashFragment
import org.checkerframework.common.subtyping.qual.Bottom


class TrashItemOptionFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private val binding by lazy { FragmentTrashItemOptionBinding.inflate(layoutInflater) }
    var listener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding.btnRestore.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        listener?.onClick(v)
        dismiss()
    }

    companion object {
        val TAG: String = TrashFragment::class.java.simpleName

        fun newInstance(listener: View.OnClickListener): TrashItemOptionFragment {
            return TrashItemOptionFragment().apply {
                this.listener = listener
            }
        }
    }
}