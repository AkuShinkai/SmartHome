package com.example.smarthome.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.data.UsageData
import com.example.smarthome.data.fetchUsageData
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UsageScreen(navController: NavController?) {
    val scope = rememberCoroutineScope()
    var usageData by remember { mutableStateOf<List<UsageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Ambil data saat pertama kali komponen dimuat
    LaunchedEffect(Unit) {
        usageData = fetchUsageData("switch_1") // Ganti dengan switch yang tersedia di Firestore
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column {
                UsageInfoSection()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Text(
                text = "Memuat data...",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        } else if (usageData.isEmpty()) {
            Text(
                text = "Tidak ada data untuk ditampilkan",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        } else {
            UsageCard(title = "Penggunaan Listrik") {
                LineUsageChart(usageData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            UsageCard(title = "Penggunaan Perangkat") {
                BarUsageChart(usageData)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UsageInfoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Tinggi header tetap
            .background(Color(0xFFBDBDBD))
            .padding(horizontal = 20.dp, vertical = 16.dp) // Padding lebih besar
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top // Menjadikan konten mulai dari atas
        ) {
            Text(
                text = "Usage",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp, // Ukuran teks tetap besar
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Start) // Geser ke kiri
                    .padding(top = 8.dp) // Tambahkan padding agar lebih ke atas
            )

            Spacer(modifier = Modifier.height(8.dp)) // Jarak lebih kecil agar tidak turun jauh

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, // Elemen lebih seimbang
                verticalAlignment = Alignment.CenterVertically
            ) {
                UsageInfoItem(icon = Icons.Default.Bolt, value = "170 KwH", label = "Listrik")
                UsageInfoItem(icon = Icons.Default.Thermostat, value = "40°C", label = "Suhu")
                UsageInfoItem(icon = Icons.Default.WaterDrop, value = "60%", label = "Kelembapan")
            }
        }
    }
}


@Composable
fun UsageInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(28.dp) // Perbesar ikon
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp) // Ukuran teks lebih besar
        Text(text = label, fontSize = 14.sp, color = Color.Gray) // Ukuran label diperbesar
    }
}

@Composable
fun UsageCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD0D0D0))
            .padding(16.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}


@Composable
fun LineUsageChart(usageData: List<UsageData>) {
    if (usageData.isEmpty()) {
        Text(
            text = "Data power belum tersedia",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
        return
    }

    val powerValues = usageData.map { it.power }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        LineChart(
            modifier = Modifier.padding(horizontal = 16.dp),
            data = remember {
                listOf(
                    Line(
                        label = "Power (W)",
                        values = powerValues,
                        color = SolidColor(Color(0xFF3F51B5)),
                        firstGradientFillColor = Color(0xFF3F51B5).copy(alpha = .3f),
                        secondGradientFillColor = Color.Transparent,
                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                        gradientAnimationDelay = 1000,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                    )
                )
            },
            animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
            maxValue = powerValues.maxOrNull() ?: 200.0,
        )
    }
}

@Composable
fun BarUsageChart(usageData: List<UsageData>) {
    if (usageData.isEmpty()) {
        Text(
            text = "Data perangkat belum tersedia",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
        return
    }

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Format waktu
    val barsData = usageData.map {
        Bars(
            label = dateFormat.format(it.timestamp.toDate()), // Format waktu untuk sumbu X
            values = listOf(
                Bars.Data(label = "Power", value = it.power, color = SolidColor(Color(0xFFE53935))),
                Bars.Data(label = "Energy", value = it.energy, color = SolidColor(Color(0xFF3F51B5))),
                Bars.Data(label = "Current", value = it.current, color = SolidColor(Color(0xFF43A047)))
            )
        )
    }

    ColumnChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp),
        data = barsData,
        barProperties = BarProperties(spacing = 5.dp),
        maxValue = barsData.maxOfOrNull { it.values.maxOf { v -> v.value } } ?: 100.0
    )
}

@Preview(showBackground = true)
@Composable
fun UsageScreenPreview() {
    UsageScreen(navController = null)
}
