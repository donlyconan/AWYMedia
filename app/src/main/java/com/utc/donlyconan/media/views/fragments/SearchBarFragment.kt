package com.utc.donlyconan.media.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSearchBarBinding

class SearchBarFragment : Fragment(), View.OnClickListener {

    private val binding by lazy { FragmentSearchBarBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText = binding.searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(Color.WHITE)
        editText.setHintTextColor(resources.getColor(R.color.search_bar_hint))
        val navClose = binding.searchBar.findViewById<View>(androidx.appcompat.R.id.search_close_btn)
       navClose.setOnClickListener(onClickListener)
    }

    private val onClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            Log.d(TAG, "onClick() called with: v = $v")
            when(view?.id) {
                androidx.appcompat.R.id.search_close_btn -> {
                    findNavController().navigateUp()
                }
                else -> {
                    Log.d(TAG, "onClick haven't been handled yet!")
                }
            }
        }
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick() called with: v = $v")

    }

    companion object {
        val TAG: String = SearchBarFragment::class.java.simpleName
    }
}