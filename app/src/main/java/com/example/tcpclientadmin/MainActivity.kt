package com.example.tcpclientadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var userIdEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var resultTextView: TextView

    private val port = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userIdEditText = findViewById(R.id.userIdEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        resultTextView = findViewById(R.id.resultTextView)

        loginButton.setOnClickListener {
            val userId = userIdEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (userId.isBlank() || password.isBlank()) {
                resultTextView.text = "Please fill in both fields."
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val ip = SettingsManager.getIp(this@MainActivity)
                    val connected = TcpSession.connect(ip, port)

                    if (!connected) {
                        withContext(Dispatchers.Main) {
                            resultTextView.text = "Could not connect to server."
                        }
                        return@launch
                    }

                    val loginMessage = "LOGIN,$userId,$password"
                    val response = TcpSession.sendMessage(loginMessage)
                    Log.d("TCP", "Server response: $response")

//                  TODO: Add username to the approval activity screen
                    withContext(Dispatchers.Main) {
                        if (response?.startsWith("LOGIN_SUCCESS") == true) {
//                            val username = response.split(",")[1]
//                            resultTextView.text = "Login successful: $username"
                            val intent = Intent(this@MainActivity, ApprovalActivity::class.java)
                            startActivity(intent)
                            resultTextView.setText("Login successful: $response")
                        } else {
                            resultTextView.setText("Login not successful: $response")
                            TcpSession.close()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        resultTextView.text = "Error: ${e.message}"
                    }
                }
            }
        }
    }
}
