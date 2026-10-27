package with.shadore.booster

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.content.pm.PackageManager
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
import kotlin.math.roundToInt

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

    private var statusText by mutableStateOf("Memeriksa Shizuku...")
    private var logText by mutableStateOf("SHADØRE siap.")
    private var isBoostOn by mutableStateOf(false)
    private var isChecking by mutableStateOf(false)
    private var boostBusy by mutableStateOf(false)

    private var pointerSpeed by mutableIntStateOf(0)
    private var originalPointerSpeed: Int? = null
    private var mouseBusy by mutableStateOf(false)
    private var mouseStatus by mutableStateOf("Tekan BACA untuk membaca nilai sistem.")

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
                isBoostOn = false
                addLog("Koneksi Shizuku terputus.")
            }
        }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_REQUEST_CODE) {
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
                            .background(Color.Black.copy(alpha = 0.82f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "SHADØRE",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            "X AZRIL  •  SYSTEM OPTIMIZER",
                            color = Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(22.dp))

                        // STATUS DAN LISENSI
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Panel,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.7f))
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    "STATUS SISTEM",
                                    color = Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    statusText,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = licenseKey,
                                    onValueChange = { licenseKey = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Masukkan license key")
                                    },
                                    singleLine = true,
                                    enabled = !isChecking && !isBoostOn,
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
                                    onClick = {
                                        when {
                                            isChecking || boostBusy -> Unit

                                            isBoostOn -> toggleBoost(false)

                                            licenseKey.isBlank() -> {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Masukkan license key dahulu.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            else -> validateLicense(
                                                licenseKey.trim()
                                            ) { valid ->
                                                if (valid) toggleBoost(true)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Red
                                    ),
                                    enabled = !isChecking && !boostBusy
                                ) {
                                    Text(
                                        when {
                                            isChecking -> "MEMERIKSA LISENSI..."
                                            boostBusy -> "MEMPROSES..."
                                            isBoostOn -> "MATIKAN BOOST"
                                            else -> "NYALAKAN BOOST"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { requestShizukuPermission() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("PERIKSA / MINTA IZIN SHIZUKU")
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // MOUSE OTG
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Panel,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.7f))
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    "MOUSE OTG",
                                    color = Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Kecepatan pointer sistem",
                                    color = Color.White
                                )

                                Slider(
                                    value = pointerSpeed.toFloat(),
                                    onValueChange = {
                                        pointerSpeed = it.roundToInt()
                                    },
                                    valueRange = -7f..7f,
                                    steps = 13,
                                    enabled = !mouseBusy
                                )

                                Text(
                                    "Nilai: $pointerSpeed  (rentang -7 hingga 7)",
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
                                        onClick = { loadPointerSpeed() },
                                        enabled = !mouseBusy,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("BACA")
                                    }

                                    Button(
                                        onClick = { savePointerSpeed() },
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
                                    onClick = { restorePointerSpeed() },
                                    enabled = !mouseBusy,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("KEMBALIKAN NILAI AWAL")
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Catatan: pengaturan ini mengubah kecepatan pointer sistem, " +
                                        "bukan sensitivitas aim di dalam game. Dukungan bergantung " +
                                        "pada versi Android dan perangkat.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // BUILD LOG
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

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "SHADØRE X AZRIL",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
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
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        }
    }

    private fun updateShizukuStatus() {
        if (Shizuku.pingBinder()) {
            statusText = if (
                Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED
            ) {
                "Shizuku terhubung dan izin tersedia."
            } else {
                "Shizuku terhubung, izin belum diberikan."
            }
            addLog(statusText)
        } else {
            statusText = "Shizuku belum terhubung."
            addLog("Periksa status Shizuku.")
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
        
