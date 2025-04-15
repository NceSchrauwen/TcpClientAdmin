package com.example.tcpclientadmin

import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

object TcpSession {
    var socket: Socket? = null
    var writer: BufferedWriter? = null
    var reader: BufferedReader? = null

    fun connect(ip: String, port: Int): Boolean {
        return try {
            val sock = Socket()
            sock.connect(InetSocketAddress(ip, port), 2000)
            socket = sock
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            Log.d("TCP", "Connected to $ip:$port ✅")
            true
        } catch (e: Exception) {
            Log.e("TCP", "Failed to connect to $ip:$port ❌", e)
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
