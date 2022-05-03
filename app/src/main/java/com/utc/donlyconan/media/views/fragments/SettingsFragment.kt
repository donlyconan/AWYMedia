package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSettingsBinding
import com.utc.donlyconan.media.views.BaseFragment


class SettingsFragment : BaseFragment() {

    val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    class SettingsScreen : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

    }
}