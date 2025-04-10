import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_LOGGED_IN = "user_logged_in"
    }

    fun setLogin(isLoggedIn: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(USER_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(USER_LOGGED_IN, false)
    }
}