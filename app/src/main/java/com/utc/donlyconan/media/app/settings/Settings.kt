package com.utc.donlyconan.media.app.settings

import android.content.Context
import com.donly.sradatn.app.settings.BooleanPreferenceDelegate
import com.donly.sradatn.app.settings.IntPreferenceDelegate
import com.donly.sradatn.app.settings.StringPreferenceDelegate

class Settings(val appContext: Context) {

    companion object {
        const val SETTINGS_NAME = "sra-datn"
        // const of default language
        const val DEFAULT_LANGUAGE = "en"
        // options of sort by
        const val SORT_BY_NAME = 1
        const val SORT_BY_CREATION = 2
        const val SORT_BY_RECENT = 3
        const val SORT_BY_DURATION = 4

        private var instance: Settings? = null

        fun getInstance(appContext: Context) = synchronized(Settings::class.java) {
            if(instance == null){
                instance = Settings(appContext)
            }
            instance!!
        }
    }

    private var preferences = appContext.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    val language by StringPreferenceDelegate(preferences, "language", DEFAULT_LANGUAGE)
    val isLoggedIn by BooleanPreferenceDelegate(preferences, "logged_in", false)
    val sortBy by IntPreferenceDelegate(preferences, "sort_by", SORT_BY_NAME)

}
