package com.utc.donlyconan.media.views.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.views.fragments.maindisplay.FavoriteFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.MainDisplayFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PersonalVideoFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.PlaylistFragment
import com.utc.donlyconan.media.views.fragments.maindisplay.RecentFragment

class MainDisplayAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private val fragMap = HashMap<Int, Fragment>()

    fun getFragment(fragId: Int) = fragMap[fragId]

    override fun getItemCount(): Int {
        return 4 /* tổng số fragment sẽ có */
    }

    override fun createFragment(fragId: Int): Fragment {
        Log.d(TAG, "createFragment() called with: fragId = $fragId")
        var fragment: Fragment? = null
        if(!fragMap.containsKey(fragId)) {
            when(fragId) {
                MainDisplayFragment.PERSONAL_FRAGMENT -> {
                    fragment = PersonalVideoFragment.newInstance()
                }
                MainDisplayFragment.RECENT_FRAGMENT -> {
                    fragment = RecentFragment.newInstance()
                }
                MainDisplayFragment.PLAYLIST_FRAGMENT -> {
                    fragment = PlaylistFragment.newInstance()
                }
                MainDisplayFragment.FAVORITE_FRAGMENT -> {
                    fragment = FavoriteFragment.newInstance()
                }
                else -> {
                    Log.d(TAG, "createFragment: $fragId isn't found!")
                }
            }
            fragMap[fragId] = fragment ?: PersonalVideoFragment.newInstance()
        }
        return fragMap[fragId]!!
    }

}