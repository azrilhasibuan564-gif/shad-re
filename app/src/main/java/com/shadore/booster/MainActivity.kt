package with.shadore.booster

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
    private var logText by mutableStateOf("Ready.")
    private var isLoggedIn by mutableStateOf(false)
    private var deviceId by mutableStateOf("")
    private var isBoostOn by mutableStateOf(false)
    private val SECRET = "shadore_secret_v2_2025_pro"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val prefs = getSharedPreferences("shadore", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("logged_in", false) && prefs.getString("device_id", "") == deviceId
        isBoostOn = prefs.getBoolean("boost_on", false)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0A0A0A)) {
                    if (!isLoggedIn) LoginScreen() else BoosterScreen()
                }
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var keyInput by remember { mutableStateOf("") }
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("shadØre", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("DEVICE LOCKED", fontSize = 12.sp, color = Color(0xFFFF3B30), letterSpacing = 3.sp)
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("DEVICE ID:", color = Color.Gray, fontSize = 10.sp)
                    Text(deviceId, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = keyInput, onValueChange = { keyInput = it }, label = { Text("Key Aktivasi") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { checkKey(keyInput) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("ACTIVATE")
            }
        }
    }

    @Composable
    fun BoosterScreen() {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("shadØre", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(if(isBoostOn) "SYSTEM BOOST: ON" else "SYSTEM BOOST: OFF", color = if(isBoostOn) Color.Green else Color.Gray, fontSize = 12.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(24.dp))
            
            // INI TOGGLE YANG NGARUH KE SISTEM
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Shizuku Boost", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if(isBoostOn) "Berpengaruh ke sistem" else "Mati", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = isBoostOn, onCheckedChange = { toggleBoost(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF3B30), checkedTrackColor = Color(0x55FF3B30)))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text(logText, color = Color(0xFF00FF00), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().height(200.dp))
            Spacer(Modifier.height(16.dp))
            Button(onClick = { toggleBoost(!isBoostOn) }, colors = ButtonDefaults.buttonColors(containerColor = if(isBoostOn) Color.DarkGray else Color(0xFFFF3B30)), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(if(isBoostOn) "MATIKAN BOOST" else "NYALAKAN BOOST", fontWeight = FontWeight.Black)
            }
        }
    }

    
private fun checkKey(input: String) {
    val licenseKey = input.trim()

    if (licenseKey.isEmpty()) {
        Toast.makeText(this, "Masukkan key!", Toast.LENGTH_SHORT).show()
        return
    }

    Thread {
        var message = "Gagal memeriksa lisensi."
        var activated = false

        try {
            val url = java.net.URL(
                "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"
            )
            val conn = url.openConnection() as java.net.HttpURLConnection

            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("apikey", "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B")

            val body = org.json.JSONObject().apply {
                put("action", "validate")
                put("license_key", licenseKey)
                put("device_id", deviceId)
            }

            conn.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream
                         else conn.errorStream

            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = org.json.JSONObject(response)

            activated = status in 200..299 &&
                json.optBoolean("success", false)

            message = if (activated) {
                "Lisensi berhasil diaktifkan!"
            } else {
                json.optString("error")
                    .ifBlank { json.optString("message") }
                    .ifBlank { "Key ditolak (HTTP $status)" }
            }

            conn.disconnect()
        } catch (e: Exception) {
            message = "Koneksi gagal: ${e.message}"
        }

        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            if (activated) {
                getSharedPreferences("shadore", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("logged_in", true)
                    .putString("device_id", deviceId)
                    .apply()

                isLoggedIn = true
            }
        }
    }.start()
}

    }

    private fun toggleBoost(on: Boolean) {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(100)
                Toast.makeText(this, "Allow Shizuku dulu!", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Shizuku belum terinstall!", Toast.LENGTH_SHORT).show()
            return
        }

        isBoostOn = on
        getSharedPreferences("shadore", Context.MODE_PRIVATE).edit().putBoolean("boost_on", on).apply()

        Thread {
            if (on) {
                log(">>> SHADØRE ON - MINTA AKSES SHIZUKU & PENGARUHI SISTEM")
                // INI YANG BIKIN NGARUH KE SISTEM VIA SHIZUKU
                runCmd("cmd power set-fixed-performance-mode-enabled true")
                runCmd("cmd thermalservice override-status 0") // matiin throttling
                runCmd("settings put global window_animation_scale 0.5")
                runCmd("settings put global transition_animation_scale 0.5")
                runCmd("settings put global animator_duration_scale 0.5")
                runCmd("cmd package bg-dexopt-job") // compile ulang biar ngebut
                runCmd("dumpsys deviceidle whitelist +with.shadore.booster")
                runCmd("am kill-all") // bersihin ram
                log(">>> BOOST AKTIF! Performance mode ON, Thermal OFF")
            } else {
                log(">>> SHADØRE OFF - RESTORE SISTEM")
                runCmd("cmd power set-fixed-performance-mode-enabled false")
                runCmd("cmd thermalservice override-status -1")
                runCmd("settings put global window_animation_scale 1.0")
                runCmd("settings put global transition_animation_scale 1.0")
                runCmd("settings put global animator_duration_scale 1.0")
                log(">>> BOOST MATI, sistem balik normal")
            }
        }.start()
    }

    private fun runCmd(cmd: String) {
        try {
            val p: ShizukuRemoteProcess = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            p.waitFor()
            val r = BufferedReader(InputStreamReader(p.inputStream)).readText()
            log("> $cmd\n$r")
        } catch (e: Exception) { log("ERR: ${e.message}") }
    }
    private fun log(s: String) { runOnUiThread { logText += "\n$s" } }
}
