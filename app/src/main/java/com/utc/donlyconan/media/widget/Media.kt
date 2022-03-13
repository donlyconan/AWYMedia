package com.utc.donlyconan.media.widget

import android.content.Context
import android.net.Uri
import android.provider.Settings.Global.getString
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.utc.donlyconan.media.R


fun Uri.createDataSource(context: Context): ProgressiveMediaSource? {
    val dataSourceFactory: DataSource.Factory =
        DefaultDataSourceFactory(context, context.getString(R.string.app_name))
    return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(this)
}

