package com.utc.donlyconan.media.app.localinteraction

import com.utc.donlyconan.media.app.utils.consumeAll
import kotlinx.coroutines.delay
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.math.min

abstract class EgmSystem {
    companion object {
        const val IP_PORT = 8888
        const val HOSTNAME = "localhost"

        const val CAPACITY_2M = 2046
        const val CAPACITY_4M = 4096
        const val CAPACITY_8M = 8192
    }

    protected var isAlive:Boolean = false
    protected lateinit var selector: Selector
    val mediumBuffer = ByteBuffer.allocate(CAPACITY_4M)
    val clients: HashMap<SelectionKey, Client> by lazy { hashMapOf() }
    val events by lazy { mutableListOf<SocketEvent>() }

    /**
     * Set up all attributes for system before running
     */
    abstract fun setup()

    fun run() {
        Log("Setup is completed and run on port: $IP_PORT")
        // Mark the server is working
        isAlive = true

        while (isAlive) {
            selector.select()
            selector.selectedKeys().iterator().consumeAll { key ->
                try {
                    if(!key.isValid) {
                        return@consumeAll
                    }
                    if (key.isAcceptable) {
                        val server = key.channel() as ServerSocketChannel
                        val socket = server.accept()
                        doAccept(socket)
                    }
                    if (key.isConnectable) {
                        doConnect(key)
                    }
                    if (key.isReadable) {
                        doRead(key)
                    }
                    if (key.isWritable) {
                        doWrite(key)
                        key.interestOps(SelectionKey.OP_READ)
                    }
                } catch (e: Exception) {
                    disconnect(key)
                    e.printStackTrace()
                }
            }

        }
        shutdown()
    }

    open fun doAccept(socket: SocketChannel) {
        socket.configureBlocking(false)
        val key = socket.register(selector, SelectionKey.OP_READ).apply {
            interestOps(SelectionKey.OP_READ)
        }
        val client = Client(key, selector, socket)
        key.attach(client)
        Log("Server# Accepted a client: IP: ${socket.remoteAddress}")
        clients[key] = client
    }

    open fun doWrite(key: SelectionKey) = key.use { socket, client ->
        mediumBuffer.clear()
        client?.packages?.iterator()?.consumeAll { data ->
            mediumBuffer.put(data).flip()
            socket.write(mediumBuffer)
        }
    }

    open fun doRead(key: SelectionKey) = key.use { socket, client ->
        mediumBuffer.clear()
        var quantity = socket.read(mediumBuffer)
        if(quantity <= -1) {
            disconnect(key)
        } else {
            mediumBuffer.flip()
            val command = Command.from(mediumBuffer)
            client?.receive(command)
            events.send(command)
            mediumBuffer.compact()
        }
    }

    open fun doConnect(key: SelectionKey) {
        Log("doConnect() called with: key = $key")
    }

    open fun disconnect(key: SelectionKey) {
        Log("disconnect() called with: address = ${key.client?.socket?.remoteAddress}")
        try {
            key.close()
            key.channel().close()
        } finally {
            clients.remove(key)
        }
    }

    open fun shutdown() {
        Log("shutdown() called")
        isAlive = false
        clients.clear()
        selector.close()
    }

    abstract fun isServer(): Boolean

    fun sendWith(command: Command) {
        Log("sendAll: command=$command")
        clients.values.forEach { client ->
            client.send(command, false)
        }
        selector.wakeup()
    }

    fun send(code: Byte, bytes: ByteArray) {
        val buffer = ByteBuffer.allocate(CAPACITY_4M)
        buffer.put(code).put(bytes).flip()
        clients.forEach { _, client ->
            client.socket.write(buffer)
            buffer.rewind()
        }
    }

    suspend fun sendFile(destination: Command, inputStream: InputStream) {
        Log("sendAll: inputStream")
        send(destination.code, destination.bytes)
        delay(50)
        inputStream.use { inp ->
            while (inp.available() > 0) {
                var bytes = ByteArray(min(inp.available(), CAPACITY_4M - 1))
                val code = if (inp.available() > CAPACITY_4M - 1) Command.CODE_FILE_SENDING else Command.CODE_FILE_END
                inp.read(bytes)
                send(code, bytes)
            }
        }
    }

    fun registerSocketEvent(event: SocketEvent) {
        events.add(event)
    }

    fun unregisterSocketEvent(event: SocketEvent) {
        events.remove(event)
    }
}