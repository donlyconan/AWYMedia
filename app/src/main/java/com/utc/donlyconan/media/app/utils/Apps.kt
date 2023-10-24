package com.utc.donlyconan.media.app.utils

import android.os.Environment
import java.io.File


const val TYPE_SUBTITLE = "*/.srt"

fun androidFile(path: String): File {
    return Environment.getExternalStoragePublicDirectory(path)
}