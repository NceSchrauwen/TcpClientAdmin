// BP6 Non-Scan Admin project
// Written by: Nina Schrauwen
// Description: This Kotlin file defines a SettingsManager object that retrieves the IP address from a JSON file in the assets folder.
// It provides a function to read the IP address from the JSON file and return it as a string.

package com.example.tcpclientadmin

import android.content.Context
import org.json.JSONObject
import java.io.File

// Object to manage settings, specifically to retrieve the IP address from a JSON file
object SettingsManager {

//  Function to get the IP address from a JSON file in the assets folder
    fun getIp(context: Context): String {
//       Open the JSON file from the assets folder and read its content
        val jsonFile = context.assets.open("settings.json").bufferedReader().use { it.readText() }
//      Parse the JSON content to extract the IP address
        val ip = JSONObject(jsonFile).getString("ip")

        return ip
    }

}
