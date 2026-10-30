package com.shadore.booster

import android.app.ActivityManager
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

private var showIntro by mutableStateOf(true)  
private val bg = Color(0xFF09090B)  
private val panel = Color(0xFF151116)  
private val neon = Color(0xFFFF3548)  
private val white = Color(0xFFF5F5F7)  
private val executor = Executors.newSingleThreadExecutor()  
private val requestCode = 1001  

private val animationKeys = listOf(  
    "window_animation_scale",  
    "transition_animation_scale",  
    "animator_duration_scale"  
)  

private val animationPrefs by lazy {  
    getSharedPreferences("shadore_animation", Context.MODE_PRIVATE)  
}  
private val historyPrefs by lazy {  
    getSharedPreferences("shadore_history", Context.MODE_PRIVATE)  
}  

private var licenseValid by mutableStateOf(false)  
private var isChecking by mutableStateOf(false)  
private var licenseKey by mutableStateOf("")  
private var statusText by mutableStateOf("Masukkan lisensi untuk melanjutkan")  
private var currentPage by mutableStateOf("Dashboard")  
private var logText by mutableStateOf("SHADØRE siap.")  
private var injectBusy by mutableStateOf(false)  
private var injectProgress by mutableIntStateOf(0)  
private val history = mutableStateListOf<String>()  

private var pointerSpeed by mutableFloatStateOf(0f)  
private var originalPointerSpeed: Int? = null  
private var mouseStatus by mutableStateOf("Pointer belum diperiksa")  
private var mouseBusy by mutableStateOf(false)  
private var crosshairStatus by mutableStateOf("Overlay belum diperiksa")  
private var systemCheckText by mutableStateOf("Pemeriksaan belum dijalankan")  
private var performanceText by mutableStateOf("Belum diukur")  
private var deviceText by mutableStateOf("Belum diperiksa")  

private var animationBusy by mutableStateOf(false)  
private var animationStatus by mutableStateOf("Belum ada perubahan")  
private var windowScale by mutableStateOf("0.5")  
private var transitionScale by mutableStateOf("0")  
private var animatorScale by mutableStateOf("0.5")  

private val permissionListener =  
    Shizuku.OnRequestPermissionResultListener { code, result ->  
        if (code == requestCode) {  
            if (result == 0) {  
                addLog("Izin Shizuku diberikan.")  
                Toast.makeText(this, "Izin Shizuku diberikan", Toast.LENGTH_SHORT).show()  
            } else {  
                addLog("Izin Shizuku ditolak.")  
            }  
        }  
    }  

private val binderReceivedListener = Shizuku.OnBinderReceivedListener {  
    addLog("Koneksi Shizuku terdeteksi.")  
}  
private val binderDeadListener = Shizuku.OnBinderDeadListener {  
    addLog("Koneksi Shizuku terputus.")  
}

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

history.addAll(  
    historyPrefs.getString("items", "").orEmpty()  
        .split("\n---\n")  
        .filter { it.isNotBlank() }  
        .take(100)  
)  

@Suppress("DEPRECATION")  
window.decorView.systemUiVisibility =  
    View.SYSTEM_UI_FLAG_FULLSCREEN or  
    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or  
    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY  

setContent {  
    MaterialTheme(  
        colorScheme = darkColorScheme(  
            primary = neon,  
            onPrimary = Color.White,  
            secondary = Color(0xFFD05A63),  
            background = bg,  
            onBackground = white,  
            surface = panel,  
            onSurface = white  
        )  
    ) {  
        Surface(  
            Modifier.fillMaxSize(),  
            color = bg  
        ) {  
            if (showIntro) {  
CinematicIntro(  
    onFinished = { showIntro = false }  
)

} else if (licenseValid) {
AppNavigation()
} else {
ActivationPage()
}
}
}
}
}
private fun hasBrightnessPermission(): Boolean {
    return Settings.System.canWrite(this)
}

private fun openBrightnessPermissionSettings() {
    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
        data = android.net.Uri.parse("package:$packageName")
    }
    startActivity(intent)
}

