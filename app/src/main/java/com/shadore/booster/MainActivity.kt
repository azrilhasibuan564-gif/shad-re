package com.shadore.booster

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
private var originalPointerSpeed by mutableIntStateOf(0)  
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
            modifier = Modifier  
                .width(170.dp)  
                .alpha(logoAlpha),  
            color = neon,  
            trackColor = Color(0xFF45121C)  
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
                else "AKTIFKAN SHADØRE"  
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
        "Game" -> GamePage()  
        "Crosshair" -> CrosshairPage()  
        "Tols Mouse" -> MousePage()  
        "Device Monitor" -> DeviceMonitorPage()  
        "Build Log" -> BuildLogPage()  
        "Performance Center" -> PerformancePage()  
        "System Check" -> SystemCheckPage()  
        "Activity History" -> HistoryPage()  
        else -> DashboardPage()  
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
        modifier = Modifier.fillMaxSize().background(bg)  
            .verticalScroll(rememberScrollState()).padding(16.dp),  
        verticalArrangement = Arrangement.spacedBy(16.dp)  
    ) {  
        Column {  
            Text("SHADØRE", color = neon, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)  
            Text("X AZRIL • PERFORMANCE CONTROL", color = Color.LightGray, fontSize = 11.sp)  
            Spacer(Modifier.height(8.dp))  
            Text(  
                if (hasShizukuPermission()) "● SHIZUKU AUTHORIZED" else "● SHIZUKU BELUM SIAP",  
                color = if (hasShizukuPermission()) Color(0xFF65E890) else neon,  
                fontSize = 11.sp,  
                fontWeight = FontWeight.Bold  
            )  
        }  

        Card(  
            modifier = Modifier.fillMaxWidth(),  
            shape = RoundedCornerShape(22.dp),  
            colors = CardDefaults.cardColors(containerColor = Color(0xFF26090E))  
        ) {  
            Column(  
                modifier = Modifier.fillMaxWidth().padding(20.dp),  
                horizontalAlignment = Alignment.CenterHorizontally  
            ) {  
                Text("INJECT ALL", color = neon, fontSize = 24.sp, fontWeight = FontWeight.Black)  
                Spacer(Modifier.height(6.dp))  
                Text("Jalankan pemeriksaan dan operasi yang didukung", color = white, fontSize = 12.sp)  
                Spacer(Modifier.height(14.dp))  
                LinearProgressIndicator(  
                    progress = injectProgress / 100f,  
                    modifier = Modifier.fillMaxWidth(),  
                    color = neon,  
                    trackColor = Color(0xFF493036)  
                )  
                Spacer(Modifier.height(6.dp))  
                Text("$injectProgress%", color = white, fontWeight = FontWeight.Bold)  
                Spacer(Modifier.height(12.dp))  
                Button(  
                    onClick = { runInjectAll() },  
                    modifier = Modifier.fillMaxWidth(),  
                    enabled = !injectBusy  
                ) { Text(if (injectBusy) "SEDANG MEMPROSES..." else "MULAI INJECT ALL") }  
            }  
        }  

        Text("CONTROL CENTER", color = neon, fontSize = 13.sp, fontWeight = FontWeight.Bold)  
        val menuItems = listOf(  
            "Game" to "GAME TOOLS",  
            "Crosshair" to "AIM OVERLAY",  
            "Tols Mouse" to "POINTER CONTROL",  
            "Device Monitor" to "DEVICE STATUS",  
            "Build Log" to "BUILD OUTPUT",  
            "Performance Center" to "PERFORMANCE",  
            "System Check" to "DIAGNOSTICS",  
            "Activity History" to "RUN HISTORY"  
        )  
        menuItems.chunked(2).forEach { rowItems ->  
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {  
                rowItems.forEach { item ->  
                    Card(  
                        modifier = Modifier.weight(1f).height(112.dp)  
                             .clickable { currentPage = item.first },  
                        shape = RoundedCornerShape(18.dp),  
                        colors = CardDefaults.cardColors(containerColor = panel)  
                    ) {  
                        Column(  
                            Modifier.fillMaxSize().padding(14.dp),  
                            verticalArrangement = Arrangement.Center  
                        ) {  
                            Text(item.first, color = neon, fontSize = 15.sp, fontWeight = FontWeight.Bold)  
                            Spacer(Modifier.height(6.dp))  
                            Text(item.second, color = Color.LightGray, fontSize = 10.sp)  
                        }  
                    }  
                }  
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))  
            }  
        }  

        Card(  
            modifier = Modifier.fillMaxWidth(),  
            colors = CardDefaults.cardColors(containerColor = panel),  
            shape = RoundedCornerShape(16.dp)  
        ) {  
            Column(Modifier.padding(14.dp)) {  
                Text("SYSTEM LOG", color = neon, fontWeight = FontWeight.Bold)  
                Spacer(Modifier.height(8.dp))  
                Text(logText, color = Color(0xFFD0D0D5), fontSize = 11.sp)  
            }  
        }  
        Text("SHADØRE X AZRIL", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))  
    }  
}  

