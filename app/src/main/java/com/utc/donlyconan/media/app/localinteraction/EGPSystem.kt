package com.utc.donlyconan.media.app.localinteraction

import com.utc.donlyconan.media.app.utils.consumeAll
import kotlinx.coroutines.delay
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.reflect.KClass

/**
 * That class represent for clients or server. It can be used to communicate among anther devices.
 * EGM System need to implement into two parts that is EGM Server and EGM Client
 */
abstract class EGPSystem {
    companion object {
        const val IP_PORT = 8888
        const val HOSTNAME = "localhost"

        const val CAPACITY_2M = 2046
        const val CAPACITY_4M = 4096
        const val CAPACITY_8M = 8192

        @JvmStatic
        fun <T: EGPSystem> create(kClass: KClass<T>, ipAddress: String): EGPSystem {
            return when(kClass) {
                EGPMediaServer::class -> EGPMediaServer()
                EGPMediaClient::class -> EGPMediaClient()
                else -> {
                    throw ClassNotFoundException("${kClass.simpleName} is not found")
                }
            }?.apply {
                setup(ipAddress)
            }
        }
    }

    protected var isAlive:Boolean = false
        private set
    protected var _selector: Selector? = null
    protected val selector: Selector get() = _selector!!
    protected val byteBuffer: ByteBuffer by lazy { ByteBuffer.allocate(CAPACITY_4M) }
    protected val clients: HashMap<SelectionKey, Client> by lazy { hashMapOf() }
    protected val events by lazy { mutableListOf<SocketEvent>() }

    /**
     * Set up all attributes for system before running
     */
    abstract fun setup(ipAddress: String)

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

        // shutdown when server is not necessary
        shutdown()
    }

    open fun doAccept(socket: SocketChannel) {
        socket.configureBlocking(false)
        val key = socket.register(selector, SelectionKey.OP_READ)
        val client = Client(key, selector, socket)
        key.attach(client)
        Log("Server# Accepted a client: IP: ${socket.remoteAddress}")
        clients[key] = client
    }

    open fun doWrite(key: SelectionKey) = key.use { socket, client ->
        byteBuffer.clear()
        client?.packages?.iterator()?.consumeAll { data ->
            byteBuffer.put(data).flip()
            socket.write(byteBuffer)
        }
    }

    open fun doRead(key: SelectionKey) = key.use { socket, client ->
        byteBuffer.clear()
        var quantity = socket.read(byteBuffer)
        if(quantity == -1) {
            disconnect(key)
        } else {
            byteBuffer.flip()
            val command = Command.from(byteBuffer)
            client?.receive(command)
            events.sendAll(command)
            byteBuffer.compact()
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
        _selector = null
    }

    abstract fun isGroupOwner(): Boolean

    fun sendWith(command: Command) {
        Log("sendWith: client.size=${clients.size}, command=$command")
        clients.values.forEach { client ->
            client.send(command, false)
        }
        selector.wakeup()
    }

    /**
     * Send to all clients with data that is attached
     * It can be used outside the current thread
     */
    fun send(code: Byte, bytes: ByteArray) {
        val buffer = ByteBuffer.allocate(CAPACITY_4M)
        buffer.put(code).put(bytes).flip()
        clients.forEach { _, client ->
            client.socket.write(buffer)
            buffer.rewind()
        }
    }

    /**
     * Send a file and it's info
     */
    suspend fun sendFile(destination: Command, inputStream: InputStream) {
        Log("sendAll: inputStream")
        send(destination.code, destination.bytes)
        delay(50 /*delay time for preparing form clients*/ )
        // spend a slot for hash code
        var bytes = ByteArray(CAPACITY_4M - 1)
        inputStream.use { inp ->
            while (inp.available() > 0) {
                if(inp.available() < CAPACITY_4M - 1) {
                    bytes = ByteArray(inp.available())
                }
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