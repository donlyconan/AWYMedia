package com.utc.donlyconan.media.app.localinteraction

import androidx.annotation.WorkerThread
import com.utc.donlyconan.media.app.utils.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket


@WorkerThread
class EGPMediaServer: EGPSystem() {
    private var _serverSocket: ServerSocket? = null
    private val serverSocket: ServerSocket get() = _serverSocket!!
    override fun setup(inetAddress: InetAddress?) {
        println( "setup() called with: inetAddress = $inetAddress")
        _serverSocket = ServerSocket(IP_PORT)
    }

    override suspend fun start() {
        println("Server is started.")
        isAlive = true
        while (isAlive) {
            val socket = serverSocket.accept()
            accept(socket)
        }
        println("Server is shutdown.")
        shutdown()
    }

    override fun shutdown() {
        super.shutdown()
        _serverSocket?.close()
        _serverSocket = null
    }

    override fun isGroupOwner(): Boolean {
        return true
    }

}

//fun main() {
//    runBlocking {
//        val service = EGPSystem.create(EGPMediaServer::class, null)
//
//        service.registerClientServiceListener(object : Client.ClientServiceListener {
//
//                var file: File? = null
//                var outputStream: OutputStream? = null
//
//                override fun onReceive(clientId: Long, bytes: ByteArray) {
//                    val code = bytes[0]
//                    when(code) {
//                        Packet.CODE_FILE_START -> {
//                            val packet = Packet.from(bytes)
//                            file = File("A:\\resources", "${System.currentTimeMillis()}_" +packet.get(String::class))
//                            outputStream = FileOutputStream(file)
//                            println("Filename=${file?.name}")
//                        }
//                        Packet.CODE_FILE_SENDING,  Packet.CODE_FILE_END -> {
//                            outputStream?.write(bytes, 1, bytes.size - 1)
//                            if (code == Packet.CODE_FILE_END) {
//                                outputStream?.flush()
//                                outputStream?.close()
//                                outputStream = null
//                                println("Size=${file?.length()}")
//                            }
//                        }
//                        Packet.CODE_MESSAGE_SEND -> {
//                            println("Message: ${Packet.from(bytes).get(String::class)}")
//                        }
//                        else -> {
//                            Log("Code = ${code} is not found")
//                        }
//                    }
//                }
//
//            }
//        )
//        launch(Dispatchers.IO) {
//            service.start()
//        }.join()
//
//    }
//}