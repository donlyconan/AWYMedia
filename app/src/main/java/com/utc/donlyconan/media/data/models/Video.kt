package com.utc.donlyconan.media.data.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Video(
    var id: Long,
    var data: Uri,
    var title: String,
    var duration: Int,
    var size: Long,
    var date: Long): Parcelable {
}