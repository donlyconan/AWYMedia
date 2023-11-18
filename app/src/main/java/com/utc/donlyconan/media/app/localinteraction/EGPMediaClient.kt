package com.utc.donlyconan.media.app.localinteraction

import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket

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
        println( "accept() called with: socket = $socket")
        val client = Client(CLIENT_ID, socket)
        clients[client.clientId] = client
        client.clientServiceListener = clientServiceListener
    }

    override suspend fun start() {
        println("Client is started with systemName=$systemName")
        isAlive = true
        systemName?.let { name -> send(Packet.from(Packet.CODE_DEVICE_NAME, name.toByteArray())) }
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
//    val file = FileInputStream("C:\\Users\\Admin\\AWYMedia\\app\\src\\main\\res\\values-vi\\strings.xml")
//    val fileEn = FileInputStream("C:\\Program Files (x86)\\Notepad++\\es.txt")
//    runBlocking {
//        file.bufferedReader().use { reader ->
//            reader.readLines().asFlow()
//                .filter { it.contains("</string>") }
//                .zip(fileEn.bufferedReader().readLines().asFlow()) { f1, f2 ->
//                    val content = f1.substringAfter("\">").substringBeforeLast("</string")
//                    f1.replace(content, f2)
//                }.collectLatest {
//                    println(it)
//                }
//        }
//    }
//
//}
