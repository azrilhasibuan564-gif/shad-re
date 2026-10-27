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

class MainActivity : ComponentActivity() {

    private var logText by mutableStateOf("Ready.")
    private var isLoggedIn by mutableStateOf(false)
    private var deviceId by mutableStateOf("")
    private var isBoostOn by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val prefs = getSharedPreferences("shadore", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("logged_in", false) &&
            prefs.getString("device_id", "") == deviceId
        isBoostOn = prefs.getBoolean("boost_on", false)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0A0A)
                ) {
                    if (!isLoggedIn) {
                        LoginScreen()
                    } else {
                        BoosterScreen()
                    }
                }
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var keyInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "shadØre",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                "DEVICE LOCKED",
                fontSize = 12.sp,
                color = Color(0xFFFF3B30),
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "DEVICE ID:",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        deviceId,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("Key Aktivasi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { checkKey(keyInput) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("ACTIVATE")
            }
        }
    }

    @Composable
    fun BoosterScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "shadØre",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                if (isBoostOn) "SYSTEM BOOST: ON" else "SYSTEM BOOST: OFF",
                color = if (isBoostOn) Color.Green else Color.Gray,
                fontSize = 12.sp,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Shizuku Boost",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            if (isBoostOn) "Animasi dipercepat" else "Mati",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked = isBoostOn,
                        onCheckedChange = { toggleBoost(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF3B30),
                            checkedTrackColor = Color(0x55FF3B30)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                logText,
                color = Color(0xFF00FF00),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { toggleBoost(!isBoostOn) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBoostOn) {
                        Color.DarkGray
                    } else {
                        Color(0xFFFF3B30)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    if (isBoostOn) "MATIKAN BOOST" else "NYALAKAN BOOST",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    private fun checkKey(input: String) {
        val licenseKey = input.trim()

        if (licenseKey.isEmpty()) {
            Toast.makeText(
                this,
                "Masukkan key!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Thread {
            var message = "Gagal memeriksa lisensi."
            var activated = false
            var connection: java.net.HttpURLConnection? = null

            try {
                val url = java.net.URL(
                    "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"
                )

                connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.setRequestProperty(
                    "apikey",
                    "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"
                )

                val body = org.json.JSONObject().apply {
                    put("action", "validate")
                    put("license_key", licenseKey)
                    put("device_id", deviceId)
                }

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val status = connection.responseCode
                val stream = if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream?.bufferedReader()?.use {
                    it.readText()
                }.orEmpty()

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
            } catch (e: Exception) {
                message = "Koneksi gagal: ${e.message}"
            } finally {
                connection?.disconnect()
            }

            runOnUiThread {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()

                if (activated) {
                    getSharedPreferences(
                        "shadore",
                        Context.MODE_PRIVATE
                    ).edit()
                        .putBoolean("logged_in", true)
                        .putString("device_id", deviceId)
                        .apply()

                    isLoggedIn = true
                }
            }
        }.start()
    }

    private fun toggleBoost(on: Boolean) {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(
                this,
                "Shizuku belum berjalan. Buka Shizuku dan tekan Start.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            if (
                Shizuku.checkSelfPermission() !=
                PackageManager.PERMISSION_GRANTED
            ) {
                Shizuku.requestPermission(100)

                Toast.makeText(
                    this,
                    "Izinkan SHADØRE di aplikasi Shizuku.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Gagal memeriksa izin: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Thread {
            try {
                val commands = if (on) {
                    listOf(
                        "settings put global window_animation_scale 0.5",
                        "settings put global transition_animation_scale 0.5",
                        "settings put global animator_duration_scale 0.5"
                    )
                } else {
                    listOf(
                        "settings put global window_animation_scale 1.0",
                        "settings put global transition_animation_scale 1.0",
                        "settings put global animator_duration_scale 1.0"
                    )
                }

                commands.forEach { runCmd(it) }

                runOnUiThread {
                    isBoostOn = on

                    getSharedPreferences(
                        "shadore",
                        Context.MODE_PRIVATE
                    ).edit()
                        .putBoolean("boost_on", on)
                        .apply()

                    log(
                        if (on) {
                            "SHADØRE: Animasi berhasil diatur."
                        } else {
                            "SHADØRE: Animasi dikembalikan."
                        }
                    )
                }
            } catch (e: Exception) {
                log("Gagal mengatur boost: ${e.message}")
            }
        }.start()
    }

    private fun runCmd(cmd: String) {
        var process: ShizukuRemoteProcess? = null

        try {
            process = Shizuku.newProcess(
                arrayOf("sh", "-c", cmd),
                null,
                null
            )

            val output = BufferedReader(
                InputStreamReader(process.inputStream)
            ).use { it.readText() }

            val error = BufferedReader(
                InputStreamReader(process.errorStream)
            ).use { it.readText() }

            val exitCode = process.waitFor()

            log(
                "> $cmd\n" +
                    output +
                    error +
                    "\nExit code: $exitCode"
            )
        } catch (e: Exception) {
            log("ERR: ${e.message}")
        } finally {
            try {
                process?.destroy()
            } catch (_: Exception) {
            }
        }
    }

    private fun log(message: String) {
        runOnUiThread {
            logText += "\n$message"
        }
    }
}
