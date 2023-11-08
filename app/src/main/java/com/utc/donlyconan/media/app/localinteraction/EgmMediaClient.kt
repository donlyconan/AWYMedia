package com.utc.donlyconan.media.app.localinteraction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.Scanner
import kotlin.experimental.or
import kotlin.math.log

class EgmMediaClient: EgmSystem() {
    lateinit var socket: SocketChannel
    var client: Client? = null

    override fun setup() {
        Log("connect: Client is connecting to the server...")
        socket = SocketChannel.open(InetSocketAddress(HOSTNAME, IP_PORT))
        socket.configureBlocking(false)
        Log("connect: Client was setup and run...")
        selector = Selector.open()
        val key = socket.register(selector, SelectionKey.OP_READ)
        client = Client(key, selector, socket)
        key.attach(client)
    }

    override fun shutdown() {
        super.shutdown()
        socket.close()
    }

    override fun isServer(): Boolean {
        return false
    }
}

fun main() {
    runBlocking {
        val clientService = EgmMediaClient()
        var file: File? = null
        var outputStream: OutputStream? = null


        clientService.registerSocketEvent(object : SocketEvent {

            override fun onReceive(command: Command) {
                Log("onReceive() called with: command = ${command.code}")
                when(command.code) {
                    Command.CODE_FILE_START -> {
                        file = File("A:\\resources",command.get(String::class))
                        outputStream = FileOutputStream(file)
                    }
                    Command.CODE_FILE_START -> {
                        file = File("A:\\resources",command.get(String::class))
                        outputStream = FileOutputStream(file)
                    }
                    Command.CODE_FILE_SENDING,  Command.CODE_FILE_END -> {
                        outputStream?.write(command.bytes, )
                        if (command.code == Command.CODE_FILE_END) {
                            outputStream?.flush()
                            outputStream?.close()
                            outputStream = null
                        }
                    }
                }

            }
        })
        delay(1000)
        launch(Dispatchers.IO) {
            Log("setup")
            clientService.apply {
                setup()
                run()
            }
        }
           Log("socketEvent")

        do {
            val scanner = Scanner(System.`in`)
            println("Enter something: ")
            val data = scanner.nextLine()
            clientService.client?.send(Command.from(data))
        } while (!data.equals("exit"))
        println("End of system.")
    }

}

