package com.utc.donlyconan.media.views.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utc.donlyconan.media.extension.widgets.TAG
import com.utc.donlyconan.media.views.fragments.*

class MainDisplayAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 4 /* tổng số fragment sẽ có */
    }

    override fun createFragment(fragId: Int): Fragment {
        Log.d(TAG, "createFragment() called with: fragId = $fragId")
        when(fragId) {
            MainDisplayFragment.PERSONAL_FRAGMENT -> {
                return PersonalVideoFragment.newInstance()
            }
            MainDisplayFragment.RECENT_FRAGMENT -> {
                return RecentFragment.newInstance()
            }
            MainDisplayFragment.SHARED_FRAGMENT -> {
                return SharedVideoFragment.newInstance()
            }
            MainDisplayFragment.FAVORITE_FRAGMENT -> {
                return FavoriteFragment.newInstance()
            }
            else -> {
                Log.d(TAG, "createFragment: $fragId isn't found!")
            }
        }
        return PersonalVideoFragment.newInstance()
    }

}