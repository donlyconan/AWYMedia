package com.utc.donlyconan.media.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.utc.donlyconan.media.app.AwyMediaApplication

open class BaseAndroidViewModel(app: Application): AndroidViewModel(app) {
    val awyApp = app as AwyMediaApplication
}