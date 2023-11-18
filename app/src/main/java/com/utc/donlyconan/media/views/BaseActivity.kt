package com.utc.donlyconan.media.views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.app.utils.setDetailedFormatDate
import com.utc.donlyconan.media.app.utils.setFormatDate
import com.utc.donlyconan.media.extension.widgets.setLocale
import java.util.Locale
import javax.inject.Inject

open class BaseActivity: AppCompatActivity() {

    companion object {
        val  TAG = BaseActivity::class.simpleName
    }

    @Inject lateinit var settings: Settings
    protected val application by lazy { applicationContext as EGMApplication }
    protected val appComponent by lazy { application.applicationComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as EGMApplication)
            .applicationComponent()
            .inject(this)
        val langCode = settings.language.lowercase()
        Log.d(TAG, "onCreate: language=${langCode}")
        setLocale(langCode)
        Log.d(TAG, "onCreate: current language=${resources.configuration.locales[0]}")
        setFormatDate(Locale.forLanguageTag(langCode))
        setDetailedFormatDate(Locale.forLanguageTag(langCode))
        super.onCreate(savedInstanceState)
    }

}