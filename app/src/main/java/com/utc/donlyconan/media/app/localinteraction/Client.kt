package com.utc.donlyconan.media.app.localinteraction
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.Stack

class Client(val key: SelectionKey, val selector: Selector, val socket: SocketChannel) {
    companion object {
        @get:Synchronized
        private var sClientId = 0L
    }
    var clientId: Long = sClientId++
        private set
    val packages: Stack<ByteArray> by lazy { Stack<ByteArray>() }
    var socketEvent: SocketEvent? = null

    fun send(bytes: ByteArray, sendImmediately: Boolean = true) {
        packages.push(bytes)
        key.interestOps(SelectionKey.OP_WRITE)
        if(sendImmediately) {
            selector.wakeup()
        }
    }

    fun send(command: Command, sendImmediately: Boolean = true) {
        val bytes = command.toByteArray()
        send(bytes, sendImmediately)
    }

    fun receive(command: Command) {
        socketEvent?.onReceive(command)
    }

    override fun toString(): String {
        return "clientId = ${clientId}, packages = ${packages.size}"
    }

}