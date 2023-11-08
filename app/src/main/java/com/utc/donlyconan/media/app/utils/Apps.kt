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

fun ImageView.setVideoImage(uri: String?, circle: Boolean = false, position: Long = 1000L) {
    val request = Glide.with(context.applicationContext)
        .load(uri?.toUri())
        .placeholder(R.drawable.im_loading)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .error(R.drawable.img_error)
        .frame(position)
        .fitCenter()
    if(circle) {
        request.circleCrop()
    }
    request.into(this)
}

fun <T> MutableIterator<out T>.consumeAll(consume: (e: T) -> Unit) {
    try {
        while (hasNext()) {
            val data = next()
            remove()
            consume(data)
        }
    } finally {}
}