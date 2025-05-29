// BP6 Non-Scan Admin project
// Written by: Nina Schrauwen
// Description: This Kotlin file defines a TcpSession object that manages a TCP connection to a server. It provides methods to connect, send messages, and close the connection. The connection is established using a socket, and communication is handled through buffered readers and writers. The object also includes error handling for connection issues and message sending/receiving.

package com.example.tcpclientadmin

import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

// TcpSession object to manage the TCP connection
object TcpSession {
//  Global variables to hold the socket, writer, and reader
    var socket: Socket? = null
    var writer: BufferedWriter? = null
    var reader: BufferedReader? = null

//   Function to connect to the TCP server
    fun connect(ip: String, port: Int): Boolean {
        return try {
//          Create a new socket and connect to the server
            val sock = Socket()
//          Try to connect to the server with a timeout of 2000 milliseconds
            sock.connect(InetSocketAddress(ip, port), 2000)
//          Assign the socket to the global variable and initialize the writer and reader
            socket = sock
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
//          Log the successful connection
            Log.d("TCP", "Connected to $ip:$port ✅")
            true
//      Catch any exceptions that occur during the connection attempt
        } catch (e: Exception) {
            Log.e("TCP", "Failed to connect to $ip:$port ❌", e)
            false
        }
    }

//   Function to send a message to the server and receive a response
    fun sendMessage(msg: String): String? {
        return try {
//          Allow the writer to write the message to the server and the reader to read the response when called
            writer?.write(msg)
            writer?.newLine()
            writer?.flush()
            reader?.readLine()
//      Catch any exceptions that occur during the reading or writing process
        } catch (e: Exception) {
            null
        }
    }

//   Function to close the socket and its streams
    fun close() {
        try {
//          Close the writer, reader, and socket if they are not null
            writer?.close()
            reader?.close()
            socket?.close()
//      Catch any exceptions that occur during the socket closing process
        } catch (e: Exception) {}
    }
}
