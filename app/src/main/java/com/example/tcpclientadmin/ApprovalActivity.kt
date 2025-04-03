package com.example.tcpclientadmin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class ApprovalActivity : AppCompatActivity() {

    private lateinit var requestText: TextView
    private lateinit var approveBtn: Button
    private lateinit var denyBtn: Button

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val port = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.approval_activity)

        requestText = findViewById(R.id.requestText)
        approveBtn = findViewById(R.id.approveButton)
        denyBtn = findViewById(R.id.denyButton)

        approveBtn.isEnabled = false
        denyBtn.isEnabled = false

        val ip = SettingsManager.getIp(this)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val reader = TcpSession.reader
                while (true) {
                    val line = reader?.readLine()
                    if (line == "NONSCAN_REQUEST") {
                        withContext(Dispatchers.Main) {
                            requestText.text = "Non-scan approval request received."
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

//    override fun onDestroy() {
//        super.onDestroy()
////        TcpSession.close()
//    }
}
