package com.utc.donlyconan.media.app.localinteraction

import androidx.annotation.WorkerThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.math.min


@WorkerThread
class EgpMediaServer : EgmSystem() {
    private lateinit var serverSocket: ServerSocketChannel

    override fun setup() {
        Log("Server# setupAndRun: Server is setting up...")
        // Connect and setup the server
        serverSocket = ServerSocketChannel.open()
        serverSocket.socket().bind(InetSocketAddress(HOSTNAME, IP_PORT))
        serverSocket.configureBlocking(false)
        // Open selector and register
        selector = Selector.open()
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)
    }


    override fun shutdown() {
        super.shutdown()
        Log("Server# Server is terminated.")
        serverSocket.close()
    }

    override fun isServer(): Boolean {
        return true
    }

}

fun main() {
    runBlocking {
        EgpMediaServer().apply {
            setup()
            run()
        }
    }
}