@Composable  
private fun GamePage() {  
    Column(  
        Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(12.dp)  
    ) {  
        PageTitle("GAME CENTER")  
        Text("Pilih aplikasi yang dapat diluncurkan.", color = white)  
        val apps = remember {  
            packageManager.getInstalledApplications(0)  
                .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null && it.packageName != packageName }  
                .sortedBy { packageManager.getApplicationLabel(it).toString() }  
        }  
        if (apps.isEmpty()) Text("Tidak ditemukan aplikasi yang dapat diluncurkan.", color = white)  
        apps.forEach { app ->  
            Card(  
                modifier = Modifier.fillMaxWidth().clickable {  
                    packageManager.getLaunchIntentForPackage(app.packageName)?.let {  
                        startActivity(it)  
                        addLog("Meluncurkan ${packageManager.getApplicationLabel(app)}")  
                    }  
                },  
                colors = CardDefaults.cardColors(containerColor = panel),  
                shape = RoundedCornerShape(14.dp)  
            ) {  
                Column(Modifier.padding(14.dp)) {  
                    Text(packageManager.getApplicationLabel(app).toString(), color = neon, fontWeight = FontWeight.Bold)  
                    Text(app.packageName, color = Color.LightGray, fontSize = 10.sp)  
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
    Column(  
        Modifier.fillMaxSize().background(bg).padding(20.dp),  
        verticalArrangement = Arrangement.spacedBy(14.dp)  
    ) {  
        PageTitle("DEVICE MONITOR")  
        Text(deviceText, color = white)  
        Button(onClick = { refreshDeviceInfo() }, modifier = Modifier.fillMaxWidth()) {  
            Text("PERIKSA PERANGKAT")  
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
    pointerSpeed = originalPointerSpeed.toFloat().coerceIn(-7f, 7f)  
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
    val runtime = Runtime.getRuntime()  
    val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)  
    val maxMb = runtime.maxMemory() / (1024 * 1024)  
    performanceText = "Memori aplikasi: $usedMb MB digunakan dari batas heap $maxMb MB.\n" +  
        "Ini bukan pengukuran FPS game."  
    addLog("Metrik memori aplikasi diperbarui.")  
    recordHistory("Performance Center", "SELESAI", "Metrik memori aplikasi dibaca.")  
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
        return  
    }  
    animationBusy = true  
    executor.execute {  
        var success = true  
        for (key in animationKeys) {  
            if (!animationPrefs.contains(key)) {  
                val original = runShell("settings get global $key")  
                if (original == null) {  
                    success = false  
                    break  
                }  
                val normalized = original.trim().let { if (it.isBlank() || it == "null") "1.0" else it }  
                animationPrefs.edit().putString(key, normalized).apply()  
            }  
        }  
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
            animationStatus = if (success) "Animasi diterapkan dan diverifikasi." else "Gagal menerapkan pengaturan. Periksa log."  
            addLog(animationStatus)  
            recordHistory("Animation Settings", if (success) "BERHASIL" else "GAGAL", animationStatus)  
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

}
             
