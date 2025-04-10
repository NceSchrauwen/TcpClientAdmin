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

    private lateinit var requestText: TextView
    private lateinit var welcomeWorker: TextView
    private lateinit var approveBtn: Button
    private lateinit var denyBtn: Button
    private lateinit var retryBtn: Button
    private lateinit var logoutBtn: Button

    private lateinit var sessionManager: SessionManager
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val port = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.approval_activity)

        val username = intent.getStringExtra("username")
        sessionManager = SessionManager(this)

        logoutBtn = findViewById(R.id.logoutButton)
        welcomeWorker = findViewById(R.id.welcomeWorker)
        requestText = findViewById(R.id.requestText)
        approveBtn = findViewById(R.id.approveButton)
        denyBtn = findViewById(R.id.denyButton)
        retryBtn = findViewById(R.id.retryButton)

        approveBtn.isEnabled = false
        denyBtn.isEnabled = false

        val ip = SettingsManager.getIp(this)

//      If the user is not logged in redirect to the login screen
        if(!sessionManager.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

//      Display the name of the worker that logged in
        if(username !== null) {
            welcomeWorker.text = "Welcome, $username!"
        } else {
            welcomeWorker.text = "No username found!"
        }

//      Function to be able to logout and be redirected to the login page
        fun logout() {
            // 1. Clear the user's login state
            sessionManager.setLogin(false)

            // 2. Create an Intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // 3. Clear the activity stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 4. Start MainActivity
            startActivity(intent)

            // 5. Finish the current activity (ApprovalActivity)
            finish()
        }

        logoutBtn.setOnClickListener {
            logout()
        }

        retryBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    TcpSession.writer?.write("FETCH_LATEST")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending fetch request: ${e.message}"
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val reader = TcpSession.reader
                while (true) {
                    val line = reader?.readLine()
                    if (line == "NONSCAN_REQUEST") {
                        withContext(Dispatchers.Main) {
                            requestText.text = "Non-scan approval is request received!"
                            approveBtn.isEnabled = true
                            denyBtn.isEnabled = true
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    requestText.text = "Error: ${e.message}"
                }
            }
        }


        approveBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    TcpSession.writer?.write("APPROVED")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
                    withContext(Dispatchers.Main) {
                        requestText.text = "Response sent: APPROVED"
                        approveBtn.isEnabled = false
                        denyBtn.isEnabled = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending approval: ${e.message}"
                    }
                }
            }
        }

        denyBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    TcpSession.writer?.write("DENIED")
                    TcpSession.writer?.newLine()
                    TcpSession.writer?.flush()
                    withContext(Dispatchers.Main) {
                        requestText.text = "Response sent: DENIED"
                        approveBtn.isEnabled = false
                        denyBtn.isEnabled = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requestText.text = "Error sending denial: ${e.message}"
                    }
                }
            }
        }
    }
}
