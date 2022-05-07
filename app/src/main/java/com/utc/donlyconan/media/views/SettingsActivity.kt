package com.utc.donlyconan.media.views

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.databinding.FragmentSettingsBinding
import com.utc.donlyconan.media.extension.widgets.setLocale
import com.utc.donlyconan.media.views.fragments.SettingsFragment
import javax.inject.Inject

/**
 * This class is Settings screen
 */
class SettingsActivity : BaseActivity() {

    val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setTitle(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }
        val tvTitle = binding.toolbar::class.java.getDeclaredField("mTitleTextView")
            .apply {
                isAccessible = true
            }
            .get(binding.toolbar) as TextView
        tvTitle.setBackgroundColor(Color.TRANSPARENT)
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