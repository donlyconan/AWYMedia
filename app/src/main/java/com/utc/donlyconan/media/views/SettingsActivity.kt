package com.utc.donlyconan.media.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.databinding.ActivitySettingsBinding

/**
 * This class is Settings screen
 */
class SettingsActivity : BaseActivity() {

    val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Settings)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setTitle(R.string.settings)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        val tvTitle = binding.toolbar::class.java.getDeclaredField("mTitleTextView")
            .apply {
                isAccessible = true
            }
            .get(binding.toolbar) as TextView
        tvTitle.setBackgroundColor(Color.TRANSPARENT)
    }

    class SettingsScreen : PreferenceFragmentCompat() {

        private val listLanguage by lazy { findPreference<ListPreference>("language") }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            Log.d(TAG, "onCreatePreferences: ")
            addPreferencesFromResource(R.xml.preferences)
            val settings = (context?.applicationContext as? EGMApplication)
                ?.applicationComponent()?.getSettings()

            listLanguage?.setOnPreferenceChangeListener { preference, newValue ->
                Log.d(TAG, "onCreatePreferences() called with: preference = $preference, newValue = $newValue")
                if(newValue != settings?.language) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                true
            }
        }

    }

    companion object {
        val TAG = SettingsActivity::class.simpleName
        const val EXTRA_NEW_TASK = "apply_new_task"
    }

}