@Composable
private fun CinematicIntro(onFinished: () -> Unit) {
var start by remember { mutableStateOf(false) }

val logoAlpha by animateFloatAsState(  
    targetValue = if (start) 1f else 0f,  
    animationSpec = tween(1200),  
    label = "logoAlpha"  
)  

val logoScale by animateFloatAsState(  
    targetValue = if (start) 1f else 0.65f,  
    animationSpec = tween(  
        durationMillis = 1400,  
        easing = FastOutSlowInEasing  
    ),  
    label = "logoScale"  
)  

val infinite = rememberInfiniteTransition(label = "glow")  
val glow by infinite.animateFloat(  
    initialValue = 0.25f,  
    targetValue = 1f,  
    animationSpec = infiniteRepeatable(  
        animation = tween(900, easing = EaseInOutSine),  
        repeatMode = RepeatMode.Reverse  
    ),  
    label = "glowAlpha"  
)  

LaunchedEffect(Unit) {  
    start = true  
    delay(3200)  
    onFinished()  
}  

Box(  
    modifier = Modifier  
        .fillMaxSize()  
        .background(  
            Brush.verticalGradient(  
                listOf(  
                    Color.Black,  
                    Color(0xFF240008),  
                    Color(0xFF09090B)  
                )  
            )  
        ),  
    contentAlignment = Alignment.Center  
) {  
    Column(  
        horizontalAlignment = Alignment.CenterHorizontally  
    ) {  
        Box(  
            modifier = Modifier  
                .size(150.dp)  
                .background(  
                    Color(0xFF38000B).copy(alpha = glow * 0.7f),  
                    CircleShape  
                ),  
            contentAlignment = Alignment.Center  
        ) {  
            
Image(
    painter = painterResource(
        id = com.shadore.booster.R.drawable.shadore_rabbit
    ),
    contentDescription = "Logo SHADØRE",
    modifier = Modifier
        .size(150.dp)
        .alpha(logoAlpha)
        .scale(logoScale)
)
        } // penutup Box logo

        Spacer(Modifier.height(24.dp))  

        Text(  
            text = "SHADØRE",  
            color = neon.copy(alpha = logoAlpha),  
            fontSize = 34.sp,  
            fontWeight = FontWeight.Black,  
            letterSpacing = 7.sp,  
            modifier = Modifier.scale(logoScale)  
        )  

        Spacer(Modifier.height(8.dp))  

        Text(  
            text = "INITIALIZING SYSTEM",  
            color = white.copy(alpha = logoAlpha * 0.75f),  
            fontSize = 10.sp,  
            letterSpacing = 3.sp  
        )  

        Spacer(Modifier.height(28.dp))  

        LinearProgressIndicator(
    progress = injectProgress / 100f,
    modifier = Modifier.fillMaxWidth(),
    color = neon,
    trackColor = Color(0xFF493036)
    )
        Spacer(Modifier.height(12.dp))  

        Text(  
            "X AZRIL • PERFORMANCE CONTROL",  
            color = Color.LightGray.copy(alpha = logoAlpha),  
            fontSize = 9.sp,  
            letterSpacing = 1.5.sp  
        )  
    }  

    TextButton(  
        onClick = onFinished,  
        modifier = Modifier  
            .align(Alignment.BottomEnd)  
            .padding(18.dp)  
    ) {  
        Text("SKIP  ›", color = white.copy(alpha = 0.8f))  
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
            fontSize = 32.sp,  
            fontWeight = FontWeight.Black,  
            letterSpacing = 4.sp  
        )  

        Spacer(Modifier.height(8.dp))  

        Text(  
            text = "LICENSE ACTIVATION",  
            color = white,  
            fontSize = 14.sp,  
            fontWeight = FontWeight.Bold  
        )  

        Spacer(Modifier.height(28.dp))  

        OutlinedTextField(  
            value = licenseKey,  
            onValueChange = { licenseKey = it },  
            modifier = Modifier.fillMaxWidth(),  
            label = { Text("License Key") },  
            singleLine = true,  
            enabled = !isChecking,  
            colors = OutlinedTextFieldDefaults.colors(  
                focusedBorderColor = neon,  
                focusedLabelColor = neon,  
                cursorColor = neon  
            )  
        )  

        Spacer(Modifier.height(16.dp))  

        Button(
            onClick = { validateLicense(licenseKey) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChecking
        ) {
            Text(
                if (isChecking) "MEMERIKSA LISENSI..."
                else "AKTIFKAN LISENSI"
            )
        }

        if (isChecking) {  
            Spacer(Modifier.height(16.dp))  
            CircularProgressIndicator(color = neon)  
        }  

        Spacer(Modifier.height(16.dp))  

        Text(  
            text = statusText,  
            color = if (licenseValid) Color(0xFF65E890) else white,  
            fontSize = 12.sp  
        )  
    }  
}  

private fun validateLicense(key: String) {  
    if (key.isBlank()) {  
        statusText = "License key tidak boleh kosong."  
        return  
    }  
    isChecking = true  
    statusText = "Memeriksa lisensi..."  
    executor.execute {  
        var connection: HttpURLConnection? = null  
        try {  
            connection = (URL(  
                "https://elrvmholtuoxzbcdhant.supabase.co/functions/v1/clever-handler"  
            ).openConnection() as HttpURLConnection).apply {  
                requestMethod = "POST"  
                connectTimeout = 15000  
                readTimeout = 15000  
                doOutput = true  
                setRequestProperty("Content-Type", "application/json")  
                setRequestProperty("apikey", "sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B")  
                setRequestProperty(  
                    "Authorization",  
                    "Bearer sb_publishable_5fpw9OhqRmS1nV66vANP9A_npNiE71B"  
                )  
            }  
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)  
            val body = JSONObject()  
                .put("action", "validate")  
                .put("license_key", key.trim())  
                .put("device_id", deviceId)  
                .toString()  
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }  
            val code = connection.responseCode  
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream  
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()  
            val json = try { JSONObject(response) } catch (_: Exception) { JSONObject() }  
            val valid = code in 200..299 &&  
                (json.optBoolean("valid", false) || json.optBoolean("success", false))  

            runOnUiThread {  
                isChecking = false  
                licenseValid = valid  
                statusText = if (valid) "Lisensi berhasil diverifikasi." else  
                    json.optString("error", json.optString("message", "Lisensi tidak valid."))  
                addLog(if (valid) "Lisensi valid." else "Verifikasi lisensi gagal. HTTP $code")  
            }  
        } catch (e: Exception) {  
            runOnUiThread {  
                isChecking = false  
                statusText = "Gagal terhubung: ${e.message ?: "periksa koneksi"}"  
                addLog("Pemeriksaan lisensi gagal.")  
            }  
        } finally {  
            connection?.disconnect()  
        }  
    }  
}  



