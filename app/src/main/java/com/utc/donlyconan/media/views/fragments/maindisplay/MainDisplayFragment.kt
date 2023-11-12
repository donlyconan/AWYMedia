package com.utc.donlyconan.media.views.fragments.maindisplay

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.navigation.NavigationBarView
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.repo.ListVideoRepository
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.databinding.DialogAboutBinding
import com.utc.donlyconan.media.databinding.FragmentMainDisplayBinding
import com.utc.donlyconan.media.views.BaseFragment
import com.utc.donlyconan.media.views.OnClickTimesListener
import com.utc.donlyconan.media.views.SettingsActivity
import com.utc.donlyconan.media.views.adapter.MainDisplayAdapter
import com.utc.donlyconan.media.views.fragments.options.MenuMoreOptionFragment
import javax.inject.Inject


/**
 * Represent for Main Screen where will info as Navigation and Base View
 */
class MainDisplayFragment : BaseFragment() {

    val binding by lazy { FragmentMainDisplayBinding.inflate(layoutInflater) }
    lateinit var mainDisplayAdapter: MainDisplayAdapter
    var sortedMenu: MenuItem? = null
    var privateFolderEntrance: MenuItem? = null
    private val args by navArgs<MainDisplayFragmentArgs>()
    @Inject lateinit var listVideoRepo: ListVideoRepository
    @Inject lateinit var trashRepo: TrashRepository
    @Inject lateinit var videoRepo: VideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(
            TAG, "onCreateView() called with: inflater = $inflater, container = $container, " +
                    "savedInstanceState = $savedInstanceState")
        setHasOptionsMenu(true)

        // Rotate the fragment when its orientation is the landscape mode
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            binding.navBar.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val navBar = binding.navBar
                    val layoutParams = navBar.layoutParams as ConstraintLayout.LayoutParams
                    val width = binding.viewPager2.layoutParams.width
                    layoutParams.width = width
                    navBar.translationX = - navBar.width.toFloat()/2 + navBar.height.toFloat()/2
                    navBar.requestLayout()

                    val menuView = navBar.getChildAt(0) as? BottomNavigationMenuView
                    menuView?.let { menu ->
                        for (i in 0 until menu.childCount) {
                            val iconView = menu.getChildAt(i)
                            iconView.rotation = -90f
                            iconView.requestLayout()
                        }
                    }
                    binding.navBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }

            })

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            TAG, "onViewCreated() called with: view = $view, " + "savedInstanceState = " +
                    "$savedInstanceState")
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
        binding.navBar.setOnItemSelectedListener(onItemSelectedListener)
        val appCompat = activity
        appCompat.setSupportActionBar(binding.appbar.toolbar)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appbar.toolbar.setOnClickListener(object : OnClickTimesListener() {
            override fun onClickTimes(v: View, times: Int): Boolean {
                Log.d(TAG, "onClickTimes() called with: v = $v, times = $times")
                // Need to click three times before navigating to the private folder
                return if(times >= 3) {
                    val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToPrivateFolder()
                    findNavController().navigate(action)
                    true
                } else {
                    false
                }
            }
        })
        settings.hideEntrance
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
        R.id.nav_playlist to 2, R.id.nav_favorite to 3,
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
            sortedMenu?.isVisible = (navItem == R.id.nav_personal || navItem == R.id.nav_playlist)
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
        privateFolderEntrance = menu.findItem(R.id.it_private_folder)
        privateFolderEntrance?.isVisible = !settings.hideEntrance
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected() called with: item = $item")
        when (item.itemId) {
            R.id.it_settings -> {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.it_search -> {
                val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToSearchBarFragment(
                        binding.viewPager2.currentItem
                    )
                findNavController().navigate(action)
            }
            R.id.it_sync_data -> {
                application.getFileService()?.syncAllVideos()
            }
            R.id.it_sort_by -> {
                val fragment = mainDisplayAdapter.getFragment(binding.viewPager2.currentItem)
               if(fragment is PersonalVideoFragment) {
                   fragment.let { frag ->
                        val checkId = if(settings.sortBy == Settings.SORT_VIDEO_BY_CREATION_UP) {
                            R.id.btn_sort_by_creation_up
                        } else {
                            R.id.btn_sort_by_creation_down
                        }
                       MenuMoreOptionFragment.newInstance(R.layout.fragment_sort_video_option, listener = frag)
                           .setCheckedId(checkId)
                           .show(supportFragmentManager, TAG)
                   }
               } else if(fragment is PlaylistFragment) {
                   fragment.let { frag ->
                       val checkId = when(settings.playlistSortBy) {
                           Settings.SORT_BY_NAME_UP -> R.id.btn_sort_by_name_up
                           else -> R.id.btn_sort_by_name_down
                       }
                       MenuMoreOptionFragment.newInstance(R.layout.fragment_sort_music_option, listener = frag)
                           .setCheckedId(checkId)
                           .show(supportFragmentManager, TAG)
                   }
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
            R.id.it_private_folder -> {
                val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToPrivateFolder()
                findNavController().navigate(action)
            }
            R.id.it_local_interaction -> {
                val action = MainDisplayFragmentDirections.actionMainDisplayFragmentToInteractionManagerFragment()
                findNavController().navigate(action)
            }
            else -> {
                Log.d(TAG, "onOptionsItemSelected: item not found!")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        privateFolderEntrance?.isVisible = !settings.hideEntrance
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

