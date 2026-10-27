
package with.shadore.booster

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.net.HttpURLConnection
import java.net.URL

private val Red = Color(0xFFD94444)
private val Panel = Color(0xF0101010)

class MainActivity : ComponentActivity() {

    companion object {
        private const val SUPABASE_URL =
            "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"

        private const val SUPABASE_KEY =
            "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"

        private const val SHIZUKU_REQUEST_CODE = 1001
    }

    private var licenseValid by mutableStateOf(false)
    private var isChecking by mutableStateOf(false)
    private var statusText by mutableStateOf("Masukkan license key.")
    private var logText by mutableStateOf("SHADØRE siap.")
    private var isBoostOn by mutableStateOf(false)
    private var boostBusy by mutableStateOf(false)

    private var pointerSpeed by mutableIntStateOf(0)
    private var originalPointerSpeed: Int? = null
    private var mouseBusy by mutableStateOf(false)
    private var mouseStatus by mutableStateOf(
        "Tekan BACA untuk membaca nilai sistem."
    )

    private val binderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            runOnUiThread {
                statusText = "Shizuku terhubung."
                addLog(statusText)
            }
        }

    private val binderDeadListener =
        Shizuku.OnBinderDeadListener {
            runOnUiThread {
                statusText = "Shizuku terputus."
                isBoostOn = false
                addLog(statusText)
            }
        }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                runOnUiThread {
                    statusText =
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            "Izin Shizuku diberikan."
                        } else {
                            "Izin Shizuku ditolak."
                        }
                    addLog(statusText)
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

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Red,
                    secondary = Red,
                    background = Color.Black,
                    surface = Panel
                )
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(R.drawable.shadore_bg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.86f))
                    )

                    if (!licenseValid) {
                        ActivationPage(
                            licenseKey = licenseKey,
                            onKeyChange = { licenseKey = it },
                            statusText = statusText,
                            isChecking = isChecking,
                            onActivate = {
                                if (licenseKey.isBlank()) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Masukkan license key dahulu.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    validateLicense(licenseKey.trim()) { valid ->
                                        if (valid) {
                                            licenseValid = true
                                            addLog("Aktivasi berhasil.")
                                            updateShizukuStatus()
                                        }
                                    }
                                }
                            }
                        )
                    } else {
                        MainMenuPage(
                            statusText = statusText,
                            logText = logText,
                            isBoostOn = isBoostOn,
                            boostBusy = boostBusy,
                            pointerSpeed = pointerSpeed,
                            mouseBusy = mouseBusy,
                            mouseStatus = mouseStatus,
                            onToggleBoost = {
                                toggleBoost(!isBoostOn)
                            },
                            onRequestShizuku = {
                                requestShizukuPermission()
                            },
                            onReadPointer = { loadPointerSpeed() },
                            onApplyPointer = { savePointerSpeed() },
                            onRestorePointer = { restorePointerSpeed() },
                            onLogout = {
                                if (!isBoostOn && !boostBusy && !mouseBusy) {
                                    licenseValid = false
                                    statusText = "Masukkan license key."
                                    addLog("Sesi lisensi ditutup.")
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Matikan boost dan selesaikan proses dahulu.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ActivationPage(
        licenseKey: String,
        onKeyChange: (String) -> Unit,
        statusText: String,
        isChecking: Boolean,
        onActivate: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "SHADØRE",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "X AZRIL • SYSTEM OPTIMIZER",
                color = Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(30.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Red)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "AKTIVASI LISENSI",
                        color = Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = licenseKey,
                        onValueChange = onKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan license key") },
                        singleLine = true,
                        enabled = !isChecking,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Red,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Red
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = onActivate,
                        enabled = !isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Text(
                            if (isChecking) "MEMERIKSA LISENSI..."
                            else "AKTIFKAN"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        statusText,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    
    @Composable
    private fun MainMenuPage(
        statusText: String,
        logText: String,
        isBoostOn: Boolean,
        boostBusy: Boolean,
        pointerSpeed: Int,
        mouseBusy: Boolean,
        mouseStatus: String,
        onToggleBoost: () -> Unit,
        onRequestShizuku: () -> Unit,
        onReadPointer: () -> Unit,
        onApplyPointer: () -> Unit,
        onRestorePointer: () -> Unit,
        onLogout: () -> Unit
    ) {
        var selectedSpeed by remember(pointerSpeed) {
            mutableFloatStateOf(pointerSpeed.toFloat())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SHADØRE",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "X AZRIL • SYSTEM OPTIMIZER",
                color = Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Red)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "STATUS SISTEM",
                        color = Red,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(statusText, color = Color.White)

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = onToggleBoost,
                        enabled = !boostBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Text(
                            when {
                                boostBusy -> "MEMPROSES..."
                                isBoostOn -> "MATIKAN BOOST"
                                else -> "NYALAKAN BOOST"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRequestShizuku,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PERIKSA / MINTA IZIN SHIZUKU")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Red)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "MOUSE OTG",
                        color = Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Kecepatan pointer sistem",
                        color = Color.White
                    )

                    Slider(
                        value = selectedSpeed.coerceIn(-7f, 7f),
                        onValueChange = { selectedSpeed = it },
                        valueRange = -7f..7f,
                        steps = 13,
                        enabled = !mouseBusy
                    )

                    Text(
                        "Nilai pilihan: ${selectedSpeed.roundToInt()}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        mouseStatus,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReadPointer,
                            enabled = !mouseBusy,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BACA")
                        }

                        Button(
                            onClick = {
                                selectedSpeed.roundToInt()
                                    .coerceIn(-7, 7)
                                    .let { selected ->
                                        onApplyPointerWithValue(
                                            selected,
                                            onApplyPointer
                                        )
                                    }
                            },
                            enabled = !mouseBusy,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red
                            )
                        ) {
                            Text("TERAPKAN")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRestorePointer,
                        enabled = !mouseBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("KEMBALIKAN NILAI AWAL")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Pengaturan ini mengubah kecepatan pointer sistem, " +
                            "bukan sensitivitas aim di dalam game.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "BUILD LOG",
                        color = Red,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        logText,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("KELUAR / GANTI KEY")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "SHADØRE X AZRIL",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }

    
    private fun validateLicense(
        key: String,
        callback: (Boolean) -> Unit
    ) {
        if (isChecking) return

        isChecking = true
        statusText = "Memvalidasi lisensi..."
        addLog("Memulai validasi lisensi.")

        Thread {
            var valid = false
            var message = "Lisensi tidak valid."
            var connection: HttpURLConnection? = null

            try {
                val deviceId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown-device"

                connection = URL(SUPABASE_URL)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.setRequestProperty("apikey", SUPABASE_KEY)
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
            } finally {
                connection?.disconnect()
            }

            runOnUiThread {
                isChecking = false
                statusText = message
                addLog(message)

                if (!valid) {
                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                callback(valid)
            }
        }.start()
    }

    private fun requestShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            statusText = "Shizuku belum terhubung."
            addLog(statusText)
            return
        }

        if (Shizuku.checkSelfPermission() ==
            PackageManager.PERMISSION_GRANTED
        ) {
            statusText = "Izin Shizuku sudah diberikan."
            addLog(statusText)
        } else {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        }
    }

    private fun updateShizukuStatus() {
        statusText = if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED
            ) {
                "Shizuku terhubung dan izin tersedia."
            } else {
                "Shizuku terhubung, izin belum diberikan."
            }
        } else {
            "Shizuku belum terhubung."
        }

        addLog(statusText)
    }

    private fun runShell(command: String): String? {
        if (!Shizuku.pingBinder()) {
            statusText = "Shizuku belum terhubung."
            return null
        }

        if (Shizuku.checkSelfPermission() !=
            PackageManager.PERMISSION_GRANTED
        ) {
            statusText = "Izin Shizuku belum diberikan."
            return null
        }

        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", command),
                null,
                null
            )

            val output = process.inputStream.bufferedReader().use {
                it.readText()
            }

            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            addLog("Perintah gagal: ${e.message}")
            null
        }
    }

    private fun toggleBoost(enable: Boolean) {
        if (boostBusy) return

        boostBusy = true
        addLog(
            if (enable) "Mengaktifkan Game Booster..."
            else "Mematikan Game Booster..."
        )

        Thread {
            val command = if (enable) {
                "settings put global window_animation_scale 0.5; " +
                    "settings put global transition_animation_scale 0.5; " +
                    "settings put global animator_duration_scale 0.5"
            } else {
                "settings put global window_animation_scale 1.0; " +
                    "settings put global transition_animation_scale 1.0; " +
                    "settings put global animator_duration_scale 1.0"
            }

            val result = runShell(command)

            runOnUiThread {
                boostBusy = false

                if (result != null) {
                    isBoostOn = enable
                    statusText = if (enable) {
                        "Game Booster aktif."
                    } else {
                        "Game Booster dimatikan."
                    }
                    addLog(statusText)
                } else {
                    statusText = "Gagal menjalankan perintah. Periksa Shizuku."
                    addLog(statusText)
                }
            }
        }.start()
    }

    private fun loadPointerSpeed() {
        if (mouseBusy) return
        mouseBusy = true

        Thread {
            val result = runShell(
                "settings get system pointer_speed"
            )

            runOnUiThread {
                mouseBusy = false
                val value = result?.toIntOrNull()

                if (value != null) {
                    pointerSpeed = value.coerceIn(-7, 7)

                    if (originalPointerSpeed == null) {
                        originalPointerSpeed = value
                    }

                    mouseStatus = "Nilai pointer berhasil dibaca."
                    addLog("Pointer speed dibaca: $value")
                } else {
                    mouseStatus = "Gagal membaca pointer. Periksa Shizuku."
                    addLog(mouseStatus)
                }
            }
        }.start()
    }

    private fun savePointerSpeed() {
        applyPointerSpeed(pointerSpeed)
    }

    private fun applyPointerSpeed(value: Int) {
        if (mouseBusy) return
        mouseBusy = true

        val safeValue = value.coerceIn(-7, 7)

        Thread {
            val result = runShell(
                "settings put system pointer_speed $safeValue"
            )

            runOnUiThread {
                mouseBusy = false

                if (result != null) {
                    pointerSpeed = safeValue
                    mouseStatus = "Nilai pointer diterapkan: $safeValue"
                    addLog(mouseStatus)
                } else {
                    mouseStatus = "Gagal menerapkan pointer speed."
                    addLog(mouseStatus)
                }
            }
        }.start()
    }

    private fun restorePointerSpeed() {
        val original = originalPointerSpeed

        if (original == null) {
            mouseStatus = "Tekan BACA dahulu untuk menyimpan nilai awal."
            addLog(mouseStatus)
            return
        }

        applyPointerSpeed(original)
    }

    private fun addLog(message: String) {
        logText = "$message\n$logText"

        if (logText.length > 3000) {
            logText = logText.take(3000)
        }
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

