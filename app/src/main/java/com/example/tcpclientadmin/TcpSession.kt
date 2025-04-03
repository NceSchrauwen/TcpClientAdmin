package com.example.tcpclientadmin

import java.io.*
import java.net.Socket

object TcpSession {
    var socket: Socket? = null
    var writer: BufferedWriter? = null
    var reader: BufferedReader? = null

    fun connect(ip: String, port: Int): Boolean {
        return try {
            socket = Socket(ip, port)
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun sendMessage(msg: String): String? {
        return try {
            writer?.write(msg)
            writer?.newLine()
            writer?.flush()
            reader?.readLine()
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {}
    }
}
