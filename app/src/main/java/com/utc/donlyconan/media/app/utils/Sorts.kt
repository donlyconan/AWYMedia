package com.utc.donlyconan.media.app.utils

import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.data.models.Video


fun List<Trash>.sortedByDeletedDate(descended: Boolean): List<Any> {
    val maps = if(descended) {
        sortedByDescending { it.deletedAt }
            .groupBy { it.deletedAt.formatShortTime() }
    } else {
        sortedBy { it.deletedAt }
            .groupBy { it.deletedAt.formatShortTime() }
    }
    val result = ArrayList<Any>(maps.size * 2)
    for (item in maps) {
        result.add(item.key)
        result.addAll(item.value)
    }
    return result
}


fun List<Video>.sortedByCreatedDate(descended: Boolean): List<Any> {
    val maps = if(descended) {
        sortedByDescending { it.createdAt }
            .groupBy { it.createdAt.formatShortTime() }
    } else {
        sortedBy { it.createdAt }
            .groupBy { it.createdAt.formatShortTime() }
    }
    val result = ArrayList<Any>(maps.size * 2)
    for (item in maps) {
        result.add(item.key)
        result.addAll(item.value)
    }
    return result
}

fun List<Video>.sortedByUpdatedDate(descended: Boolean): List<Any> {
    val maps = if(descended) {
        sortedByDescending { it.updatedAt }
            .groupBy { it.updatedAt.formatShortTime() }
    } else {
        sortedBy { it.updatedAt }
            .groupBy { it.updatedAt.formatShortTime() }
    }
    val result = ArrayList<Any>(maps.size * 2)
    for (item in maps) {
        result.add(item.key)
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