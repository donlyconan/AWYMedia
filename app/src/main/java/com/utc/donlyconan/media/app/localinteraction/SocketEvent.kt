package com.utc.donlyconan.media.app.localinteraction


interface SocketEvent {

    fun onReceive(command: Command)

}