package com.utc.donlyconan.media.app.localinteraction

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.Socket
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

fun ByteArray.fill(bytes: ByteArray, offset: Int = 0) {
    var index = offset
    var length = Math.min(bytes.size + offset, size)
    while (index < length) {
        this[index] = bytes[index - offset]
        index++
    }
}

fun Socket.send(packet: Packet) {
    getOutputStream().write(packet.bytes())
}

fun Map<InetAddress, Client>.all(block: Client.() -> Unit): Int {
    var count = 0
    forEach { _, client ->
        try {
            block.invoke(client)
            count++
        } finally { }
    }
    return count
}

fun Any.serialize(): ByteArray? {
    val out = ByteArrayOutputStream()
    val os = ObjectOutputStream(out)
    os.writeObject(this)
    val data = out.toByteArray()
    out.close()
    os.close()
    return data
}

fun ByteArray.deserialize(): Any {
    val byteInput = ByteArrayInputStream(this)
    val objectInput = ObjectInputStream(byteInput)
    val data = objectInput.readObject()
    byteInput.close()
    objectInput.close()
    return data
}

fun Int.toBytes() = ByteBuffer.allocate(4).array()