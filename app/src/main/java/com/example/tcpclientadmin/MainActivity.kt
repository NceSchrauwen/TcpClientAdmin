package com.example.tcpclientadmin

import SessionManager
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

    private lateinit var sessionManager: SessionManager

    private val port = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

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
                withContext(Dispatchers.Main) {
                    resultTextView.text = "Attempting connection..."
                }

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

                    withContext(Dispatchers.Main) {
                        if (response?.startsWith("LOGIN_SUCCESS") == true) {
                            val username = response.split(",")[1]
//                          Set the user's login state
                            sessionManager.setLogin(true)
                            val intent = Intent(this@MainActivity, ApprovalActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            resultTextView.setText("Login successful: $response")
                        } else if (response.isNullOrEmpty()){
                            resultTextView.setText("Server is not responding")
                        } else {
                            resultTextView.setText("Login not successful: $response")
                            TcpSession.close()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        resultTextView.text = "❌ Exception: ${e.message}"
                        Log.e("TCP", "Exception during login", e)
                    }
                }
            }
        }
    }
}
