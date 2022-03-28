package com.utc.donlyconan.media.views.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.views.adapter.MainDisplayAdapter
import com.utc.donlyconan.media.databinding.FragmentMainDisplayBinding
import com.utc.donlyconan.media.views.MainActivity

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
        Log.d(TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                    "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
        binding.navBar.setOnItemSelectedListener(onItemSelectedListener)
        val appCompat = activity as MainActivity
        appCompat.setSupportActionBar(binding.appbar.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(PersonalVideoFragment.TAG, "onCreateOptionsMenu() called with: menu = $menu, inflater = $inflater")
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        inflater.inflate(R.menu.menu_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(PersonalVideoFragment.TAG, "onOptionsItemSelected() called with: item = $item")
        when (item.itemId) {
            R.id.it_search -> {
                val action = MainDisplayFragmentDirections
                    .actionMainDisplayFragmentToSearchBarFragment()
                findNavController().navigate(action)
            }
            else -> {
                Log.d(PersonalVideoFragment.TAG, "onOptionsItemSelected: item not found!")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver,
            IntentFilter(ACTION_SHOW_LOADING).apply {
                addAction(ACTION_HIDE_LOADING)
                addAction(ACTION_SHOW_NO_DATA_VIEW)
                addAction(ACTION_HIDE_NO_DATA_VIEW)
            })
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.d(TAG, "onReceive() called with: context = $context, intent = $intent")
            when (intent.action) {
                ACTION_SHOW_NO_DATA_VIEW -> {
                    binding.tvNoData.visibility = View.VISIBLE
                }
                ACTION_HIDE_NO_DATA_VIEW -> {
                    binding.tvNoData.visibility = View.INVISIBLE
                }
                ACTION_SHOW_LOADING -> {
                    binding.llLoading.visibility = View.VISIBLE
                }
                ACTION_HIDE_LOADING -> {
                    binding.llLoading.visibility = View.INVISIBLE
                }
            }
        }
    }


    companion object {
        val TAG = MainDisplayFragment.javaClass.simpleName
        val ACTION_SHOW_NO_DATA_VIEW = "com.utc.awymedia.ACTION_SHOW_NO_DATA_VIEW"
        val ACTION_HIDE_NO_DATA_VIEW = "com.utc.awymedia.ACTION_HIDE_NO_DATA_VIEW"
        val ACTION_SHOW_LOADING = "com.utc.awymedia.ACTION_SHOW_LOADING"
        val ACTION_HIDE_LOADING = "com.utc.awymedia.ACTION_HIDE_LOADING"
    }
}

