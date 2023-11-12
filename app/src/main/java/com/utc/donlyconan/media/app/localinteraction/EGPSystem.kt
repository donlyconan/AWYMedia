package com.utc.donlyconan.media.app.localinteraction

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
        const val DEFAULT_BUFFER_SIZE = 2048

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
        protected var sClientId: Long = 0L
    }

    var isAlive:Boolean = false
        protected set
    protected val clients: HashMap<Long, Client> by lazy { hashMapOf() }
    protected val supervisorJob = SupervisorJob()
    protected val coroutineScope = CoroutineScope(supervisorJob + Dispatchers.IO + CoroutineExceptionHandler {_,e -> e.printStackTrace() })
    val events: MutableList<Client.ClientServiceListener> by lazy { mutableListOf() }
    val listClients: List<Client> get() = clients.values.toList()
    private var systemName: String? = null


        /**
     * Set up all attributes for system before running
     */
    abstract fun setup(inetAddress: InetAddress?)

    /**
     * Perform to start a server or client
     * Require: call accept(...)
     */
    abstract suspend fun start()

    open suspend fun accept(socket: Socket) {
        println( "accept() called with: socket = $socket")
        sClientId++
        val client = Client(sClientId, socket)
        clients[client.clientId] = client
        client.clientServiceListener = clientServiceListener
        systemName?.let { name -> send(Packet.from(Packet.CODE_DEVICE_NAME, name.toByteArray())) }
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

    open suspend fun send(clientId: Long, packet: Packet) {
        val bytes = packet.bytes()
        clients[clientId]?.send(packet)
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
    open suspend fun sendFile(packet: Packet, inp: InputStream) {
        println("sendFile() called with: purpose = $packet, inputStream=$inp")
        send(packet)
        // spend a slot for code in the head
        var quantity = 0
        while (quantity != -1) {
            quantity = inp.read(packet.bytes(), Packet.INDEX_DATA, packet.capacity() - Packet.INDEX_DATA)
            if(quantity != -1) {
                if (quantity >= DEFAULT_BUFFER_SIZE - Packet.INDEX_DATA) {
                    packet.code(Packet.CODE_FILE_SENDING)
                } else {
                    packet.code(Packet.CODE_FILE_END)
                    println("Last package: " + quantity)
                }
                packet.length(quantity)
                send(packet)
            }
        }
    }

    fun threadInfo() = "threadId: ${Thread.currentThread().id}; threadName: ${Thread.currentThread().name}"

    open fun shutdown() {
        isAlive = false
        clients.values.forEach { client -> client.close() }
    }

    fun setName(name: String) {
        systemName = name
    }

    protected val clientServiceListener = object : Client.ClientServiceListener {
        override fun onStart(clientId: Long, socket: Socket) {
            events.forEach { e -> e.onStart(clientId, socket) }
        }

        override fun onClose(clientId: Long, socket: Socket) {
            clients.remove(clientId)
            events.forEach { e -> e.onClose(clientId, socket) }
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