package com.utc.donlyconan.media.app.localinteraction

import androidx.annotation.WorkerThread
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
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

    override fun doRead(key: SelectionKey): SelectionKey {
        val key = super.doRead(key)
        key.client?.send(Command.from("Time: ${System.currentTimeMillis()}"))
        return key
    }

    override fun shutdown() {
        super.shutdown()
        Log("Server# Server is terminated.")
        serverSocket.close()
    }

    fun sendAll(command: Command) {
        Log("sendAll: command=$command")
        clients.values.forEach { client ->
            client.send(command, false)
        }
        selector.wakeup()
    }

    fun sendAll(bytes: ByteArray) {
        Log("sendAll: array byte.size=${bytes.size}")
        clients.values.forEach { client ->
            client.send(bytes, false)
        }
        selector.wakeup()
    }

    fun sendFile(fileName: String, inputStream: InputStream) {
        Log("sendAll: inputStream")
        sendAll(Command.from(fileName))
        inputStream.use { inp ->
            while (inp.available() > 0) {
                val bytes = ByteArray(min(inp.available(), CAPACITY_4M) + 1)
                bytes[0] = if(inp.available() < CAPACITY_4M) Command.CODE_FILE_END else Command.CODE_FILE_SENDING
                inp.read(bytes, 1, bytes.size)
                sendAll(bytes)
            }
        }
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