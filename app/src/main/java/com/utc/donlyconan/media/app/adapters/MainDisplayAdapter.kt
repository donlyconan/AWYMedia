package com.utc.donlyconan.media.app.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utc.donlyconan.media.views.fragments.ListVideoFragment

class MainDisplayAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int {
        return 1
    }

    override fun createFragment(position: Int): Fragment {
        return ListVideoFragment.newInstance()
    }

}