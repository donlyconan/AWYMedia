package com.utc.donlyconan.media.app.localinteraction

import androidx.annotation.WorkerThread
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


@WorkerThread
class EGPMediaServer: EGPSystem() {
    private var serverSocket: ServerSocketChannel? = null

    override fun setup(ipAddress: String) {
        Log("Server is setting up...")
        // Connect and setup the server
        serverSocket = ServerSocketChannel.open().apply {
            socket().bind(InetSocketAddress(ipAddress, IP_PORT))
            configureBlocking(false)
            // Open selector and register
            _selector = Selector.open()
            register(selector, SelectionKey.OP_ACCEPT)
        }
    }


    override fun shutdown() {
        super.shutdown()
        Log("Server is terminated.")
        serverSocket?.close()
        serverSocket = null
    }

    override fun isGroupOwner(): Boolean {
        return true
    }

}

fun main() {
    runBlocking {
        EGPSystem.create(EGPMediaServer::class, EGPSystem.HOSTNAME)?.apply {
            registerSocketEvent(object : SocketEvent {
                override fun onReceive(command: Command) {
                    super.onReceive(command)
                    Log("Message received: ${command.get(String::class)}")
                }
            })
            run()
        }

    }
}