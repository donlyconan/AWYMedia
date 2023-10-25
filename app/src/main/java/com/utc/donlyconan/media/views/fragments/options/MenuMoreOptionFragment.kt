package com.utc.donlyconan.media.views.fragments.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.utc.donlyconan.media.R

/**
 * Responsible for showing a Bottom Dialog which will provide some options to the user
 */
class MenuMoreOptionFragment: BottomSheetDialogFragment(), View.OnClickListener {

    private var listener: View.OnClickListener? = null
    private val layoutId by lazy { arguments!!.getInt(EXTRA_LAYOUT_ID) }
    private val map by lazy { HashMap<Int, Boolean>() }
    private var onInitialView: OnInitialView? = null
    private var viewGones = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: layoutId=$layoutId")
        return layoutInflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        val container = view.findViewById<ViewGroup>(R.id.container)
        for (i in 0 until container.childCount) {
            container.getChildAt(i)?.let { view ->
                view.setOnClickListener(this)
                view.isSelected = map[view.id] ?: false
                if(viewGones.contains(view.id)) {
                    view.visibility = View.GONE
                }
            }
        }
        onInitialView?.onInitial(view)
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        })
    }

    fun setOnInitialView(listener: OnInitialView): MenuMoreOptionFragment {
        onInitialView = listener
        return this
    }

    fun setVisibility(viewId: Int): MenuMoreOptionFragment {
        Log.d(TAG, "setVisibility() called with: viewId = $viewId")
        viewGones.add(viewId)
        return this
    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick() called with: view = $view")
        listener?.onClick(view)
        dismiss()
    }

    fun setViewState(viewId: Int, isSelected: Boolean): MenuMoreOptionFragment {
        Log.d(TAG, "setSelectedView() called with: viewId = $viewId, isSelected = $isSelected")
        map[viewId] = isSelected
        return this
    }

    interface OnInitialView {
        fun onInitial(v: View)
    }

    companion object {
        
        val TAG = MenuMoreOptionFragment::class.simpleName
        const val EXTRA_LAYOUT_ID = "layout_id"

        fun newInstance(layoutId: Int,   listener: View.OnClickListener? = null) : MenuMoreOptionFragment {
            val instance = MenuMoreOptionFragment()
            instance.listener = listener
            instance.arguments = bundleOf(EXTRA_LAYOUT_ID to layoutId)
            return instance
        }
        
    }

}