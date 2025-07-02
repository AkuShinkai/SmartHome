package com.example.smarthome.ui.screens

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.data.UsageData
import com.example.smarthome.data.calculateTotalEnergy
import com.example.smarthome.data.fetchAllUsageData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class UsageFilter {
    DAILY, WEEKLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(navController: NavController?) {

    val BackgroundColor = Color(0xFFF3F3F3)

    var selectedFilter by remember { mutableStateOf(UsageFilter.DAILY) }

    val scope = rememberCoroutineScope()
    var usageData by remember { mutableStateOf<List<UsageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val filteredData = filterUsageData(usageData, selectedFilter)
    val totalEnergyKwh by remember {
        derivedStateOf {
            calculateTotalEnergy(filteredData) / 1000.0
        }
    }

    // Ambil data saat pertama kali komponen dimuat
    LaunchedEffect(Unit) {
        val data = fetchAllUsageData()
        Log.d("UsageScreen", "Fetched data count: ${data.size}")
        data.forEach {
            Log.d("UsageScreen", "Device: ${it.device}, Power: ${it.power}, Timestamp: ${it.timestamp.toDate()}")
        }
        usageData = data
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        UsageInfoSection(totalEnergy = totalEnergyKwh)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)) // Lekukan atas
                .background(BackgroundColor) // Warna abu-abu
                .padding(top = 16.dp, bottom = 16.dp) // Spasi agar tidak terlalu mepet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                FilterSelector(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                UsageCard(title = "Penggunaan Listrik") {
                    LineUsageChart(filteredData, selectedFilter)
                }

                Spacer(modifier = Modifier.height(16.dp))

                UsageCard(title = "Penggunaan Perangkat") {
                    BarUsageChart(filteredData)
                }
            }
        }
    }
}

@Composable
fun UsageInfoSection(totalEnergy: Double) {
    var temperature by remember { mutableStateOf("--") }
    var humidity by remember { mutableStateOf("--") }
    var smoke by remember { mutableStateOf("--") }

    // Listener realtime ke Firebase RTDB
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val sensorRef = database.getReference("smart_alarm/sensors")

        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempValue = snapshot.child("temperature").getValue(Double::class.java)
                val humidValue = snapshot.child("humidity").getValue(Int::class.java)
                val smokeValue = snapshot.child("smoke").getValue(Double::class.java)

                temperature = if (tempValue != null) "${tempValue}°C" else "--"
                humidity = if (humidValue != null) "${humidValue}%" else "--"
                smoke = if (smokeValue != null) "${smokeValue}%" else "--"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UsageInfoSection", "Failed to read temperature/humidity: ${error.message}")
            }
        })
    }

    // Tampilan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Usage",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UsageInfoItem(icon = Icons.Default.WarningAmber, value = smoke, label = "Gas")
                    UsageInfoItem(icon = Icons.Default.Thermostat, value = temperature, label = "Suhu")
                    UsageInfoItem(icon = Icons.Default.WaterDrop, value = humidity, label = "Kelembapan")
                }
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
            .background(Color(0xFFFFFFFF))
            .padding(16.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

    @Composable
    fun LineUsageChart(usageData: List<UsageData>, filter: UsageFilter) {
        val deviceList = listOf("socket_2_lubang", "smart_lamp", "smart_door")

        val labels: List<String>
        val grouped = mutableMapOf<String, MutableMap<String, MutableList<Double>>>()

        usageData.forEach { data ->
            val date = data.timestamp.toDate()
            val label: String

            if (filter == UsageFilter.DAILY) {
                val calendar = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.HOUR_OF_DAY, (get(Calendar.HOUR_OF_DAY) / 5) * 5)
                }
                label = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            } else {
                label = SimpleDateFormat("EEE", Locale("id")).format(date)
            }

            if (data.device in deviceList) {
                val deviceMap = grouped.getOrPut(label) { mutableMapOf() }
                val powerList = deviceMap.getOrPut(data.device) { mutableListOf() }
                powerList.add(data.power.toDouble())
            }
        }

        labels = if (filter == UsageFilter.DAILY) {
            listOf("00:00", "05:00", "10:00", "15:00", "20:00", "23:59")
        } else {
            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        }

        val deviceLabelMap = mapOf(
            "socket_2_lubang" to "Socket 2",
            "smart_lamp" to "Lampu",
            "smart_door" to "Pintu"
        )

        val lines = deviceList.map { device ->
            Line(
                label = deviceLabelMap[device] ?: device,
                values = labels.map { label ->
                    val values = grouped[label]?.get(device)
                    if (values != null && values.isNotEmpty()) values.average() else 0.0
                },
                color = SolidColor(
                    when (device) {
                        "socket_2_lubang" -> Color(0xFFE53935)
                        "smart_lamp" -> Color(0xFF3F51B5)
                        "smart_door" -> Color(0xFF43A047)
                        else -> Color.Gray
                    }
                ),
                firstGradientFillColor = Color.Transparent,
                secondGradientFillColor = Color.Transparent,
                drawStyle = DrawStyle.Stroke(width = 2.dp),
            )
        }

        val labelProps = LabelProperties(
            enabled = true,
            labels = labels,
            builder = { modifier, label, _, _ ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            LineChart(
                data = lines,
                modifier = Modifier.padding(horizontal = 16.dp),
                animationMode = AnimationMode.Together(delayBuilder = { it * 200L }),
                maxValue = lines.flatMap { it.values }.maxOrNull() ?: 100.0,
                labelProperties = labelProps,

            )
        }
    }