@Composable
private fun AppNavigation() {
    when (currentPage) {
        "Dashboard" -> DashboardPage()
        "Inject Center" -> InjectCenterPage()
        "Game" -> GamePage()
        "Crosshair" -> CrosshairPage()
        "Tols Mouse" -> MousePage()
        "Device Monitor" -> DeviceMonitorPage()
        "Build Log" -> BuildLogPage()
        "Performance Center" -> PerformancePage()
        "System Check" -> SystemCheckPage()
        "Activity History" -> HistoryPage()
        "Display" -> DisplayPage()
        "Bloat" -> BloatPage()
        else -> DashboardPage()
    }
}

   
@Composable
private fun InjectCenterPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageTitle("INJECT CENTER")

        Text(
            text = when {
                injectBusy -> "Proses sedang berjalan..."
                injectProgress >= 100 -> "Pemeriksaan selesai."
                injectProgress > 0 -> "Proses berhenti atau belum selesai."
                else -> "Siap menjalankan pemeriksaan."
            },
            color = white
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = panel
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "PROGRESS",
                    color = neon,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = injectProgress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = neon,
                    trackColor = Color(0xFF493036)
                )

                Text(
                    "$injectProgress%",
                    color = white,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                Button(
                    onClick = { runInjectAll() },
                    enabled = !injectBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (injectBusy) "SEDANG MEMPROSES..."
                        else if (injectProgress > 0) "JALANKAN ULANG"
                        else "MULAI INJECT ALL"
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = panel
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "PROCESS LOG",
                    color = neon,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = logText,
                    color = Color(0xFFD0D0D5),
                    fontSize = 12.sp
                )
            }
        }

        OutlinedButton(
            onClick = { currentPage = "Dashboard" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("KEMBALI KE DASHBOARD")
        }
    }
}

  
    @Composable  
private fun MousePage() {  
    Column(  
        modifier = Modifier  
            .fillMaxSize()  
            .background(bg)  
            .verticalScroll(rememberScrollState())  
            .padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(16.dp)  
    ) {  
        PageTitle("TOLS MOUSE")  

        Text(  
            text = "Atur kecepatan pointer perangkat.",  
            color = white  
        )  

        Card(  
            modifier = Modifier.fillMaxWidth(),  
            colors = CardDefaults.cardColors(  
                containerColor = panel  
            ),  
            shape = RoundedCornerShape(18.dp)  
        ) {  
            Column(  
                modifier = Modifier.padding(18.dp),  
                verticalArrangement = Arrangement.spacedBy(12.dp)  
            ) {  
                Text(  
                    text = "POINTER SPEED",  
                    color = neon,  
                    fontWeight = FontWeight.Bold  
                )  

                Text(  
                    text = pointerSpeed.roundToInt().toString(),  
                    color = white,  
                    fontSize = 28.sp,  
                    fontWeight = FontWeight.Black  
                )  

                Slider(  
                    value = pointerSpeed,  
                    onValueChange = { pointerSpeed = it },  
                    valueRange = -7f..7f,  
                    steps = 13,  
                    enabled = !mouseBusy  
                )  

                Row(  
                    modifier = Modifier.fillMaxWidth(),  
                    horizontalArrangement = Arrangement.SpaceBetween  
                ) {  
                    Text("-7", color = Color.LightGray)  
                    Text("0", color = Color.LightGray)  
                    Text("+7", color = Color.LightGray)  
                }  
            }  
        }  

        Text(  
            text = mouseStatus,  
            color = white,  
            fontSize = 12.sp  
        )  

        Button(  
            onClick = { readPointerSpeed() },  
            modifier = Modifier.fillMaxWidth(),  
            enabled = !mouseBusy  
        ) {  
            Text("BACA KECEPATAN SAAT INI")  
        }  

        Button(  
            onClick = { applyPointerSpeed() },  
            modifier = Modifier.fillMaxWidth(),  
            enabled = !mouseBusy  
        ) {  
            Text("TERAPKAN KECEPATAN")  
        }  

        OutlinedButton(  
            onClick = { restorePointerSpeed() },  
            modifier = Modifier.fillMaxWidth(),  
            enabled = !mouseBusy  
        ) {  
            Text("PULIHKAN KECEPATAN TERSIMPAN")  
        }  

        if (mouseBusy) {  
            LinearProgressIndicator(  
                modifier = Modifier.fillMaxWidth(),  
                color = neon  
            )  
        }  

        Text(  
            text = "Catatan: perubahan memerlukan izin Shizuku. " +  
                "Kecepatan pointer terutama berlaku pada perangkat " +  
                "yang menggunakan mouse fisik.",  
            color = Color.LightGray,  
            fontSize = 11.sp  
        )  

        BackButton()  
    }  
}  


