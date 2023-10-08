package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.utc.donlyconan.media.app.EGMApplication

open class BaseAndroidViewModel(app: Application): AndroidViewModel(app) {
    val myApp = app as EGMApplication
}