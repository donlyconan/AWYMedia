package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.views.fragments.adapter.MainDisplayAdapter
import com.utc.donlyconan.media.databinding.FragmentMainDisplayBinding

/**
 * Represent for Main Screen where will info as Navigation and Base View
 */
class MainDisplayFragment : Fragment() {
    val binding by lazy { FragmentMainDisplayBinding.inflate(layoutInflater) }
    lateinit var mainDisplayAdapter: MainDisplayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(
            TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState"
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                    "$savedInstanceState"
        )
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
        binding.navBar.setOnItemSelectedListener(onItemSelectedListener)
    }

    private fun setUpViewPager() {
        Log.d(TAG, "setUpViewPager() called")
        mainDisplayAdapter = MainDisplayAdapter(this)
        binding.viewPager2.apply {
            adapter = mainDisplayAdapter
            registerOnPageChangeCallback(onPageChangedCallBackListener)
        }
    }


    private val mappingFunc = hashMapOf(R.id.nav_personal to 0, R.id.nav_recent to 1,
        R.id.nav_shared to 2, R.id.nav_favorite to 3,)

    private val onPageChangedCallBackListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val navItem = mappingFunc.keys.find { key ->
                val value = mappingFunc[key]
                value == position
            } ?: 0
            Log.d(TAG, "onPageSelected() called with: position = $position, navItem=$navItem")
            binding.navBar.selectedItemId = navItem
        }
    }

    private val onItemSelectedListener = object : NavigationBarView.OnItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            Log.d(TAG, "onNavigationItemSelected() called with: item = $item")
            val position = mappingFunc[item.itemId]
            if(position != null) {
                binding.viewPager2.currentItem = position
                return true
            }
            return false
        }
    }

    companion object {
        val TAG = MainDisplayFragment.javaClass.simpleName
    }
}

