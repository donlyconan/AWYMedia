package com.utc.donlyconan.media.app.localinteraction

import android.os.Parcel
import android.os.Parcelable
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel


fun SelectionKey.use(block: (socket: SocketChannel, client: Client?) -> Unit) {
    val socket = channel() as SocketChannel
    val client = attachment() as? Client
    block(socket, client)
}

fun SelectionKey.close() {
    try {
        channel().close()
        this.cancel()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Log(msg: String) {
    println(msg)
}

var SelectionKey.client: Client? get() = attachment() as? Client
    set(value) = attach(value) as Unit

inline fun List<SocketEvent>.send(block: SocketEvent.() -> Unit) {
    forEach { e -> block.invoke(e)}
}

fun Parcelable.marshall(): ByteArray {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

fun unmarshall(bytes: ByteArray): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    return parcel
}

fun <T> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
    val parcel = unmarshall(bytes)
    val result = creator.createFromParcel(parcel)
    parcel.recycle()
    return result
}

fun ByteArray.fill(bytes: ByteArray, offset: Int = 0) {
    var index = offset
    var length = Math.min(bytes.size + offset, size)
    while (index < length) {
        this[index] = bytes[index - offset]
        index++
    }
}