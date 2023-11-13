package com.utc.donlyconan.media.app.localinteraction

import android.util.Log
import com.utc.donlyconan.media.app.utils.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.Scanner

class EGPMediaClient: EGPSystem() {
    companion object {
        const val CLIENT_ID = 0L
    }

    private var _socket: Socket? = null
    val socket: Socket get() = _socket!!
    var client: Client? = null
        private set

    override fun setup(inetAddress: InetAddress?) {
        println("setup() called with: inetAddress = $inetAddress")
        _socket = Socket(inetAddress, IP_PORT)
        coroutineScope.launch {
            accept(socket)
        }
    }

    override suspend fun accept(socket: Socket) {
        println( "accept() called with: socket = $socket, systemName=$systemName")
        val client = Client(CLIENT_ID, socket)
        clients[client.clientId] = client
        client.clientServiceListener = clientServiceListener
        systemName?.let { name -> send(Packet.from(Packet.CODE_DEVICE_NAME, name.toByteArray())) }
    }

    override suspend fun start() {
        println("Client is started.")
        isAlive = true
        clients.values.firstOrNull()?.start()
        println("Client is shutdown.")
        shutdown()
    }

    override fun shutdown() {
        super.shutdown()
        _socket?.close()
        _socket = null
    }

    override fun isGroupOwner() = false
}
//
//fun main() {
//    runBlocking {
//        val service = (EGPSystem.create(EGPMediaClient::class, InetAddress.getLocalHost()) as EGPMediaClient)
//
//        service.registerClientServiceListener(object : Client.ClientServiceListener {
//
//            var file: File? = null
//            var outputStream: OutputStream? = null
//
//            override fun onReceive(clientId: Long, bytes: ByteArray) {
//                val code = bytes[0]
//                when(code) {
//                    Packet.CODE_FILE_START -> {
//                        val packet = Packet.from(bytes)
//                        file = File("A:\\resources", packet.get(String::class))
//                        outputStream = FileOutputStream(file)
//                        println("Filename=${file?.name}")
//                    }
//                    Packet.CODE_FILE_SENDING,  Packet.CODE_FILE_END -> {
//                        outputStream?.write(bytes, 1, bytes.size - 1)
//                        if (code == Packet.CODE_FILE_END) {
//                            outputStream?.flush()
//                            outputStream?.close()
//                            outputStream = null
//                            println("Size=${file?.length()}")
//                        }
//                    }
//                    Packet.CODE_MESSAGE_SEND -> {
//                        println("Message: ${Packet.from(bytes).get(String::class)}")
//                    }
//                    else -> {
//                        Log("Code = ${code} is not found")
//                    }
//                }
//            }
//
//        })
//
//        launch(Dispatchers.IO) {
//            val scanner = Scanner(System.`in`)
//            while (true) {
//                println("Input something ${service.threadInfo()}: ")
//                val text = scanner.nextLine()
//                service.send(Packet.from(text))
//            }
//        }
//
//
//        GlobalScope.launch {
//            delay(2000)
//            val file = File("B:\\Downloads\\English Conversation Practice - Improve Speaking Skills.mp4")
//            val packet = Packet.from(Packet.CODE_FILE_START, file.name.toByteArray())
//            service.sendFile(packet, file.inputStream())
//        }
//
//        launch {
//            service.start()
//        }.join()
//    }
//
//}
//

//fun main() {
////        send(packet)
//    // spend a slot for code in the head
//    val file = FileInputStream("B:\\Downloads\\Merriam_Websters_Vocabulary_Builder.pdf")
//    val packet = Packet.from("")
//    file.use { inp ->
//        var quantity = 0
//        while (quantity != -1) {
//            quantity = inp.read(packet.bytes(), Packet.INDEX_DATA, packet.capacity() - Packet.INDEX_DATA)
//            if(quantity != -1) {
//                if (quantity >= EGPSystem.DEFAULT_BUFFER_SIZE - Packet.INDEX_DATA) {
//                    packet.code(Packet.CODE_FILE_SENDING)
//                    println("Quantity=$quantity")
//                } else {
//                    packet.code(Packet.CODE_FILE_END)
//                    println("Last package: " + quantity)
//                }
//                packet.length(quantity)
////                send(packet)
//            }
//        }
//    }
//
//}
