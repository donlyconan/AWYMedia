package com.utc.donlyconan.media.app.utils

import java.text.DecimalFormat

fun Long.convertToStorageData(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var temp = this.toFloat()
    var level = 0
    while (temp / 1024 > 1) {
        temp /= 1024
        level++
    }
    val formatter = DecimalFormat("#,###,###.#")
    return "${formatter.format(temp)} ${units[level]}"
}

fun Int.toShortTime(): String {
    val hour = this / 3600
    val min = (this - 3600 * hour) / 60
    val sec = this - hour * 3600 - min * 60
    return StringBuilder().apply {
        if(hour > 0) {
            append(if(hour >= 10) hour else "0$hour").append(":")
        }
        append(if(min >= 10) min else "0$min").append(":")
        append(if(sec >= 10) sec else "0$sec")
    }.toString()
}
//
//fun main() {
////    println("Unit=" + 300L.convertToStorageData() )
////    println("Unit=" + 3000L.convertToStorageData() )
////    println("Unit=" + 30000L.convertToStorageData() )
////    println("Unit=" + 300000L.convertToStorageData() )
////    println("Unit=" + 3000000L.convertToStorageData() )
////    println("Unit=" + 30000000L.convertToStorageData() )
////    println("Unit=" + 300000000L.convertToStorageData() )
////    println("Unit=" + 3000000000.convertToStorageData() )
////    println("Unit=" + 300000000000.convertToStorageData() )
//
////    println("Time=" + 10000L.toShortTime())
////    println("Time=" + 10L.toShortTime())
////    println("Time=" + 1000L.toShortTime())
////    println("Time=" + 100L.toShortTime())
////    println("Time=" + 1000900L.toShortTime())
//
////    Searches.evaluate("doreamon", "sraamkon")
//}