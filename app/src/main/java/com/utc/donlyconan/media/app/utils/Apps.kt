package com.utc.donlyconan.media.app.utils

import android.os.Environment
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utc.donlyconan.media.R
import java.io.File
import java.util.Calendar


const val TYPE_SUBTITLE = "*/*.srt"

fun androidFile(path: String): File {
    return Environment.getExternalStoragePublicDirectory(path)
}

fun now(): Long {
    return Calendar.getInstance().timeInMillis
}

fun ImageView.setVideoImage(uri: String?, circle: Boolean = false) {
    val request = Glide.with(context.applicationContext)
        .load(uri?.toUri())
        .placeholder(R.drawable.im_loading)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .error(R.drawable.img_error)
        .frame(1000L)
        .fitCenter()
    if(circle) {
        request.circleCrop()
    }
    request.into(this)
}