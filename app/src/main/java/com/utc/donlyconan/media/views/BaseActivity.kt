package com.utc.donlyconan.media.views

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.extension.widgets.setLocale
import com.utc.donlyconan.media.views.fragments.MainDisplayFragment
import java.util.*
import javax.inject.Inject

open class BaseActivity: AppCompatActivity() {

    companion object {
        val  TAG = BaseActivity::class.simpleName
    }

    @Inject lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AwyMediaApplication)
            .applicationComponent()
            .inject(this)
        val langCode = settings.language.lowercase()
        Log.d(TAG, "onCreate: language=${langCode}")
        setLocale(langCode)
        Log.d(TAG, "onCreate: current language=${resources.configuration.locales[0]}")
        super.onCreate(savedInstanceState)
    }

}