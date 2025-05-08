package com.example.tcpclientadmin

import android.content.Context
import org.json.JSONObject
import java.io.File

object SettingsManager {

    fun getIp(context: Context): String {
        val jsonFile = context.assets.open("settings.json").bufferedReader().use { it.readText() }
        val ip = JSONObject(jsonFile).getString("ip")

        return ip
    }

}
