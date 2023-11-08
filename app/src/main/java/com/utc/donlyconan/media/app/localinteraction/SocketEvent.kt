package com.utc.donlyconan.media.app.localinteraction


interface SocketEvent {
    fun onReceive(command: Command) {
        onReceive(command.code, command.bytes)
    }

    fun onReceive(code: Byte, bytes: ByteArray) { }

    fun onError(e: Throwable) {}
}