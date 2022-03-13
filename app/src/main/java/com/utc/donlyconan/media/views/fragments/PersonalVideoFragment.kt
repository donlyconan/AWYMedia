package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.databinding.FragmentPersonalVideoBinding

class PersonalVideoFragment : Fragment() {

    val binding by lazy { FragmentPersonalVideoBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                "savedInstanceState = $savedInstanceState")
        return binding.root
    }

    companion object {
        val TAG = PersonalVideoFragment.javaClass.simpleName

        fun newInstance() = PersonalVideoFragment()
    }
}