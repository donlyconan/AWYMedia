package com.utc.donlyconan.media.app.utils

import android.media.ThumbnailUtils
import android.os.Environment
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.utc.donlyconan.media.R
import java.io.File
import java.util.Calendar


const val TYPE_SUBTITLE = "*/.srt"

fun androidFile(path: String): File {
    return Environment.getExternalStoragePublicDirectory(path)
}

fun now(): Long {
    return Calendar.getInstance().timeInMillis
}

fun ImageView.setVideoImage(uri: String) {
    Glide.with(context.applicationContext)
        .applyDefaultRequestOptions(
            RequestOptions()
                .fallback(R.drawable.ic_baseline_error_24)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
        .load(uri.toUri())
        .placeholder(R.drawable.im_loading)
        .error(R.drawable.img_error)
        .fitCenter()
        .into(this)
}