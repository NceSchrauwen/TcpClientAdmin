package com.example.tcpclientadmin

import android.content.Context
import org.json.JSONObject
import java.io.File

object SettingsManager {
    private const val SETTINGS_FILE = "settings.json"

    fun getIp(context: Context): String {
        val file = File(context.filesDir, SETTINGS_FILE)

        // First-time setup with dummy IP
        if (!file.exists()) {
            file.writeText("""{"ip": "192.168.2.30"}""")
        }

        val json = JSONObject(file.readText())
        return json.getString("ip")
    }

}