@Composable
private fun DashboardPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SHADØRE",
                    color = neon,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Android Performance",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Text(
                "V2",
                color = neon,
                fontWeight = FontWeight.Bold
            )
        }

        // STATUS SHIZUKU
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasShizukuPermission())
                    Color(0xFF10251A) else Color(0xFF26090E)
            )
        ) {
            Text(
                text = if (hasShizukuPermission())
                    "✓ Shizuku ready"
                else
                    "! Shizuku belum siap",
                color = if (hasShizukuPermission())
                    Color(0xFF65E890) else neon,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(18.dp)
            )
        }

        
        // INFORMASI PERANGKAT - DATA ANDROID
        val context = LocalContext.current

        val ramInfo = remember {
            ActivityManager.MemoryInfo().also { info ->
                val manager = context.getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager
                manager.getMemoryInfo(info)
            }
        }

        val totalRam = ramInfo.totalMem.toDouble() /
            (1024.0 * 1024.0 * 1024.0)
        val usedRam = (ramInfo.totalMem - ramInfo.availMem)
            .toDouble() / (1024.0 * 1024.0 * 1024.0)

        val batteryIntent = remember {
            context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }

        val batteryLevel = batteryIntent?.let {
            val level = it.getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1
            )
            val scale = it.getIntExtra(
                BatteryManager.EXTRA_SCALE, -1
            )
            if (level >= 0 && scale > 0) {
                level * 100 / scale
            } else {
                -1
            }
        } ?: -1

        val batteryTemp = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE, -1
        ) ?: -1

        val storage = remember {
            StatFs(Environment.getDataDirectory().absolutePath)
        }

        val totalStorage = storage.totalBytes.toDouble() /
            (1024.0 * 1024.0 * 1024.0)
        val freeStorage = storage.availableBytes.toDouble() /
            (1024.0 * 1024.0 * 1024.0)
        val usedStorage = totalStorage - freeStorage
        val storagePercent = if (totalStorage > 0) {
            (usedStorage * 100 / totalStorage).toInt()
        } else {
            0
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardInfoCard(
                "RAM",
                String.format(Locale.US, "%.1f GB", totalRam),
                String.format(
                    Locale.US, "%.1f GB digunakan", usedRam
                ),
                Modifier.weight(1f)
            ) {
                currentPage = "Device Monitor"
            }

            DashboardInfoCard(
                "CPU",
                "${Runtime.getRuntime().availableProcessors()} Core",
                Build.HARDWARE.ifBlank { Build.SOC_MODEL },
                Modifier.weight(1f)
            ) {
                currentPage = "System Check"
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardInfoCard(
                "Battery",
                if (batteryLevel >= 0) "$batteryLevel%" else "Tidak tersedia",
                if (batteryTemp >= 0) {
                    String.format(
                        Locale.US, "%.1f°C", batteryTemp / 10.0
                    )
                } else {
                    "Suhu tidak tersedia"
                },
                Modifier.weight(1f)
            ) {
                currentPage = "Device Monitor"
            }

            DashboardInfoCard(
                "Storage",
                String.format(Locale.US, "%.0f GB", totalStorage),
                "$storagePercent% digunakan",
                Modifier.weight(1f)
            ) {
                currentPage = "Device Monitor"
            }
        }
        

        // INJECT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    currentPage = "Inject Center"
                },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = panel
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "▶",
                    color = neon,
                    fontSize = 26.sp
                )

                Spacer(Modifier.width(18.dp))

                Column {
                    Text(
                        "Inject All",
                        color = white,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Optimasi performa perangkat",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // CONTROL CENTER
        Text(
            "CONTROL CENTER",
            color = neon,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        val menuItems = listOf(
    Triple("Game", "Game optimizer", "🎮"),
    Triple("Tols Mouse", "Pointer control", "⚙"),
    Triple("Performance Center", "Performance", "⚡"),
    Triple("Device Monitor", "Device status", "📊"),
    Triple("Display", "Screen settings", "▣"),
    Triple("Bloat", "App management", "⚙"),
    Triple("System Check", "Diagnostics", "✓"),
    Triple("Activity History", "Run history", "◷")
)

        menuItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clickable {
                                currentPage = item.first
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = panel
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement =
                                Arrangement.Center
                        ) {
                            Text(
                                item.third,
                                color = neon,
                                fontSize = 23.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                item.first,
                                color = white,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                item.second,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // SYSTEM LOG
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = panel
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "SYSTEM LOG",
                    color = neon,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    logText,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            "SHADØRE V2 X AZRIL",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DashboardInfoCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(118.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = panel
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                color = Color.Gray,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = white,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                subtitle,
                color = neon,
                fontSize = 10.sp
            )
        }
    }
}


@Composable
private fun GamePage() {
    val context = LocalContext.current

    val gamePackages = listOf(
        "Free Fire" to "com.dts.freefireth",
        "Free Fire MAX" to "com.dts.freefiremax"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageTitle("GAME CENTER")

        Text(
            "Pilih game yang ingin kamu jalankan.",
            color = white
        )

        gamePackages.forEach { (gameName, packageName) ->
            val launchIntent = remember(packageName) {
                context.packageManager.getLaunchIntentForPackage(packageName)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = panel
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        gameName,
                        color = neon,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (launchIntent != null) "Status: Terpasang"
                        else "Status: Belum terpasang",
                        color = Color.LightGray
                    )

                    Button(
                        onClick = {
                            val intent = context.packageManager
                                .getLaunchIntentForPackage(packageName)

                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                addLog("$gameName tidak ditemukan")
                            }
                        },
                        enabled = launchIntent != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BUKA GAME")
                    }
                }
            }
        }

        BackButton()
    }
}

