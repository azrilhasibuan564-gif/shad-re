package com.shadore.booster

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    private var logText by mutableStateOf("Menunggu...")
    private var isLoggedIn by mutableStateOf(false)
    private var isShadOn by mutableStateOf(false)
    private var deviceId by mutableStateOf("")
    private val SECRET = "shadore_secret_key_v2_2025"
    private val PERM_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val prefs = getSharedPreferences("shadore", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("logged_in", false) && prefs.getString("device_id", "") == deviceId
        isShadOn = prefs.getBoolean("shad_on", false)

        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                logText = "Shizuku Connected! Siap ON"
            }
        }

        setContent {
           
