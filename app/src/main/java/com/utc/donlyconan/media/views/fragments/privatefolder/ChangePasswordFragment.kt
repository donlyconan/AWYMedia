package com.utc.donlyconan.media.views.fragments.privatefolder

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    companion object {
        const val TYPE_REGISTER_PASSWORD = 1
        const val TYPE_CHANGE_PASSWORD = 2

        fun newInstance() = ChangePasswordFragment()
    }

    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.event.observe(this) {

        }
    }


}