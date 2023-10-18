package com.utc.donlyconan.media.app.utils

import java.text.SimpleDateFormat


const val ONE_HOUR = 3_600
const val TIME_ONE_DAY = 86_400
const val TIME_7_HOURS = 7 * ONE_HOUR
val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
val shortTimeFormat = SimpleDateFormat("MM yyyy")
val shortestOfTimeFormat = SimpleDateFormat("E, dd/MM/yyyy")


fun Long.atStartOfDay(): Long {
    val offset = this % TIME_ONE_DAY
    return this - offset - TIME_7_HOURS
}

fun Long.atEndOfDay(): Long {
    val offset = this % TIME_ONE_DAY
    return this + TIME_ONE_DAY - offset - TIME_7_HOURS - 1
}

fun Long.toShortTime(): CharSequence {
    return shortestOfTimeFormat.format(this * 1000)
}