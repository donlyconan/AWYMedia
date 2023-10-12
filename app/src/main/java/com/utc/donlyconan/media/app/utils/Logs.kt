package com.utc.donlyconan.media.app.utils

import android.util.Log
import timber.log.Timber

//import timber.log.Timber

object Logs {

    const val DEBUG_MODE = true


    fun d(tag: String, msg: String) = Log.d(tag, msg)

    fun d(msg: String) = Timber.d(msg)

}