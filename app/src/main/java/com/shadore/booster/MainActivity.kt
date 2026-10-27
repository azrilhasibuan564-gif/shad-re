package with.shadore.booster

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val bg = Color(0xFF080B12)
    private val panel = Color(0xFF141A26)
    private val neon = Color(0xFF00E5FF)
    private val white = Color.White

    private var licenseValid by mutableStateOf(false)
    private var isChecking by mutableStateOf(false)
    private var statusText by mutableStateOf("Masukkan lisensi untuk melanjutkan")
    private var licenseKey by mutableStateOf("")

    private var logText by mutableStateOf("SHADØRE siap.")
    private var isBoostOn by mutableStateOf(false)
    private var boostBusy by mutableStateOf(false)

    private var pointerSpeed by mutableFloatStateOf(0f)
    private var originalPointerSpeed by mutableIntStateOf(0)
    private var mouseBusy by mutableStateOf(false)
    private var mouseStatus by mutableStateOf("Kecepatan pointer belum dibaca")

    private var animationBusy by mutableStateOf(false)
    private var animationStatus by mutableStateOf("Belum ada perubahan")

    private var windowScale by mutableStateOf("0.5")
    private var transitionScale by mutableStateOf("0")
    private var animatorScale by mutableStateOf("0.5")

    private val animationPrefs by lazy {
        getSharedPreferences("shadore_animation", Context.MODE_PRIVATE)
    }

    private val animationKeys = listOf(
        "window_animation_scale",
        "transition_animation_scale",
        "animator_duration_scale"
    )

    private val requestCode = 1001

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == requestCode) {
                if (result == 0) {
                    addLog("Izin Shizuku diberikan.")
                    Toast.makeText(
                        this,
                        "Shizuku berhasil diizinkan",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    addLog("Izin Shizuku ditolak.")
                }
            }
        }

    private val binderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            addLog("Koneksi Shizuku terdeteksi.")
        }

    private val binderDeadListener =
        Shizuku.OnBinderDeadListener {
            addLog("Koneksi Shizuku terputus.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)

        setContent {
            MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Color(0xFFB026FF),
        secondary = Color(0xFF8A2BE2),
        background = Color(0xFF08080C),
        surface = Color(0xFF12121A),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )
) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bg
                ) {
                    if (licenseValid) {
                        MainMenuPage()
                    } else {
                        ActivationPage()
                    }
                }
            }
        }
    }

    @Composable
    private fun ActivationPage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SHADØRE",
                color = neon,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "X AZRIL • GAME OPTIMIZER",
                color = white,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(30.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "AKTIVASI LISENSI",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = licenseKey,
                        onValueChange = { licenseKey = it },
                        label = { Text("Masukkan license key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { validateLicense(licenseKey) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChecking && licenseKey.isNotBlank()
                    ) {
                        Text(
                            if (isChecking) "MEMERIKSA..."
                            else "AKTIFKAN SHADØRE"
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = statusText,
                        color = white,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun MainMenuPage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "SHADØRE",
                    color = neon,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "X AZRIL • PERFORMANCE CONTROL",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            // SHIZUKU
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "SHIZUKU AUTHORIZATION",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (Shizuku.pingBinder())
                            "Status: koneksi tersedia"
                        else
                            "Status: belum terhubung",
                        color = white
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { requestShizukuPermission() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PERIKSA / MINTA IZIN")
                    }
                }
            }

            // GAME BOOSTER
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "GAME BOOSTER",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (isBoostOn) "Status: aktif" else "Status: nonaktif",
                        color = white
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { toggleGameBooster() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !boostBusy
                    ) {
                        Text(
                            if (boostBusy) "MEMPROSES..."
                            else if (isBoostOn) "MATIKAN BOOST"
                            else "AKTIFKAN BOOST"
                        )
                    }
                }
            }

            // MOUSE OTG
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "MOUSE OTG CONTROL",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(mouseStatus, color = white, fontSize = 12.sp)

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Pointer: ${pointerSpeed.roundToInt()}",
                            color = white
                        )
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { readPointerSpeed() },
                            enabled = !mouseBusy
                        ) {
                            Text("BACA")
                        }
                    }

                    Slider(
                        value = pointerSpeed,
                        onValueChange = { pointerSpeed = it },
                        valueRange = -7f..7f
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { applyPointerSpeed() },
                            modifier = Modifier.weight(1f),
                            enabled = !mouseBusy
                        ) {
                            Text("TERAPKAN")
                        }

                        OutlinedButton(
                            onClick = { restorePointerSpeed() },
                            modifier = Modifier.weight(1f),
                            enabled = !mouseBusy
                        ) {
                            Text("PULIHKAN")
                        }
                    }
                }
            }

            // ANIMATION CONTROL
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "ANIMATION CONTROL",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Atur animasi sistem Android.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    AnimationScaleSelector(
                        title = "Window Animation",
                        value = windowScale,
                        onValueChange = { windowScale = it }
                    )

                    AnimationScaleSelector(
                        title = "Transition Animation",
                        value = transitionScale,
                        onValueChange = { transitionScale = it }
                    )

                    AnimationScaleSelector(
                        title = "Animator Duration",
                        value = animatorScale,
                        onValueChange = { animatorScale = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        animationStatus,
                        color = white,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { applyAnimationSettings() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !animationBusy
                    ) {
                        Text("TERAPKAN ANIMASI")
                    }

                    OutlinedButton(
                        onClick = { restoreAnimationSettings() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !animationBusy
                    ) {
                        Text("PULIHKAN NILAI AWAL")
                    }
                }
            }

            // LOG
            Card(
                colors = CardDefaults.cardColors(containerColor = panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "SYSTEM LOG",
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = logText,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "SHADØRE X AZRIL",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 11.sp
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AnimationScaleSelector(
       title: String,
       value: String,
       onValueChange: (String) -> Unit
   ) {
        Column {
            Text(title, color = white, fontSize = 13.sp)

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("0", "0.5", "1").forEach { option ->
                    FilterChip(
                        selected = value == option,
                        onClick = { onValueChange(option) },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    
    private fun validateLicense(key: String) {
        if (key.isBlank()) {
            statusText = "License key tidak boleh kosong."
            return
        }

        isChecking = true
        statusText = "Memeriksa lisensi..."

        Thread {
            var connection: HttpURLConnection? = null

            try {
                val url = URL(
                    "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"
                )

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty(
                        "apikey",
                        "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"
                    )
                    setRequestProperty("Authorization",
                        "Bearer sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"
                    )
                }

                val deviceId = Settings.Secure.getString(
    contentResolver,
    Settings.Secure.ANDROID_ID
)

     val body = JSONObject()
    .put("action", "validate")
    .put("license_key", key.trim())
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

                val json = try {
                    JSONObject(response)
                } catch (_: Exception) {
                    JSONObject()
                }

                val valid = code in 200..299 &&
                    (
                        json.optBoolean("valid", false) ||
                        json.optBoolean("success", false)
                    )

                runOnUiThread {
                    isChecking = false
                    licenseValid = valid
                    statusText = if (valid) {
                        "Lisensi berhasil diverifikasi."
                    } else {
                        json.optString(
    "error",
    json.optString(
        "message",
        "Lisensi tidak valid. Periksa respons server."
    )
)
                    }

                    addLog(
                        if (valid) "Lisensi valid."
                        else "Verifikasi lisensi gagal. HTTP $code"
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    isChecking = false
                    statusText = "Gagal terhubung: ${e.message}"
                    addLog("Pemeriksaan lisensi gagal.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                addLog("Shizuku belum aktif. Jalankan Shizuku terlebih dahulu.")
                return
            }

            when {
                Shizuku.checkSelfPermission() ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    addLog("Izin Shizuku sudah tersedia.")
                }

                Shizuku.shouldShowRequestPermissionRationale() -> {
                    Shizuku.requestPermission(requestCode)
                }

                else -> {
                    Shizuku.requestPermission(requestCode)
                }
            }
        } catch (e: Exception) {
            addLog("Gagal memeriksa Shizuku: ${e.message}")
        }
    }

    private fun hasShizukuPermission(): Boolean {
        return try {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    private fun runShell(command: String): String? {
        if (!hasShizukuPermission()) {
            addLog("Perintah dibatalkan: izin Shizuku belum tersedia.")
            return null
        }

        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", command),
                null,
                null
            )

            val output = process.inputStream
                .bufferedReader()
                .use { it.readText() }

            val error = process.errorStream
                .bufferedReader()
                .use { it.readText() }

            val exitCode = process.waitFor()

            if (exitCode != 0) {
                addLog(
                    "Perintah gagal (kode $exitCode): " +
                        error.ifBlank { output }.take(250)
                )
                null
            } else {
                output.trim()
            }
        } catch (e: Exception) {
            addLog("Perintah gagal: ${e.message}")
            null
        }
    }

    private fun toggleGameBooster() {
        if (boostBusy) return

        boostBusy = true

        Thread {
            val target = if (isBoostOn) "1.0" else "0.5"

            val commands = listOf(
                "settings put global window_animation_scale $target",
                "settings put global transition_animation_scale $target",
                "settings put global animator_duration_scale $target"
            )

            var success = true

            for (command in commands) {
                if (runShell(command) == null) {
                    success = false
                    break
                }
            }

            runOnUiThread {
                boostBusy = false

                if (success) {
                    isBoostOn = !isBoostOn
                    addLog(
                        if (isBoostOn) {
                            "Game Booster: skala animasi diatur ke 0.5."
                        } else {
                            "Game Booster: skala animasi diatur ke 1.0."
                        }
                    )
                } else {
                    addLog("Game Booster gagal diterapkan sepenuhnya.")
                }
            }
        }.start()
    }

    private fun readPointerSpeed() {
        if (mouseBusy) return

        mouseBusy = true

        Thread {
            val result = runShell(
                "settings get system pointer_speed"
            )

            runOnUiThread {
                mouseBusy = false

                val speed = result?.trim()?.toIntOrNull()

                if (speed != null) {
                    originalPointerSpeed = speed
                    pointerSpeed = speed.toFloat()
                    mouseStatus = "Kecepatan pointer saat ini: $speed"
                    addLog("Pointer speed berhasil dibaca.")
                } else {
                    mouseStatus = "Gagal membaca pointer speed."
                    addLog("Tidak dapat membaca pointer speed.")
                }
            }
        }.start()
    }

    private fun applyPointerSpeed() {
        if (mouseBusy) return

        val speed = pointerSpeed.roundToInt().coerceIn(-7, 7)
        mouseBusy = true

        Thread {
            val result = runShell(
                "settings put system pointer_speed $speed"
            )

            val verify = if (result != null) {
                runShell("settings get system pointer_speed")
            } else {
                null
            }

            runOnUiThread {
                mouseBusy = false

                if (verify?.trim()?.toIntOrNull() == speed) {
                    mouseStatus = "Pointer speed berhasil diatur: $speed"
                    addLog("Pointer speed diterapkan dan diverifikasi.")
                } else {
                    mouseStatus = "Gagal menerapkan pointer speed."
                    addLog("Verifikasi pointer speed gagal.")
                }
            }
        }.start()
    }

    private fun restorePointerSpeed() {
        pointerSpeed = originalPointerSpeed.toFloat()
        applyPointerSpeed()
    }

    private fun applyAnimationSettings() {
        if (animationBusy) return

        if (!hasShizukuPermission()) {
            animationStatus = "Aktifkan Shizuku dan izinkan akses terlebih dahulu."
            addLog("Animation Control memerlukan izin Shizuku.")
            return
        }

        val values = listOf(
            windowScale,
            transitionScale,
            animatorScale
        )

        if (values.any { it !in listOf("0", "0.5", "1") }) {
            animationStatus = "Pilihan skala animasi tidak valid."
            return
        }

        animationBusy = true

        Thread {
            var success = true
            var savedOriginals = true

            for (key in animationKeys) {
                if (!animationPrefs.contains(key)) {
                    val original = runShell("settings get global $key")

                    if (original == null) {
                        savedOriginals = false
                        break
                    }

                    val normalized = original.trim().let {
                        if (it.isBlank() || it == "null") "1.0" else it
                    }

                    animationPrefs.edit()
                        .putString(key, normalized)
                        .apply()
                }
            }

            if (!savedOriginals) {
                success = false
            } else {
                for (i in animationKeys.indices) {
                    val key = animationKeys[i]
                    val value = values[i]

                    if (runShell(
                            "settings put global $key $value"
                        ) == null
                    ) {
                        success = false
                        break
                    }

                    val verify = runShell(
                        "settings get global $key"
                    )

                    if (verify?.trim()?.toFloatOrNull() !=
                        value.toFloatOrNull()
                    ) {
                        success = false
                        break
                    }
                }
            }

            runOnUiThread {
                animationBusy = false

                animationStatus = if (success) {
                    "Animasi berhasil diterapkan dan diverifikasi."
                } else {
                    "Gagal menerapkan semua pengaturan. Periksa log."
                }

                addLog(animationStatus)
            }
        }.start()
    }

    private fun restoreAnimationSettings() {
        if (animationBusy) return

        if (!hasShizukuPermission()) {
            animationStatus = "Aktifkan Shizuku dan izinkan akses terlebih dahulu."
            return
        }

        if (!animationKeys.all { animationPrefs.contains(it) }) {
            animationStatus = "Nilai awal belum tersimpan. Terapkan pengaturan terlebih dahulu."
            addLog("Pemulihan dibatalkan: nilai awal tidak lengkap.")
            return
        }

        animationBusy = true

        Thread {
            var success = true

            for (key in animationKeys) {
                val original = animationPrefs.getString(key, "1.0")
                    ?: "1.0"

                if (runShell(
                        "settings put global $key $original"
                    ) == null
                ) {
                    success = false
                    break
                }

                val verify = runShell(
                    "settings get global $key"
                )

                if (verify?.trim()?.toFloatOrNull() !=
                    original.toFloatOrNull()
                ) {
                    success = false
                    break
                }
            }

            runOnUiThread {
                animationBusy = false

                animationStatus = if (success) {
                    "Nilai animasi awal berhasil dipulihkan."
                } else {
                    "Pemulihan gagal atau belum lengkap. Periksa log."
                }

                addLog(animationStatus)
            }
        }.start()
    }

    private fun addLog(message: String) {
        runOnUiThread {
            logText = "$message\n$logText"
            logText = logText.lines().take(30).joinToString("\n")
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        super.onDestroy()
    }
}
