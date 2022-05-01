package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.databinding.FragmentSplashScreenBinding


class SplashScreenFragment : Fragment() {

    val binding by lazy { FragmentSplashScreenBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        (context?.applicationContext as AwyMediaApplication).applicationComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        val TAG: String = SplashScreenFragment::class.java.simpleName
    }
}