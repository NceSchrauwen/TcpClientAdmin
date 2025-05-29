// BP6 Non-Scan Admin project
// Written by: Nina Schrauwen
// Description: This Kotlin file is in charge of managing the user session and login state for the application.

import android.content.Context
import android.content.SharedPreferences

// Class to manage user session and login state
class SessionManager(context: Context) {

    // SharedPreferences to store user login state
    private var prefs: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    // Key for the login state, which indicates if the user is logged in or not
    companion object {
        const val USER_LOGGED_IN = "user_logged_in"
    }

    // Function to set the login state
    fun setLogin(isLoggedIn: Boolean) {
//      Create an editor to modify the SharedPreferences
        val editor = prefs.edit()
        // Put the login state into the SharedPreferences and apply the changes
        editor.putBoolean(USER_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    // Function to check if the user is logged in, defaulting to false if not set
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(USER_LOGGED_IN, false)
    }
}