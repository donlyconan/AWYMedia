package com.utc.donlyconan.media.app.localinteraction

import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

fun SelectionKey.use(block: (socket: SocketChannel, client: Client?) -> Unit): SelectionKey {
    val socket = channel() as SocketChannel
    val client = attachment() as? Client
    block(socket, client)
    return this
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

fun List<SocketEvent>.send(command: Command) {
    forEach { e -> e.onReceive(command) }
}