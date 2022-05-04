package com.utc.donlyconan.media.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.databinding.FragmentSettingsBinding
import com.utc.donlyconan.media.views.BaseFragment


/**
 * This class is settings screen
 */
class SettingsFragment : BaseFragment() {

    val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.setTheme(R.style.Theme_Settings)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.apply {
            setSupportActionBar(binding.toolbar)
            setTitle(R.string.settings)
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    class SettingsScreen : PreferenceFragmentCompat() {

        val listLanguage by lazy { findPreference<ListPreference>("language") }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

    }

    companion object {
        val TAG = SettingsFragment::class.simpleName
    }
}