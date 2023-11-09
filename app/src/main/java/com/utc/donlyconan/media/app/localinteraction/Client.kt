package com.utc.donlyconan.media.app.localinteraction
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import kotlin.math.log

open class Client(val clientId: Long, val socket: Socket) {
    protected val inputStream = DataInputStream(socket.getInputStream())
    protected val outputStream = DataOutputStream(socket.getOutputStream())
    @Volatile
    var clientServiceListener: ClientServiceListener? = null

    /**
     * Start a service
     * Thread will be block to run the service.
     */
    suspend fun start() {
        println("Client#$clientId is started. ThreadInfo={${Thread.currentThread().name}}")
        var count = 0
        val bytes = ByteArray(EGPSystem.DEFAULT_BUFFER_SIZE)
        clientServiceListener?.onStart(clientId, socket)
        while (count != -1){
            count = inputStream.read(bytes)
            if(count < bytes.size) {
                clientServiceListener?.onReceive(clientId, bytes.copyOfRange(0, count))
            } else {
                clientServiceListener?.onReceive(clientId, bytes)
            }
        }
        close()
        throw ConnectException("Client#$clientId is disconnected")
    }

    fun send(bytes: ByteArray) {
        outputStream.write(bytes)
    }

    fun send(buffer: ByteBuffer) {
        socket.channel.write(buffer)
    }

    fun send(packet: Packet) {
        outputStream.write(packet.toByteArray())
    }

    fun close() {
        try {
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
    }
}