@Composable  
private fun CrosshairPage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(14.dp)  
    ) {  
        PageTitle("CROSSHAIR")  
        Text("Status: $crosshairStatus", color = white)  
        Button(  
            onClick = {  
                try {  
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))  
                } catch (e: Exception) {  
                    crosshairStatus = "Pengaturan overlay tidak dapat dibuka."  
                    addLog(crosshairStatus)  
                }  
            },  
            modifier = Modifier.fillMaxWidth()  
        ) { Text("BUKA IZIN OVERLAY") }  
        Text(  
            "Izin overlay belum menggambar crosshair. Layanan overlay khusus masih perlu dibuat.",  
            color = Color.LightGray,  
            fontSize = 12.sp  
        )  
        BackButton()  
    }  
}  


@Composable
private fun DeviceMonitorPage() {
    val context = LocalContext.current
    val monitorText = remember {
        androidx.compose.runtime.mutableStateOf("")
    }

    fun refreshMonitor() {
        try {
            // RAM
            val activityManager = context.getSystemService(
                Context.ACTIVITY_SERVICE
            ) as ActivityManager

            val memory = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memory)

            val totalRam = memory.totalMem / (1024.0 * 1024.0 * 1024.0)
            val usedRam = (memory.totalMem - memory.availMem) /
                (1024.0 * 1024.0 * 1024.0)

            // STORAGE
            val stat = StatFs(
                Environment.getDataDirectory().absolutePath
            )

            val totalStorage = stat.totalBytes /
                (1024.0 * 1024.0 * 1024.0)

            val freeStorage = stat.availableBytes /
                (1024.0 * 1024.0 * 1024.0)

            val usedStorage = totalStorage - freeStorage

            // BATTERY
            val battery = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val level = battery?.getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1
            ) ?: -1

            val scale = battery?.getIntExtra(
                BatteryManager.EXTRA_SCALE, -1
            ) ?: -1

            val batteryPercent = if (level >= 0 && scale > 0) {
                level * 100 / scale
            } else {
                -1
            }

            val temperature = battery?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE, -1
            ) ?: -1

            // CPU
            val cores = Runtime.getRuntime().availableProcessors()

            val cpuName = if (Build.VERSION.SDK_INT >= 31) {
                Build.SOC_MODEL
            } else {
                Build.HARDWARE
            }

            monitorText.value = """
                DEVICE MONITOR

                RAM
                Total: ${String.format(Locale.US, "%.2f", totalRam)} GB
                Terpakai: ${String.format(Locale.US, "%.2f", usedRam)} GB
                Tersedia: ${String.format(Locale.US, "%.2f", memory.availMem / (1024.0 * 1024.0 * 1024.0))} GB

                CPU
                Prosesor: $cpuName
                Jumlah core: $cores

                BATTERY
                Kapasitas: ${if (batteryPercent >= 0) "$batteryPercent%" else "Tidak tersedia"}
                Suhu: ${if (temperature >= 0) "${String.format(Locale.US, "%.1f", temperature / 10.0)} °C" else "Tidak tersedia"}

                STORAGE
                Total: ${String.format(Locale.US, "%.2f", totalStorage)} GB
                Terpakai: ${String.format(Locale.US, "%.2f", usedStorage)} GB
                Tersedia: ${String.format(Locale.US, "%.2f", freeStorage)} GB
            """.trimIndent()

            addLog("Device Monitor berhasil diperbarui.")
            recordHistory(
                "Device Monitor",
                "BERHASIL",
                "Data RAM, CPU, baterai, dan storage dibaca."
            )
        } catch (e: Exception) {
            monitorText.value = "Gagal membaca data perangkat: ${e.message}"
            addLog("Device Monitor gagal diperbarui.")
        }
    }

    LaunchedEffect(Unit) {
        refreshMonitor()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PageTitle("DEVICE MONITOR")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = panel
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = monitorText.value,
                color = white,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        Button(
            onClick = { refreshMonitor() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REFRESH DEVICE")
        }

        BackButton()
    }
}

@Composable  
private fun PerformancePage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).padding(20.dp),  

        verticalArrangement = Arrangement.spacedBy(14.dp)  
    ) {  
        PageTitle("PERFORMANCE CENTER")  
        Text(performanceText, color = white)  
        Button(onClick = { refreshPerformance() }, modifier = Modifier.fillMaxWidth()) {  
            Text("UKUR STATUS SAAT INI")  
        }  
        BackButton()  
    }  
}  

@Composable  
private fun SystemCheckPage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(14.dp)  
    ) {  
        PageTitle("SYSTEM CHECK")  
        Text(systemCheckText, color = white)  
        Button(onClick = { runSystemCheck() }, modifier = Modifier.fillMaxWidth()) {  
            Text("JALANKAN PEMERIKSAAN")  
        }  
        Button(onClick = { requestShizukuPermission() }, modifier = Modifier.fillMaxWidth()) {  
            Text("PERIKSA / MINTA IZIN SHIZUKU")  
        }  
        BackButton()  
    }  
}  

