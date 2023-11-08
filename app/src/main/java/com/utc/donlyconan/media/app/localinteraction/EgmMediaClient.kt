package com.utc.donlyconan.media.app.localinteraction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.Scanner
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
        launch(Dispatchers.IO) {
            Log("setup")
            clientService.apply {
                setup()
                run()
            }
        }
           Log("socketEvent")
        clientService.registerSocketEvent(object : SocketEvent {
            override fun onReceive(command: Command) {
                Log("Received: " + command.get(String::class))
            }
        })
        do {
            val scanner = Scanner(System.`in`)
            println("Enter something: ")
            val data = scanner.nextLine()
            clientService.client?.send(Command.from(data))
        } while (!data.equals("exit"))
        println("End of system.")
    }

}

