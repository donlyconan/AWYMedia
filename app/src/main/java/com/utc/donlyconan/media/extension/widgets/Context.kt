package com.utc.donlyconan.media.extension.widgets

import android.content.Context
import android.widget.Toast

val TAG = "AwyMediaApplication"

fun Context.showMessage(msg: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.showMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}