@Composable  
private fun BuildLogPage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(14.dp)  
    ) {  
        PageTitle("BUILD LOG")  
        Text(logText, color = white, fontSize = 12.sp)  
        Button(onClick = { logText = "Log dibersihkan."; addLog("Log dibersihkan.") }, modifier = Modifier.fillMaxWidth()) {  
            Text("BERSIHKAN LOG")  
        }  
        BackButton()  
    }  
}  

@Composable  
private fun HistoryPage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(12.dp)  
    ) {  
        PageTitle("ACTIVITY HISTORY")  
        if (history.isEmpty()) {  
            Text("Belum ada aktivitas.", color = Color.LightGray)  
        } else {  
            history.forEach { item ->  
                Card(  
                    modifier = Modifier.fillMaxWidth(),  
                    colors = CardDefaults.cardColors(containerColor = panel),  
                    shape = RoundedCornerShape(12.dp)  
                ) {  
                    Text(item, modifier = Modifier.padding(12.dp), color = white, fontSize = 12.sp)  
                }  
            }  
        }  
        OutlinedButton(  
            onClick = {  
                history.clear()  
                historyPrefs.edit().remove("items").apply()  
                addLog("Riwayat aktivitas dibersihkan.")  
            },  
            modifier = Modifier.fillMaxWidth()  
        ) { Text("HAPUS RIWAYAT") }  
        BackButton()  
    }  
}  

@Composable  
private fun PageTitle(title: String) {  
    Text(title, color = neon, fontSize = 25.sp, fontWeight = FontWeight.Black)  
}  

@Composable  
private fun BackButton() {  
    OutlinedButton(onClick = { currentPage = "Dashboard" }, modifier = Modifier.fillMaxWidth()) {  
        Text("KEMBALI KE DASHBOARD")  
    }  
}  

private fun requestShizukuPermission() {  
    try {  
        if (!Shizuku.pingBinder()) {  
            addLog("Shizuku belum aktif. Jalankan Shizuku terlebih dahulu.")  
            return  
        }  
        if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {  
            addLog("Izin Shizuku sudah tersedia.")  
        } else {  
            Shizuku.requestPermission(requestCode)  
        }  
    } catch (e: Exception) {  
        addLog("Gagal memeriksa Shizuku: ${e.message}")  
    }  
}  

private fun hasShizukuPermission(): Boolean = try {  
    Shizuku.pingBinder() &&  
        Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED  
} catch (_: Exception) {  
    false  
}  

private fun runShell(command: String): String? {  
    if (!hasShizukuPermission()) {  
        addLog("Perintah dilewati: izin Shizuku belum tersedia.")  
        return null  
    }  
    return try {  
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)  
        val output = process.inputStream.bufferedReader().use { it.readText() }  
        val error = process.errorStream.bufferedReader().use { it.readText() }  
        val exitCode = process.waitFor()  
        if (exitCode != 0) {  
            addLog("Perintah gagal ($exitCode): ${(error.ifBlank { output }).take(200)}")  
            null  
        } else output.trim()  
    } catch (e: Exception) {  
        addLog("Perintah gagal: ${e.message}")  
        null  
    }  
}  

private fun readPointerSpeed() {  
    if (mouseBusy) return  
    if (!hasShizukuPermission()) {  
        mouseStatus = "Shizuku belum aktif atau belum diizinkan."  
        addLog(mouseStatus)  
        return  
    }  
    mouseBusy = true  
    executor.execute {  
        val result = runShell("settings get system pointer_speed")  
        runOnUiThread {  
            mouseBusy = false  
            val speed = result?.trim()?.toIntOrNull()  
            if (speed != null) {  
                originalPointerSpeed = speed  
                pointerSpeed = speed.toFloat().coerceIn(-7f, 7f)  
                mouseStatus = "Pointer speed terbaca: $speed"  
                addLog(mouseStatus)  
            } else {  
                mouseStatus = "Gagal membaca pointer speed."  
                addLog(mouseStatus)  
            }  
        }  
    }  
}  

private fun applyPointerSpeed() {  
    if (mouseBusy) return  
    if (!hasShizukuPermission()) {  
        mouseStatus = "Izin Shizuku diperlukan."  
        addLog(mouseStatus)  
        return  
    }  
    val speed = pointerSpeed.roundToInt().coerceIn(-7, 7)  
    mouseBusy = true  
    executor.execute {  
        val output = runShell("settings put system pointer_speed $speed")  
        val verify = if (output != null) runShell("settings get system pointer_speed") else null  
        runOnUiThread {  
            mouseBusy = false  
            if (verify?.trim()?.toIntOrNull() == speed) {  
                mouseStatus = "Pointer speed berhasil diverifikasi: $speed"  
                addLog(mouseStatus)  
                recordHistory("Tols Mouse", "BERHASIL", mouseStatus)  
            } else {  
                mouseStatus = "Gagal menerapkan pointer speed."  
                addLog(mouseStatus)  
                recordHistory("Tols Mouse", "GAGAL", mouseStatus)  
            }  
        }  
    }  
}  


private fun restorePointerSpeed() {
    val original = originalPointerSpeed

    if (original == null) {
        mouseStatus = "Baca pointer speed terlebih dahulu."
        addLog(mouseStatus)
        return
    }

    pointerSpeed = original.toFloat().coerceIn(-7f, 7f)
    applyPointerSpeed()
}

