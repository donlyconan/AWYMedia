package com.utc.donlyconan.media.app.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId


val shortestOfTimeFormat = SimpleDateFormat("E, dd MMM yyyy")
val formatDate = SimpleDateFormat("MMM dd, yyyy")

fun Long.atStartOfDay(): Long {
    val localDate = LocalDate.ofEpochDay(this)
    val startOfDay = localDate.atStartOfDay()
    return startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond
}

fun Long.formatShortTime(): CharSequence {
    return shortestOfTimeFormat.format(this)
}

fun Long.formatToTime(): String {
    return formatDate.format(this)
}