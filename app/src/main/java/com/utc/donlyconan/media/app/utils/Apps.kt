package com.utc.donlyconan.media.app.utils

import android.os.Environment
import java.io.File
import java.util.Calendar


const val TYPE_SUBTITLE = "*/.srt"

fun androidFile(path: String): File {
    return Environment.getExternalStoragePublicDirectory(path)
}

fun now(): Long {
    return Calendar.getInstance().timeInMillis
}