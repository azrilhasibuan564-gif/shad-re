package with.shadore.booster

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
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
    private fun requestShizukuPermission() {
    if (!Shizuku.pingBinder()) {
        statusText = "Shizuku belum terhubung."
        addLog("Jalankan Shizuku terlebih dahulu.")
        return
    }

    if (Shizuku.checkSelfPermission() ==
        PackageManager.PERMISSION_GRANTED
    ) {
        statusText = "Izin Shizuku sudah diberikan."
        addLog("Izin Shizuku tersedia.")
    } else {
        Shizuku.requestPermission(1001)
    }
    }


    private var statusText by mutableStateOf("Memeriksa Shizuku...")
    private var logText by mutableStateOf("SHADØRE siap.")
    private var isBoostOn by mutableStateOf(false)
    private var isChecking by mutableStateOf(false)

    private val binderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            runOnUiThread {
                statusText = "Shizuku terhubung."
                addLog("Koneksi Shizuku diterima.")
            }
        }

    private val binderDeadListener =
        Shizuku.OnBinderDeadListener {
            runOnUiThread {
                statusText = "Shizuku terputus."
                addLog("Koneksi Shizuku terputus.")
            }
        }
        
    private val requestPermissionResultListener =
    Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1001) {
            runOnUiThread {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    statusText = "Izin Shizuku diberikan."
                    addLog("Izin Shizuku diberikan.")
                } else {
                    statusText = "Izin Shizuku ditolak."
                    addLog("Izin Shizuku ditolak.")
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(
            requestPermissionResultListener
        )
        
        updateShizukuStatus()

        setContent {
            var licenseKey by remember { mutableStateOf("") }

            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Background utama SHADØRE
                    Image(
                        painter = painterResource(
                            id = R.drawable.shadore_bg
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Lapisan gelap agar teks terbaca
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.68f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 22.dp)
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SHADØRE",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(Modifier.height(28.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xDD080D13),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF168DCE).copy(alpha = 0.75f)
                            )
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    text = "STATUS SISTEM",
                                    color = Color(0xFF54C8FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = statusText,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = licenseKey,
                                    onValueChange = {
                                        licenseKey = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Masukkan license key")
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF35BFFF),
                                        unfocusedBorderColor =
                                            Color(0xFF526575),
                                        focusedPlaceholderColor =
                                            Color.LightGray,
                                        unfocusedPlaceholderColor =
                                            Color.Gray,
                                        cursorColor = Color(0xFF35BFFF)
                                    )
                                )

                                Spacer(Modifier.height(16.dp))

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
                                        .height(54.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF087DB5)
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            isChecking ->
                                                "MEMERIKSA LISENSI..."
                                            isBoostOn ->
                                                "MATIKAN BOOST"
                                            else ->
                                                "NYALAKAN BOOST"
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
                            color = Color(0xDD080D13),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF168DCE).copy(alpha = 0.5f)
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "BUILD LOG",
                                    color = Color(0xFF54C8FF),
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = logText,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
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
            statusText = "Shizuku belum terhubung."
            addLog("Periksa status dan izin Shizuku.")
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
            var message = "Lisensi tidak valid."

            try {
                val deviceId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown-device"

                val connection =
                    URL(SUPABASE_URL).openConnection()
                        as HttpURLConnection

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
                    SUPABASE_KEY
                )
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $SUPABASE_KEY"
                )

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
                    json.optBoolean("success", false)

                message = if (valid) {
                    "Lisensi valid."
                } else {
                    json.optString(
                        "error",
                        "Lisensi ditolak oleh server."
                    )
                }
            } catch (e: Exception) {
                message = "Gagal menghubungi server. Periksa internet."
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

    if (on && Shizuku.checkSelfPermission() !=
        PackageManager.PERMISSION_GRANTED
    ) {
        requestShizukuPermission()
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
            addLog("Periksa izin Shizuku.")
        }
    }

    private fun addLog(message: String) {
        logText = message
    }

        override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(
            requestPermissionResultListener
        )
        super.onDestroy()
    }
}
