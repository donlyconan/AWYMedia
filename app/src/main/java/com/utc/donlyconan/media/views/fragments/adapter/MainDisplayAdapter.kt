package com.utc.donlyconan.media.views.fragments.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utc.donlyconan.media.views.fragments.*

class MainDisplayAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private val fragmentList = listOf<Fragment>(
        PersonalVideoFragment.newInstance(),
        RecentFragment.newInstance(),
        SharedVideoFragment.newInstance(),
        FavoriteFragment.newInstance(),
    )

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}