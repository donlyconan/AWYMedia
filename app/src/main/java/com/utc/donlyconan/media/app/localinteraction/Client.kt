package com.utc.donlyconan.media.app.localinteraction
import androidx.recyclerview.widget.DiffUtil
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import java.net.ConnectException
import java.net.Socket

open class Client(val clientId: Long, val socket: Socket) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                if(oldItem is Client && newItem is Client) {
                    return oldItem.clientId == newItem.clientId
                }
                return false
            }
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }

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
        var curIndex = 0
        val packet = Packet.from("")

        while (isAlive) {
            var readIndex = inputStream.read(bytes, curIndex, bytes.size - curIndex)
            curIndex += readIndex
            if (readIndex == -1) {
                println("Can not read anything.")
                isAlive = false
            } else if(curIndex >= EGPSystem.DEFAULT_BUFFER_SIZE) {
                packet.replace(bytes)
                if (packet.code() == Packet.CODE_DEVICE_NAME) {
                    name = String(packet.data())
                }
                clientServiceListener?.onReceive(clientId, packet)
                curIndex = 0
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
        fun onReceive(bytes: ByteArray, part: Int) {}
    }
}