package com.utc.donlyconan.media.app.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

const val DATE_FORMAT = "MMM dd, yyyy"
const val DETAIL_FORMAT_DATE = "E, dd MMM yyyy"

private var _detailOfTimeFormat = SimpleDateFormat(DETAIL_FORMAT_DATE)
private var _formatDate = SimpleDateFormat(DATE_FORMAT)
val formatDate get() = _formatDate
val shortestOfTimeFormat get() = _detailOfTimeFormat

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

fun setFormatDate(locale: Locale) {
    _formatDate = SimpleDateFormat(DATE_FORMAT, locale)
}

fun setDetailedFormatDate(locale: Locale) {
    _detailOfTimeFormat = SimpleDateFormat(DETAIL_FORMAT_DATE, locale)
}