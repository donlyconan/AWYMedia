package com.utc.donlyconan.media.views

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.utc.donlyconan.media.R
import java.util.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
    }

}