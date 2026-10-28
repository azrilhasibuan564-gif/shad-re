
package com.shadore.booster

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

private val bg = Color(0xFF10090C)
private val panel = Color(0xFF26090E)
private val neon = Color(0xFFFF365B)
private val white = Color(0xFFF5F0F2)
private val green = Color(0xFF65E890)

class MainActivity : ComponentActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val logItems = mutableStateListOf<String>()
    private var injectProgress by mutableIntStateOf(0)
    private var injectBusy by mutableStateOf(false)
    private var injectStatus by mutableStateOf("BELUM DIMULAI")
    private var currentPage by mutableStateOf("Dashboard")

    private val animationKeys = listOf(
        "window_animation_scale",
        "transition_animation_scale",
        "animator_duration_scale"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = bg,
                    surface = panel,
                    primary = neon
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bg
                ) {
                    when (currentPage) {
                        "Dashboard" -> DashboardPage()
                        "Inject All" -> InjectAllPage()
                        else -> DashboardPage()
                    }
                }
            }
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
        if (!hasShizukuPermission()) return null

        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", command),
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

            if (exitCode == 0) output.ifBlank { error } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun addLog(message: String) {
        mainHandler.post {
            val time = java.text.SimpleDateFormat(
                "HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            logItems.add("[$time] $message")

            while (logItems.size > 300) {
                logItems.removeAt(0)
            }
        }
    }

    private fun runInjectAll() {
        if (injectBusy) return

        injectBusy = true
        injectProgress = 0
        injectStatus = "SEDANG BERJALAN"
        logItems.clear()

        addLog("=== SHADØRE INJECT ALL DIMULAI ===")

        executor.execute {
            var completed = 0
            var failures = 0

            fun stage(label: String, action: () -> Boolean) {
                addLog("[$label] Memulai...")

                val success = try {
                    action()
                } catch (e: Exception) {
                    addLog("[$label] Error: ${e.message ?: "tidak diketahui"}")
                    false
                }

                if (success) {
                    completed++
                    addLog("[$label] BERHASIL")
                } else {
                    failures++
                    addLog("[$label] GAGAL / TIDAK TERSEDIA")
                }

                mainHandler.post {
                    injectProgress = ((completed + failures) * 100 / 4)
                }
            }

            stage("SHIZUKU") {
                hasShizukuPermission()
            }

            stage("DEVICE CHECK") {
                val model = android.os.Build.MODEL
                val androidVersion = android.os.Build.VERSION.RELEASE

                addLog("Perangkat: $model")
                addLog("Android: $androidVersion")
                true
            }

            stage("PERFORMANCE CHECK") {
                val runtime = Runtime.getRuntime()
                addLog("Jumlah CPU: ${runtime.availableProcessors()}")
                addLog(
                    "Heap tersedia: " +
                        "${runtime.freeMemory() / (1024 * 1024)} MB"
                )
                true
            }

            stage("ANIMATION SCALE") {
                if (!hasShizukuPermission()) {
                    addLog("Izin Shizuku diperlukan untuk tahap ini.")
                    false
                } else {
                    var success = true

                    for (key in animationKeys) {
                        val result = runShell(
                            "settings put global $key 0.5"
                        )

                        if (result == null) {
                            success = false
                            addLog("Gagal mengubah $key")
                            break
                        }

                        val verify = runShell(
                            "settings get global $key"
                        )?.trim()?.toFloatOrNull()

                        if (verify != 0.5f) {
                            success = false
                            addLog("Verifikasi $key tidak cocok.")
                            break
                        }
                    }

                    success
                }
            }

            addLog("=== PROSES SELESAI ===")
            addLog("Berhasil: $completed | Gagal: $failures")

            mainHandler.post {
                injectProgress = 100
                injectStatus = if (failures == 0) {
                    "SELESAI"
                } else {
                    "SELESAI DENGAN KENDALA"
                }
                injectBusy = false
            }
        }
    }

    @Composable
    private fun DashboardPage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "SHADØRE",
                color = neon,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "X AZRIL • PERFORMANCE CONTROL",
                color = Color.LightGray,
                fontSize = 11.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = panel
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "INJECT ALL",
                        color = neon,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Pemeriksaan dan optimasi yang didukung",
                        color = white
                    )

                    Spacer(Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { injectProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = neon
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "$injectProgress% • $injectStatus",
                        color = white
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { currentPage = "Inject All" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BUKA INJECT ALL")
                    }
                }
            }

            Text(
                if (hasShizukuPermission()) {
                    "● SHIZUKU AUTHORIZED"
                } else {
                    "● SHIZUKU BELUM SIAP"
                },
                color = if (hasShizukuPermission()) green else neon,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun InjectAllPage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = { currentPage = "Dashboard" }
            ) {
                Text("← KEMBALI", color = neon)
            }

            Text(
                "INJECT ALL",
                color = neon,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                injectStatus,
                color = white,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { injectProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = neon
            )

            Text(
                "$injectProgress%",
                color = white,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { runInjectAll() },
                enabled = !injectBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (injectBusy) "SEDANG MEMPROSES..."
                    else "MULAI / ULANGI PROSES"
                )
            }

            Text(
                "LOG AKTIVITAS",
                color = neon,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF180D11)
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (logItems.isEmpty()) {
                        item {
                            Text(
                                "Belum ada log. Tekan MULAI untuk memulai.",
                                color = Color.LightGray
                            )
                        }
                    }

                    items(logItems) { line ->
                        Text(
                            text = line,
                            color = when {
                                "BERHASIL" in line -> green
                                "GAGAL" in line || "Error:" in line ->
                                    neon
                                else -> white
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
