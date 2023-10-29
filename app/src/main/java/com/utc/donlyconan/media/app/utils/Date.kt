package com.utc.donlyconan.media.app.utils

import java.text.SimpleDateFormat


const val ONE_HOUR = 3_600
const val TIME_ONE_DAY = 86_400
const val TIME_7_HOURS = 7 * ONE_HOUR
val shortestOfTimeFormat = SimpleDateFormat("E, dd MMM yyyy")
val formatDate = SimpleDateFormat("MMM dd, yyyy")


fun Long.atStartOfDay(): Long {
    val offset = this % TIME_ONE_DAY
    return this - offset - TIME_7_HOURS
}

fun Long.atEndOfDay(): Long {
    val offset = this % TIME_ONE_DAY
    return this + TIME_ONE_DAY - offset - TIME_7_HOURS - 1
}

fun Long.formatShortTime(): CharSequence {
    return shortestOfTimeFormat.format(this)
}

fun Long.formatToTime(): String {
    return formatDate.format(this)
}