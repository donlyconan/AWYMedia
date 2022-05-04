package com.utc.donlyconan.media.extension.widgets

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import java.util.*


val TAG = "AwyMediaApplication"

fun Context.showMessage(msg: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.showMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.setLocale(languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
}