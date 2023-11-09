package com.utc.donlyconan.media.app.localinteraction


interface SocketEvent {
    fun onReceive(packet: Packet) {
        onReceive(packet.code, packet.bytes)
    }

    fun onReceive(code: Byte, bytes: ByteArray) { }

    fun onError(e: Throwable) {}
}