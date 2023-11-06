package com.utc.donlyconan.media.views

import android.view.View
import android.view.View.OnClickListener
import com.utc.donlyconan.media.app.utils.now

abstract class OnClickTimesListener : OnClickListener {

    companion object {
        const val DELAY_TIME = 300L
    }

    private var times: Int = 1
    private var clickedTime: Long = 0L

    override fun onClick(v: View) {
        if(clickedTime >= now() - DELAY_TIME) {
            val result = onClickTimes(v, times + 1)
            if(result) {
                times = 1
            } else {
                times++
            }
        } else {
            times = 1
        }
        clickedTime = now()
    }

    abstract fun onClickTimes(v: View, times: Int): Boolean

}