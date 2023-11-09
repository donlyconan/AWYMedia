package com.utc.donlyconan.media.app.localinteraction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.reflect.KClass

/**
 * That class represent for clients or server. It can be used to communicate among anther devices.
 * EGM System need to implement into two parts that is EGM Server and EGM Client
 */
abstract class EGPSystem {
    companion object {
        const val IP_PORT = 8888
        const val HOSTNAME = "localhost"

        const val DEFAULT_BUFFER_SIZE = 4097
        const val DEFAULT_DATA_SIZE = DEFAULT_BUFFER_SIZE - 1
        const val CAPACITY_8M = 8192

        @JvmStatic
        fun <T: EGPSystem> create(kClass: KClass<T>, inetAddress: InetAddress?): EGPSystem {
            return when(kClass) {
                EGPMediaServer::class -> EGPMediaServer()
                EGPMediaClient::class -> EGPMediaClient()
                else -> {
                    throw IllegalArgumentException("${kClass.simpleName} is not accepted.")
                }
            }.apply {
                setup(inetAddress)
            }
        }

        @get:Synchronized
        private var sClientId: Long = 0L
    }

    protected var isAlive:Boolean = false
    protected val clients: HashMap<InetAddress, Client> by lazy { hashMapOf() }
    protected val supervisorJob = SupervisorJob()
    protected val coroutineScope = CoroutineScope(supervisorJob + Dispatchers.IO)
    val events: MutableList<Client.ClientServiceListener> by lazy { mutableListOf() }


    /**
     * Set up all attributes for system before running
     */
    abstract fun setup(inetAddress: InetAddress?)

    /**
     * Perform to start a server or client
     * Require: call accept(...)
     */
    abstract suspend fun start()

    open fun accept(socket: Socket) {
        println("${socket.inetAddress.hostAddress} is accepted. ${threadInfo()}")
        sClientId++
        val client = Client(sClientId, socket)
        clients[socket.inetAddress] = client
        client.clientServiceListener = clientServiceListener
        coroutineScope.launch(newSingleThreadContext("Client#${client.clientId}")) {
            client.start()
        }
        send(Packet.from("client#$sClientId have took part in the waiting room."))
    }

    abstract fun isGroupOwner(): Boolean

    open fun send(bytes: ByteArray) {
        clients.forEach { id, client ->
            client.send(bytes)
        }
    }

    open fun send(packet: Packet) {
        val bytes = packet.toByteArray()
        send(bytes)
    }

    open fun sendWith(buffer: ByteBuffer) {
        clients.forEach { inet, client ->
            client.send(buffer)
        }
    }

    /**
     * Send a file and it's info
     */
    fun sendFile(packet: Packet, inputStream: InputStream) {
        send(packet)
        // spend a slot for code in the head
        var bytes = ByteArray(DEFAULT_BUFFER_SIZE)
        inputStream.use { inp ->
            while (inp.available() > 0) {
                if (inp.available() > DEFAULT_BUFFER_SIZE - 1) {
                    bytes[0] = Packet.CODE_FILE_SENDING
                } else {
                    bytes = ByteArray(inp.available() + 1)
                    bytes[0] = Packet.CODE_FILE_END
                    println("Last package: " + inp.available())
                }
                inp.read(bytes, 1, bytes.size - 1)
                send(bytes)
            }
        }
    }

    fun threadInfo() = "threadId: ${Thread.currentThread().id}; threadName: ${Thread.currentThread().name}"

    open fun shutdown() {
        isAlive = false
        clients.values.forEach { client -> client.close() }
    }

    private val clientServiceListener = object : Client.ClientServiceListener {
        override fun onStart(clientId: Long, socket: Socket) {
            events.forEach { e -> e.onStart(clientId, socket) }
        }

        override fun onClose(clientId: Long, socket: Socket) {
            events.forEach { e -> e.onStart(clientId, socket) }
        }

        override fun onReceive(clientId: Long, bytes: ByteArray) {
            events.forEach { e -> e.onReceive(clientId, bytes) }
        }
    }

    fun registerClientServiceListener(event: Client.ClientServiceListener) {
        events.add(event)
    }

    fun unregisterClientServiceListener(event: Client.ClientServiceListener) {
        events.remove(event)
    }
}