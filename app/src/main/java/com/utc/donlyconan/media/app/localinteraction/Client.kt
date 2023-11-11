package com.utc.donlyconan.media.app.localinteraction
import java.net.ConnectException
import java.net.Socket

open class Client(val clientId: Long, val socket: Socket) {
    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream()
    @Volatile
    var clientServiceListener: ClientServiceListener? = null
    var name: String? = null
    var isAlive: Boolean = false
        private set

    /**
     * Start a service
     * Thread will be block to run the service.
     */
    suspend fun start() {
        println("Client#$clientId is started. ThreadInfo={${Thread.currentThread().name}}")
        val bytes = ByteArray(EGPSystem.DEFAULT_BUFFER_SIZE)
        clientServiceListener?.onStart(clientId, socket)
        isAlive = true
        while (isAlive) {
            var readIndex = inputStream.read(bytes)
            if (readIndex == -1) {
                isAlive = false
            } else {
                val packet = Packet.from(bytes, readIndex)
                if (packet.code() == Packet.CODE_DEVICE_NAME) {
                    name = String(packet.data())
                }
                clientServiceListener?.onReceive(clientId, packet)
            }
        }
        close()
        throw ConnectException("Client#$clientId is disconnected.")
    }

    fun send(bytes: ByteArray) {
        outputStream.write(bytes)
    }

    fun send(packet: Packet) {
        outputStream.write(packet.bytes())
    }

    fun close() {
        try {
            isAlive = false
            inputStream.close()
            outputStream.close()
            socket.close()
        } finally {
            println("client#$clientId is closed.")
            clientServiceListener?.onClose(clientId, socket)
        }
    }

    interface ClientServiceListener {
        fun onStart(clientId: Long, socket: Socket) {}
        fun onClose(clientId: Long, socket: Socket) {}
        fun onReceive(clientId: Long, bytes: ByteArray) {}
        fun onReceive(clientId: Long, packet: Packet) {}
    }
}