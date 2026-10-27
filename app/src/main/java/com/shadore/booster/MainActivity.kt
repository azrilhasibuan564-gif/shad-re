package with.shadore.booster

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    companion object {
        private const val SUPABASE_URL =
            "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"

        private const val SUPABASE_KEY =
            "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"
    }

    private var statusText by mutableStateOf("Memeriksa Shizuku...")
    private var logText by mutableStateOf("SHADØRE siap.")
    private var isBoostOn by mutableStateOf(false)
    private var isChecking by mutableStateOf(false)

    private val binderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            runOnUiThread {
                statusText = "Shizuku terhubung."
                addLog("Binder Shizuku diterima.")
            }
        }

    private val binderDeadListener =
        Shizuku.OnBinderDeadListener {
            runOnUiThread {
                statusText = "Shizuku terputus."
                addLog("Koneksi Shizuku terputus.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)

        updateShizukuStatus()

        setContent {
            var licenseKey by remember { mutableStateOf("") }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF080808)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SHADØRE",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "GAME BOOSTER",
                            color = Color(0xFFFF4545),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF171717),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    "STATUS SISTEM",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    statusText,
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = licenseKey,
                                    onValueChange = { licenseKey = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Masukkan license key")
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii
                                    )
                                )

                                Spacer(Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (isBoostOn) {
                                            toggleBoost(false)
                                        } else if (licenseKey.isBlank()) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Masukkan license key dahulu.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (!isChecking) {
                                            validateLicense(
                                                licenseKey.trim()
                                            ) { valid ->
                                                if (valid) {
                                                    toggleBoost(true)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBoostOn)
                                            Color(0xFF333333)
                                        else
                                            Color(0xFFFF3030)
                                    )
                                ) {
                                    Text(
                                        when {
                                            isChecking -> "MEMERIKSA LISENSI..."
                                            isBoostOn -> "MATIKAN BOOST"
                                            else -> "NYALAKAN BOOST"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF121212),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "BUILD LOG",
                                    color = Color(0xFFFF4545),
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    logText,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "SHADØRE • Android",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    private fun updateShizukuStatus() {
        if (Shizuku.pingBinder()) {
            statusText = "Shizuku terhubung."
            addLog("Binder Shizuku tersedia.")
        } else {
            statusText = "Shizuku belum terhubung ke aplikasi."
            addLog("Periksa Shizuku dan izin aplikasi.")
        }
    }

    private fun validateLicense(
        key: String,
        callback: (Boolean) -> Unit
    ) {
        isChecking = true
        statusText = "Memvalidasi lisensi..."
        addLog("Memulai validasi lisensi.")

        Thread {
            var valid = false
            var message = "Validasi gagal."

            try {
                val deviceId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown-device"

                val connection =
                    URL(SUPABASE_URL).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.setRequestProperty("apikey", SUPABASE_KEY)

                val body = JSONObject()
                    .put("action", "validate")
                    .put("license_key", key)
                    .put("device_id", deviceId)
                    .toString()

                connection.outputStream.use {
                    it.write(body.toByteArray(Charsets.UTF_8))
                }

                val code = connection.responseCode
                val stream = if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream?.bufferedReader()?.use {
                    it.readText()
                }.orEmpty()

                connection.disconnect()

                val json = JSONObject(response)
                valid = code in 200..299 &&
                    json.optBoolean("valid", false)

                message = if (valid) {
                    "Lisensi valid."
                } else {
                    json.optString(
                        "error",
                        "Lisensi tidak valid."
                    )
                }
            } catch (e: Exception) {
                message = "Gagal menghubungi server. Periksa internet dan endpoint."
            }

            runOnUiThread {
                isChecking = false
                statusText = message
                addLog(message)
                callback(valid)
            }
        }.start()
    }

    private fun toggleBoost(on: Boolean) {
        if (!Shizuku.pingBinder()) {
            isBoostOn = false
            statusText = "Shizuku belum terhubung."
            addLog("Boost dibatalkan: Binder tidak tersedia.")

            Toast.makeText(
                this,
                "Periksa koneksi Shizuku.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val scale = if (on) "0.5" else "1.0"

            val commands = arrayOf(
                "settings put global window_animation_scale $scale",
                "settings put global transition_animation_scale $scale",
                "settings put global animator_duration_scale $scale"
            )

            for (command in commands) {
                val process = Shizuku.newProcess(
                    arrayOf("sh", "-c", command),
                    null,
                    null
                )

                val exitCode = process.waitFor()
                process.destroy()

                if (exitCode != 0) {
                    throw IllegalStateException(
                        "Perintah gagal: kode $exitCode"
                    )
                }
            }

            isBoostOn = on
            statusText = if (on) {
                "Boost aktif: animasi 0.5x."
            } else {
                "Boost mati: animasi 1.0x."
            }

            addLog(statusText)

            Toast.makeText(
                this,
                statusText,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            isBoostOn = false
            statusText = "Gagal menjalankan perintah."
            addLog("Periksa izin Shizuku dan log build.")
        }
    }

    private fun addLog(message: String) {
        logText = message
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        super.onDestroy()
    }
}
