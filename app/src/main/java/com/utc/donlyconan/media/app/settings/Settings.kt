package com.donly.sradatn.app.settings

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Settings @Inject constructor(context: Context) {
    companion object {
        const val SETTINGS_NAME = "sra-datn"

        // const of default language
        const val DEFAULT_LANGUAGE = "en"

        // const of key
        const val KEY_LANGUAGE = "keys.language"
    }

    var preferences: SharedPreferences =
        context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    val language by StringPreferenceDelegate(preferences, KEY_LANGUAGE, DEFAULT_LANGUAGE)

}