package com.utc.donlyconan.media.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.DialogAboutBinding
import com.utc.donlyconan.media.databinding.FragmentMainDisplayBinding
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.adapter.MainDisplayAdapter
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment

/**
 * Represent for Main Screen where will info as Navigation and Base View
 */
class MainDisplayFragment : BaseFragment() {

    val binding by lazy { FragmentMainDisplayBinding.inflate(layoutInflater) }
    lateinit var mainDisplayAdapter: MainDisplayAdapter
    var sortedMenu: MenuItem? = null
    private val args by navArgs<MainDisplayFragmentArgs>()

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
        val appCompat = activity
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
        binding.viewPager2.setCurrentItem(args.screenId, false)
    }


    private val mappingFunc = hashMapOf(
        R.id.nav_personal to 0, R.id.nav_recent to 1,
        R.id.nav_shared to 2, R.id.nav_favorite to 3,
    )

    private val onPageChangedCallBackListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val navItem = mappingFunc.keys.find { key ->
                val value = mappingFunc[key]
                value == position
            } ?: 0
            Log.d(TAG, "onPageSelected() called with: position = $position, navItem=$navItem")
            binding.navBar.selectedItemId = navItem
            sortedMenu?.isVisible = navItem == R.id.nav_personal
        }
    }

    private val onItemSelectedListener = object : NavigationBarView.OnItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            Log.d(TAG, "onNavigationItemSelected() called with: item = $item")
            val position = mappingFunc[item.itemId]
            if (position != null) {
                binding.viewPager2.currentItem = position
                return true
            }
            return false
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu() called with: menu = $menu, inflater = $inflater")
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        inflater.inflate(R.menu.menu_bar, menu)
        sortedMenu = menu.findItem(R.id.it_sort_by);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected() called with: item = $item")
        when (item.itemId) {
            R.id.it_search -> {
                val action = MainDisplayFragmentDirections
                    .actionMainDisplayFragmentToSearchBarFragment(binding.viewPager2.currentItem)
                findNavController().navigate(action)
            }
            R.id.it_sort_by -> {
                val fragment = mainDisplayAdapter.getFragment(binding.viewPager2.currentItem)
                        as? PersonalVideoFragment
                fragment?.let { frag ->
                    MenuMoreOptionFragment.newInstance(R.layout.fragment_sorted_video_option, frag)
                        .show(supportFragmentManager, TAG)
                }
            }
            R.id.it_trash -> {
                val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToTrashFragment()
                findNavController().navigate(action)
            }
            R.id.it_about -> {
                val binding = DialogAboutBinding.inflate(layoutInflater)
                AlertDialog.Builder(context!!)
                    .setView(binding.root)
                    .show()
            }
            R.id.it_help -> {
                val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToHelpAndFeedbackFragment()
                findNavController().navigate(action)
            }
            else -> {
                Log.d(TAG, "onOptionsItemSelected: item not found!")
            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        // Bộ const để ánh xạ fragment
        val TAG = MainDisplayFragment::class.simpleName
        const val PERSONAL_FRAGMENT = 0
        const val RECENT_FRAGMENT = 1
        const val PLAYLIST_FRAGMENT = 2
        const val FAVORITE_FRAGMENT = 3
    }
}

