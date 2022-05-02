package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSettingsBinding


class SettingsFragment : PreferenceFragmentCompat() {

    val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}