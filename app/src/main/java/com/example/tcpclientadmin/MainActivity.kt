// BP6 Non-Scan Admin project
// Written by: Nina Schrauwen
// Description: This Kotlin file handles the login functionality for the application, including user authentication via TCP connection.
// It also establishes a connection to the server, sends login credentials, and manages the user session.

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
    // Declare UI elements
    private lateinit var userIdEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var resultTextView: TextView

//  Declare a SessionManager instance to manage user session and login state
    private lateinit var sessionManager: SessionManager

//  Define the port number for the TCP connection
    private val port = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the activity_main layout (login screen)
        setContentView(R.layout.activity_main)

        // Initialize the SessionManager to handle user session management
        sessionManager = SessionManager(this)

//      Link UI elements to their respective views in the layout
        userIdEditText = findViewById(R.id.userIdEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        resultTextView = findViewById(R.id.resultTextView)

//      Function to handle login communication with the server
        loginButton.setOnClickListener {
//          Retrieve user input from EditText fields
            val userId = userIdEditText.text.toString()
            val password = passwordEditText.text.toString()

//          Check if the userId or password fields are empty
            if (userId.isBlank() || password.isBlank()) {
                resultTextView.text = "Please fill in both fields."
                return@setOnClickListener
            }


//          Start a coroutine to handle the TCP connection and login process and display the result
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    resultTextView.text = "Attempting connection..."
                }

                try {
//                  Get the current IP address and port number from SettingsManager and attempt to connect to the server
                    val ip = SettingsManager.getIp(this@MainActivity)
                    val connected = TcpSession.connect(ip, port)

//                  If the connection is not successful, update the UI with an error message
                    if (!connected) {
                        withContext(Dispatchers.Main) {
                            resultTextView.text = "Could not connect to server."
                        }
                        return@launch
                    }

//                  If the connection is successful, send the login credentials to the server for authentication
                    val loginMessage = "LOGIN,$userId,$password"
                    val response = TcpSession.sendMessage(loginMessage)
                    Log.d("TCP", "Server response: $response")

                    withContext(Dispatchers.Main) {
//                      If the response indicates a successful login, extract the username and start the ApprovalActivity
                        if (response?.startsWith("LOGIN_SUCCESS") == true) {
                            val username = response.split(",")[1]
//                          Set the user's login state
                            sessionManager.setLogin(true)
                            val intent = Intent(this@MainActivity, ApprovalActivity::class.java)
//                          Username can be retreived in the ApprovalActivity
                            intent.putExtra("username", username)
                            startActivity(intent)
                            resultTextView.setText("Login successful: $response")
//                      If no response is received/not defined, display a message indicating the server is not responding
                        } else if (response == null) {
                            resultTextView.setText("Server is not responding")
//                      If no response is received/not defined, display a message indicating the server is not responding
                        } else if (response.isNullOrEmpty()){
                            resultTextView.setText("Server is not responding")
//                      Otherwise, display the response from the server indicating a failed login (credentials incorrect or device on other network, etc)
                        } else {
                            resultTextView.setText("Login not successful: $response")
                            TcpSession.close()
                        }
                    }
//              Catch any exceptions that occur during the connection or login process and display an error message
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
