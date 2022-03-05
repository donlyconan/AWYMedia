package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.adapters.MainDisplayAdapter
import com.utc.donlyconan.media.databinding.FragmentMainDisplayBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainDisplayFragment : Fragment() {
    lateinit var binding: FragmentMainDisplayBinding
    lateinit var mainDisplayAdapter: MainDisplayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        binding = FragmentMainDisplayBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)
        mainDisplayAdapter = MainDisplayAdapter(this)
        binding.viewPager.adapter = mainDisplayAdapter
    }

    
    companion object {
        val TAG = MainDisplayFragment.javaClass.simpleName
    }
}