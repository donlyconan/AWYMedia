package com.utc.donlyconan.media.views.fragments.privatefolder

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.R

class PrivateFolderFragment : Fragment() {

    companion object {
        fun newInstance() = PrivateFolderFragment()
    }

    private lateinit var viewModel: PrivateFolderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_private_folder, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PrivateFolderViewModel::class.java)
        // TODO: Use the ViewModel
    }

}