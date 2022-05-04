package com.utc.donlyconan.media.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utc.donlyconan.media.app.AwyMediaApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.extension.widgets.setLocale
import javax.inject.Inject

open class BaseActivity: AppCompatActivity() {

    @Inject lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AwyMediaApplication)
            .applicationComponent()
            .inject(this)
        super.onCreate(savedInstanceState)
        setLocale(settings.language)
    }

}