package com.utc.donlyconan.media.app.utils

import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.models.Video


fun List<Trash>.sortedByDeletedDate(descended: Boolean): List<Any> {
    val maps = if(descended) {
        sortedByDescending { it.deletedAt }
            .groupBy { it.deletedAt.atStartOfDay() }
    } else {
        sortedBy { it.deletedAt }
            .groupBy { it.deletedAt.atStartOfDay() }
    }
    val result = ArrayList<Any>(maps.size * 2)
    for (item in maps) {
        result.add(item.key.toShortTime())
        result.addAll(item.value)
    }
    return result
}


fun List<Video>.sortedByCreatedDate(descended: Boolean): List<Any> {
    val maps = if(descended) {
        sortedByDescending { it.createdAt }
            .groupBy { it.createdAt.atStartOfDay() }
    } else {
        sortedBy { it.createdAt }
            .groupBy { it.createdAt.atStartOfDay() }
    }
    val result = ArrayList<Any>(maps.size * 2)
    for (item in maps) {
        result.add(item.key.toShortTime())
        result.addAll(item.value)
    }
    return result
}

//fun main() {
//    val matrix = Searches.evaluate("doreamon ad".toCharArray(), "syoreakm".toCharArray());
//
//    for (i in 0 until matrix.size) {
//        matrix[i].forEach { print("$it ") }
//        println()
//    }
//}