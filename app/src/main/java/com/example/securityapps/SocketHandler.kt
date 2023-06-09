package com.example.securityapps
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    private lateinit var socket: Socket

    @Synchronized
    fun getSocket(): Socket {
        if (!::socket.isInitialized) {
            try {
                // Set up socket connection
                val opts = IO.Options()
                opts.forceNew = true
                socket = IO.socket("http://192.168.1.10:5000/callPhone")
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
        return socket
    }

    @Synchronized
    fun establishConnection() {
        getSocket().connect()
    }

    @Synchronized
    fun closeConnection() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
}