private fun refreshDeviceInfo() {  
    deviceText = buildString {  
        appendLine("Perangkat: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")  
        appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")  
        appendLine("Shizuku terhubung: ${runCatching { Shizuku.pingBinder() }.getOrDefault(false)}")  
        append("Izin Shizuku: ${hasShizukuPermission()}")  
    }  
    addLog("Informasi perangkat diperbarui.")  
    recordHistory("Device Monitor", "SELESAI", "Informasi perangkat dibaca.")  
}  


private fun refreshPerformance() {
    val activityManager =
        getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
    val availableRamMb = memoryInfo.availMem / (1024 * 1024)
    val usedRamMb = totalRamMb - availableRamMb

    val ramPercent = if (totalRamMb > 0) {
        (usedRamMb * 100) / totalRamMb
    } else {
        0
    }

    val runtime = Runtime.getRuntime()
    val appUsedMb =
        (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val appMaxMb = runtime.maxMemory() / (1024 * 1024)

    performanceText = """
        STATUS PERFORMANCE CENTER

        RAM PERANGKAT
        Total RAM: $totalRamMb MB
        RAM Terpakai: $usedRamMb MB
        RAM Tersedia: $availableRamMb MB
        Penggunaan RAM: $ramPercent%

        MEMORI APLIKASI SHADØRE
        Heap digunakan: $appUsedMb MB
        Batas heap aplikasi: $appMaxMb MB

        Catatan:
        Data RAM merupakan perkiraan kondisi sistem.
        Data ini bukan pengukuran FPS game.
    """.trimIndent()

    addLog("Metrik RAM dan memori aplikasi diperbarui.")
    recordHistory(
        "Performance Center",
        "SELESAI",
        "Metrik RAM perangkat dan memori aplikasi dibaca."
    )
}


private fun runSystemCheck() {  
    val shizukuConnected = runCatching { Shizuku.pingBinder() }.getOrDefault(false)  
    val permission = hasShizukuPermission()  
    val overlay = Settings.canDrawOverlays(this)  
    systemCheckText = "Koneksi internet: perlu diuji saat validasi lisensi\n" +  
        "Shizuku terhubung: $shizukuConnected\n" +  
        "Izin Shizuku: $permission\n" +  
        "Izin overlay: $overlay\n" +  
        "Android API: ${android.os.Build.VERSION.SDK_INT}"  
    addLog("System Check selesai.")  
    recordHistory("System Check", "SELESAI", systemCheckText.replace("\n", "; "))  
}  

private fun runInjectAll() {  
    if (injectBusy) return  
    injectBusy = true  
    injectProgress = 0  
    addLog("=== INJECT ALL DIMULAI ===")  
    recordHistory("Inject All", "DIMULAI", "Pemeriksaan dan operasi yang didukung dimulai.")  
    executor.execute {  
        var completed = 0  
        var failures = 0  
        val total = 4  

        fun stage(label: String, action: () -> Boolean) {  
            runOnUiThread { addLog("[$label] Memulai...") }  
            val ok = try { action() } catch (_: Exception) { false }  
            if (ok) {  
                completed++  
                runOnUiThread { addLog("[$label] BERHASIL") }  
            } else {  
                failures++  
                runOnUiThread { addLog("[$label] GAGAL / TIDAK TERSEDIA") }  
            }  
            runOnUiThread { injectProgress = ((completed + failures) * 100 / total) }  
        }  

        stage("SHIZUKU") { hasShizukuPermission() }  
        stage("DEVICE CHECK") {  
            runOnUiThread { refreshDeviceInfo() }  
            true  
        }  
        stage("PERFORMANCE") {  
            runOnUiThread { refreshPerformance() }  
            true  
        }  
        stage("ANIMATION SCALE") {  
            if (!hasShizukuPermission()) false else {  
                var ok = true  
                for (key in animationKeys) {  
                    if (runShell("settings put global $key 0.5") == null) {  
                        ok = false  
                        break  
                    }  
                    val verify = runShell("settings get global $key")  
                    if (verify?.trim()?.toFloatOrNull() != 0.5f) {  
                        ok = false  
                        break  
                    }  
                }  
                ok  
            }  
        }  

        runOnUiThread {  
            injectBusy = false  
            val result = if (failures == 0) "SELESAI" else "SELESAI DENGAN $failures KEGAGALAN"  
            addLog("=== INJECT ALL $result ===")  
            recordHistory("Inject All", if (failures == 0) "BERHASIL" else "SEBAGIAN GAGAL",  
                "$completed berhasil; $failures gagal atau tidak tersedia.")  
            Toast.makeText(  
                this,  
                if (failures == 0) "Inject All selesai" else "Inject All selesai dengan kegagalan",  
                Toast.LENGTH_LONG  
            ).show()  
        }  
    }  
}  

private fun applyAnimationSettings() {
    if (animationBusy) return

    if (!hasShizukuPermission()) {
        animationStatus = "Aktifkan Shizuku dan izinkan akses terlebih dahulu."
        addLog(animationStatus)
        return
    }

    val values = listOf(windowScale, transitionScale, animatorScale)

    if (values.any { it !in listOf("0", "0.5", "1") }) {
        animationStatus = "Pilihan skala animasi tidak valid."
        addLog(animationStatus)
        return
    }

    animationBusy = true

    executor.execute {
        var success = true

        // Simpan nilai awal sebelum mengubah pengaturan.
        for (key in animationKeys) {
            if (!animationPrefs.contains(key)) {
                val original = runShell("settings get global $key")

                if (original == null) {
                    success = false
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

        // Terapkan dan verifikasi pengaturan animasi.
        if (success) {
            for (i in animationKeys.indices) {
                val key = animationKeys[i]
                val value = values[i]

                if (runShell("settings put global $key $value") == null) {
                    success = false
                    break
                }

                val verify = runShell("settings get global $key")

                if (verify?.trim()?.toFloatOrNull() != value.toFloatOrNull()) {
                    success = false
                    break
                }
            }
        }

        runOnUiThread {
            animationBusy = false

            animationStatus = if (success) {
                "Animasi diterapkan dan diverifikasi."
            } else {
                "Gagal menerapkan pengaturan. Periksa log."
            }

            addLog(animationStatus)

            recordHistory(
                "Animation Settings",
                if (success) "BERHASIL" else "GAGAL",
                animationStatus
            )
        }
    }
}

private fun restoreAnimationSettings() {  
    if (animationBusy) return  
    if (!hasShizukuPermission()) {  
        animationStatus = "Aktifkan Shizuku dan izinkan akses terlebih dahulu."  
        addLog(animationStatus)  
        return  
    }  
    if (!animationKeys.all { animationPrefs.contains(it) }) {  
        animationStatus = "Nilai awal belum tersimpan."  
        addLog(animationStatus)  
        return  
    }  
    animationBusy = true  
    executor.execute {  
        var success = true  
        for (key in animationKeys) {  
            val original = animationPrefs.getString(key, "1.0") ?: "1.0"  
            if (runShell("settings put global $key $original") == null) {  
                success = false  
                break  
            }  
            val verify = runShell("settings get global $key")  
            if (verify?.trim()?.toFloatOrNull() != original.toFloatOrNull()) {  
                success = false  
                break  
            }  
        }  
        runOnUiThread {  
            animationBusy = false  
            animationStatus = if (success) "Nilai animasi awal dipulihkan." else "Pemulihan gagal. Periksa log."  
            addLog(animationStatus)  
            recordHistory("Animation Restore", if (success) "BERHASIL" else "GAGAL", animationStatus)  
        }  
    }  
}  

private fun addLog(message: String) {  
    runOnUiThread {  
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())  
        logText = "[$time] $message\n$logText"  
        logText = logText.lines().take(60).joinToString("\n")  
    }  
}  

private fun recordHistory(feature: String, status: String, details: String) {  
    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())  
    val entry = "$time | $feature | $status\n$details"  
    runOnUiThread {  
        history.add(0, entry)  
        while (history.size > 100) history.removeAt(history.lastIndex)  
        historyPrefs.edit().putString("items", history.joinToString("\n---\n")).apply()  
    }  
}  

override fun onDestroy() {
    Shizuku.removeRequestPermissionResultListener(permissionListener)
    Shizuku.removeBinderReceivedListener(binderReceivedListener)
    Shizuku.removeBinderDeadListener(binderDeadListener)
    executor.shutdown()
    super.onDestroy()
}
            
@Composable
private fun DisplayPage() {
    val context = LocalContext.current
    var brightness by remember {
        mutableFloatStateOf(
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            ).toFloat()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PageTitle("DISPLAY CONTROL")

        Card(
            colors = CardDefaults.cardColors(containerColor = panel)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("KECERAHAN LAYAR", color = neon)

                Text(
                    "${brightness.toInt()} / 255",
                    color = white
                )

                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 1f..255f
                )

                Button(
                    onClick = {
                        if (Settings.System.canWrite(context)) {
                            val success = Settings.System.putInt(
                                context.contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS,
                                brightness.toInt()
                            )

                            if (success) {
                                addLog("Kecerahan layar berhasil diubah.")
                            }
                        } else {
                            openBrightnessPermissionSettings()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TERAPKAN KECERAHAN")
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Jika izin belum tersedia, aktifkan izin ubah pengaturan sistem.",
                    color = Color.LightGray
                )
            }
        }

        Button(
            onClick = {
                openBrightnessPermissionSettings()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("IZINKAN PENGATURAN SISTEM")
        }

        BackButton()
    }
}

@Composable
private fun BloatPage() {
    val context = LocalContext.current

    val systemApps = remember {
        context.packageManager
            .getInstalledApplications(0)
            .filter {
                (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            }
            .sortedBy {
                it.loadLabel(context.packageManager).toString()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageTitle("BLOAT MANAGEMENT")

        Text(
            "Aplikasi sistem terdeteksi: ${systemApps.size}",
            color = neon,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Pilih aplikasi untuk melihat informasi dan pengaturannya.",
            color = white
        )

        systemApps.forEach { app ->
            val appName = app.loadLabel(
                context.packageManager
            ).toString()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = panel
                )
            ) {
                Column(
                    Modifier.padding(12.dp)
                ) {
                    Text(
                        appName,
                        color = white,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        app.packageName,
                        color = Color.LightGray
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.parse(
                                        "package:${app.packageName}"
                                    )
                                )
                                context.startActivity(intent)
                                addLog("Info aplikasi dibuka: $appName")
                            } catch (e: Exception) {
                                addLog("Gagal membuka info aplikasi.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LIHAT PENGATURAN")
                    }
                }
            }
        }

        BackButton()
    }
}
}
