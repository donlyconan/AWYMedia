package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.utc.donlyconan.media.R

/**
 * A simple [Fragment] subclass.
 * Use the [ListVideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListVideoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_video, container, false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListVideoFragment.
         */
        fun newInstance(): ListVideoFragment {
            val fragment = ListVideoFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}