package com.utc.donlyconan.media.app.localinteraction

import com.utc.donlyconan.media.app.utils.Logs
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.Socket
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

    var isAlive:Boolean = false
        protected set
    protected val clients: HashMap<Long, Client> by lazy { hashMapOf() }
    protected val supervisorJob = SupervisorJob()
    protected val coroutineScope = CoroutineScope(supervisorJob + Dispatchers.IO + CoroutineExceptionHandler {_,e -> e.printStackTrace() })
    val events: MutableList<Client.ClientServiceListener> by lazy { mutableListOf() }
    val listClients: List<Client> get() = clients.values.toList()


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
        sClientId++
        val client = Client(sClientId, socket)
        clients[client.clientId] = client
        client.clientServiceListener = clientServiceListener
        coroutineScope.launch {
            client.start()
        }
    }

    abstract fun isGroupOwner(): Boolean

    open suspend fun send(bytes: ByteArray) {
        clients.forEach { id, client ->
            client.send(bytes)
        }
    }

    open suspend fun send(packet: Packet) {
        val bytes = packet.bytes()
        send(bytes)
    }

    open suspend fun send(code: Byte, obj: Any) {
        obj.serialize()?.let {
            val packet = Packet.from(code, it)
            send(packet)
        }
    }

    /**
     * Disconnect a specific client from system
     */
    fun disconnect(clientId: Long){
        clients[clientId]?.close()
    }

    /**
     * Send a file and it's info
     */
    open suspend fun sendFile(purpose: Packet, inputStream: InputStream) {
        send(purpose)
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
            events.forEach { e -> e.onClose(clientId, socket) }
            clients.remove(clientId)
        }

        override fun onReceive(clientId: Long, bytes: ByteArray) {
            events.forEach { e -> e.onReceive(clientId, bytes) }
        }

        override fun onReceive(clientId: Long, packet: Packet) {
            events.forEach { e -> e.onReceive(clientId, packet) }
        }
    }

    fun registerClientServiceListener(event: Client.ClientServiceListener) {
        events.add(event)
    }

    fun unregisterClientServiceListener(event: Client.ClientServiceListener) {
        events.remove(event)
    }
}