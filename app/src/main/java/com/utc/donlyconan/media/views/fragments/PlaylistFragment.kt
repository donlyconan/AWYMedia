package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentPlaylistBinding


class PlaylistFragment : Fragment() {

    val binding by lazy { FragmentPlaylistBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
    }

    companion object {
        val TAG: String = PlaylistFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PlaylistFragment {
            return PlaylistFragment()
        }
    }
}