@Composable
fun BarUsageChart(usageData: List<UsageData>) {
    if (usageData.isEmpty()) {
        Text("Data belum tersedia", modifier = Modifier.padding(16.dp))
        return
    }

    // Peta nama device menjadi label lebih ramah
    val deviceLabelMap = mapOf(
        "socket_2_lubang" to "Socket 2",
        "smart_lamp" to "Lampu",
        "smart_door" to "Pintu"
    )

    // Warna tiap perangkat
    fun colorForDevice(device: String): Color {
        return when (device) {
            "socket_2_lubang" -> Color(0xFFE53935)
            "smart_lamp" -> Color(0xFF3F51B5)
            "smart_door" -> Color(0xFF43A047)
            else -> Color.Gray
        }
    }

    // Group berdasarkan perangkat
    val groupedByDevice = usageData.groupBy { it.device }

    // Transform ke List<Bars> where each device is a separate Bar
    val barsData = groupedByDevice.map { (device, entries) ->
        val label = deviceLabelMap[device] ?: device
        val total = entries.sumOf { it.power.toDouble() }
        Bars(
            label = label,
            values = listOf(
                Bars.Data(
                    label = label, // This label might not be used directly for individual bars when it's a single bar in a group
                    value = total,
                    color = SolidColor(colorForDevice(device))
                )
            )
        )
    }

    // Calculate max value for the chart
    val maxValue = barsData.maxOfOrNull { it.values.maxOf { v -> v.value } } ?: 100.0

    ColumnChart(
        modifier = Modifier
            .fillMaxWidth() // Make the chart take full available width
            .height(250.dp) // Set a fixed height
            .padding(horizontal = 16.dp), // Padding around the chart
        data = barsData,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        maxValue = maxValue,
        barProperties = BarProperties(
            thickness = 30.dp,
            spacing = 60.dp,
            style = DrawStyle.Fill
        ),
        labelProperties = LabelProperties(
            enabled = false
        )
    )
}

fun filterUsageData(data: List<UsageData>, filter: UsageFilter): List<UsageData> {
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()

    return when (filter) {
        UsageFilter.DAILY -> {
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            data.filter { it.timestamp.toDate().time >= startOfDay }
        }

        UsageFilter.WEEKLY -> {
            calendar.timeInMillis = now
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = calendar.timeInMillis
            data.filter { it.timestamp.toDate().time >= sevenDaysAgo }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSelector(
    selectedFilter: UsageFilter,
    onFilterSelected: (UsageFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        UsageFilter.DAILY to "Harian",
        UsageFilter.WEEKLY to "Mingguan"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFFFFFF))
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentLabel = when (selectedFilter) {
                UsageFilter.DAILY -> "Harian"
                UsageFilter.WEEKLY -> "Mingguan"
            }

            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentLabel,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp))
        ) {
            filterOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFilterSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
