package com.utc.donlyconan.media.app.localinteraction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.Scanner

class EGPMediaClient: EGPSystem() {
    var socket: SocketChannel? = null

    override fun setup(ipAddress: String) {
        Log("Client is setting up...")
        socket = SocketChannel.open(InetSocketAddress(ipAddress, IP_PORT)).apply {
            configureBlocking(false)
            _selector = Selector.open()
            val key = register(selector, SelectionKey.OP_READ)
            val client = Client(key, selector, this)
            key.attach(client)
            clients[key] = client
        }
    }

    override fun shutdown() {
        super.shutdown()
        socket?.close()
        socket = null
    }

    override fun isGroupOwner(): Boolean {
        return false
    }
}

fun main() {
    runBlocking {
        val clientService = EGPSystem.create(EGPMediaClient::class, EGPSystem.HOSTNAME)
        var file: File? = null
        var outputStream: OutputStream? = null


        clientService.registerSocketEvent(object : SocketEvent {

            override fun onReceive(command: Command) {
                super.onReceive(command)
                Log("onReceive() called with: command = ${command.code}")
                when(command.code) {
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
                    else -> {
                        Log("Code = ${command.code} is not found")
                    }
                }

            }
        })
        delay(1000)
        launch(Dispatchers.IO) {
            Log("setup")
            clientService.apply {
                run()
            }
        }
           Log("socketEvent")

        do {
            val scanner = Scanner(System.`in`)
            println("Enter something: ")
            val data = scanner.nextLine()
            clientService.sendWith(Command.from(data))
        } while (!data.equals("exit"))
        println("End of system.")
    }

}

