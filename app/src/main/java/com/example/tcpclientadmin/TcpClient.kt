package com.example.tcpclientadmin

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

object TcpClient {
    fun sendMessage(ip: String, port: Int, message: String): String {
        Socket(ip, port).use { socket ->
            val output: OutputStream = socket.getOutputStream()
            output.write(message.toByteArray())
            output.flush()

            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            return input.readLine() ?: ""
        }
    }
}
