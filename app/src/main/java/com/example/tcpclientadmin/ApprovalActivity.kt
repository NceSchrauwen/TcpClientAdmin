// BP6 Non-Scan Admin project
// Written by: Nina Schrauwen
// Description: This Kotlin file defines an ApprovalActivity that handles non-scan approval requests from a server.
// It manages user sessions, displays requests, and allows users to approve or deny requests. The activity also handles socket communication, including sending and receiving messages, and checks for connection status using ping/pong messages. The activity provides a user interface for interaction and handles logout functionality.

package com.example.tcpclientadmin

import SessionManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ApprovalActivity : AppCompatActivity() {
    // UI element declarations
    private lateinit var requestText: TextView
    private lateinit var welcomeWorker: TextView
    private lateinit var approveBtn: Button
    private lateinit var denyBtn: Button
    private lateinit var retryBtn: Button
    private lateinit var logoutBtn: Button

    // Session and socket variables, also sets the port number
    private lateinit var sessionManager: SessionManager
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val port = 12345

    // Used for ping/pong connection check
    var lastPongTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the approval activity layout (main screen)
        setContentView(R.layout.approval_activity)

        // Get username from intent and initialize session manager
        val username = intent.getStringExtra("username")
        sessionManager = SessionManager(this)

        // Find UI elements by ID
        logoutBtn = findViewById(R.id.logoutButton)
        welcomeWorker = findViewById(R.id.welcomeWorker)
        requestText = findViewById(R.id.requestText)
        approveBtn = findViewById(R.id.approveButton)
        denyBtn = findViewById(R.id.denyButton)
        retryBtn = findViewById(R.id.retryButton)

        // Disable approve/deny buttons until a request is received
        approveBtn.isEnabled = false
        denyBtn.isEnabled = false

        // Get server IP from settings
        val ip = SettingsManager.getIp(this)

        // Check if the user is logged in, if not, redirect to login
        if(!sessionManager.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Display welcome message with username (name extracted from log in activity)
        // If name is known, show it; otherwise, show a default message
        if(username !== null) {
            welcomeWorker.text = "Welcome, $username!"
        } else {
            welcomeWorker.text = "No username found!"
        }

        // Logout function: clears session and navigates to login
        fun logout() {
            sessionManager.setLogin(false)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Logout button click listener
        logoutBtn.setOnClickListener {
            // Clear session and navigate to login activity
            logout()
        }

        // Retry button: sends a request to fetch the latest non-scan approval request from the server
        retryBtn.setOnClickListener {
        // Write to server to fetch latest non-scan approval request, make sure no data is left after sending the command
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    TcpSession.writer?.write("FETCH_LATEST")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
                // If a message isn't able to be sent, catch the exception and display an error message
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending fetch request: ${e.message}"
                    }
                }
            }
        }

        // Coroutine: listens for messages from server
        GlobalScope.launch(Dispatchers.IO) {
            try {
//              Connect to the server using the IP and port, using the TcpSession object and the reader/writer variables
                val reader = TcpSession.reader
                while (true) {
                    // Read a line from the server
                    val line = reader?.readLine()

                    // Handle PONG for connection check
                    if (line == "PONG"){
//                       Update lastPongTime to current time
                        lastPongTime = System.currentTimeMillis()
                    // Handle approval request
                    } else if (line == "NONSCAN_REQUEST") {
                        withContext(Dispatchers.Main) {
//                          Update UI to show non-scan approval request is received
                            requestText.text = "Non-scan approval is request received!"
//                          Enable approve and deny buttons (Which should be greyed out until a request is received)
                            approveBtn.isEnabled = true
                            denyBtn.isEnabled = true
                        }
                    // Handle forced logout from server
                    } else if (line == "LOGOUT") {
                        withContext(Dispatchers.Main) {
//                          Update UI to show disconnection message
                            requestText.text = "Disconnected by server"
//                          Logout the user
                            logout()
                        }
                        break
                    }
                }
//          Catch any exceptions that occur while reading from the server and display the error message
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    requestText.text = "Error: ${e.message}"
                }
            }
        }

        // Coroutine: sends PING every second, checks for PONG timeout from server
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                try {
//                  Send PING to server
                    TcpSession.writer?.write("PING")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
//              Catch any exceptions that occur while sending the PING and display the error message
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Ping error: ${e.message}"
                        // If ping fails, logout (inactive server)
                        logout()
                    }
                    break
                }

                // If no PONG in 6 seconds, consider connection lost
                if (System.currentTimeMillis() - lastPongTime > 6000) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Connection lost (no PONG received). Logging out."
                        // If no PONG received, logout
                        logout()
                    }
                    break
                }

                delay(1000) // Wait 1 second before next ping
            }
        }

        // Approve button: sends APPROVED to server
        approveBtn.setOnClickListener {
//          Launch a coroutine to handle the button click
            GlobalScope.launch(Dispatchers.IO) {
                try {
//                  Send APPROVED message to server and flush the writer
                    TcpSession.writer?.write("APPROVED")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
//                  Update UI to show response sent and grey out the (approve/deny) buttons
                    withContext(Dispatchers.Main) {
                        requestText.text = "Response sent: APPROVED"
                        approveBtn.isEnabled = false
                        denyBtn.isEnabled = false
                    }
                // Catch any exceptions that occur while sending the approval and display the error message
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending approval: ${e.message}"
                    }
                }
            }
        }

        // Deny button: sends DENIED to server
        denyBtn.setOnClickListener {
//          Launch a coroutine to handle the button click
            GlobalScope.launch(Dispatchers.IO) {
                try {
//                  Send DENIED message to server and flush the writer
                    TcpSession.writer?.write("DENIED")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
                    // Update UI to show response sent and grey out the (approve/deny) buttons
                    withContext(Dispatchers.Main) {
                        requestText.text = "Response sent: DENIED"
                        approveBtn.isEnabled = false
                        denyBtn.isEnabled = false
                    }
//              Catch any exceptions that occur while sending the denial and display the error message
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending denial: ${e.message}"
                    }
                }
            }
        }
    }
}