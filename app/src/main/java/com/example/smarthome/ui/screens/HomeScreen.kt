package com.example.smarthome.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smarthome.R
import com.example.smarthome.api.WeatherRepository
import com.example.smarthome.data.WeatherResponse
import com.example.smarthome.security.ApiKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun HomeScreen(navController: NavController?) {

    val PrimaryColor = Color(0xFF2AABD5)
    val SecondaryColor = Color(0xFF54BCDE)
    val BackgroundColor = Color(0xFFF3F3F3)
    val ButtonColor = Color(0xFF1A91C1)
    val TextColor = Color(0xFF005A80)

    var selectedTab by remember { mutableStateOf("All Devices") }
    var showEditDialog by remember { mutableStateOf(false) }

    var showHistoryDialog by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userName by remember { mutableStateOf<String?>(null) }

    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    val repository = WeatherRepository()

    // 🔹 Data Perangkat & Ruangan
    var devices by remember {
        mutableStateOf(
            listOf(
                Device("Smart Lamp", Icons.Default.Lightbulb, "Kamar Tidur"),
                Device("Smart Socket", Icons.Default.Power, "Ruang Tamu")
            )
        )
    }

    LaunchedEffect(key1 = true) {
        weather = repository.getWeather("Madiun", ApiKey.WEATHER_API_KEY)
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "Guest"
                }
                .addOnFailureListener {
                    userName = "Guest"
                }
        } else {
            userName = "Guest"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showHistoryDialog = true },
                modifier = Modifier.padding(top = 5.dp)) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                )
            }
        }

        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = { Text("Riwayat Penggunaan") },
                text = { Text("Ini adalah konten riwayat penggunaan...") },
                confirmButton = {
                    TextButton(onClick = { showHistoryDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
        }

        if (userName == null) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Hello, $userName! 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 WeatherCard
        WeatherCard(
            temperature = weather?.main?.temp?.toInt() ?: 0,
            condition = weather?.weather?.firstOrNull()?.main ?: "Unknown",
            location = weather?.name ?: "Unknown",
            feelsLike = weather?.main?.feels_like?.toInt() ?: 0,
            humidity = weather?.main?.humidity ?: 0,
            windSpeed = weather?.wind?.speed?.toInt() ?: 0,
            airQualityIndex = getAirQualityIndex(weather)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Filter Tab
        val tabs = listOf("All Devices", "Kamar Tidur", "Ruang Tamu")
        Row {
            tabs.forEach { tab ->
                Text(
                    text = tab,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { selectedTab = tab },
                    color = if (selectedTab == tab) Color.Black else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Filter & Tampilkan Perangkat Berdasarkan Ruangan
        val filteredDevices = if (selectedTab == "All Devices") devices else devices.filter { it.room == selectedTab }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            filteredDevices.forEach { device ->
                DeviceCard(device.name, device.icon)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 🔹 Tombol Edit untuk Mengubah Ruangan Perangkat
        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(Color(0xFFD6D6D6))
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Edit", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Bagian untuk Chart
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DeviceUsageChart(navController) { route ->
                navController?.navigate(route)
            }
        }

        // 🔹 Dialog Edit Ruangan
        if (showEditDialog) {
            EditDeviceDialog(
                devices = devices,
                onSave = { updatedDevices ->
                    devices = updatedDevices
                    showEditDialog = false
                },
                onCancel = { showEditDialog = false } // Tambahkan ini agar dialog bisa ditutup tanpa menyimpan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 🔹 Model Data Perangkat
data class Device(val name: String, val icon: ImageVector, var room: String)

// 🔹 Dialog Edit Perangkat
@Composable
fun EditDeviceDialog(
    devices: List<Device>,
    onSave: (List<Device>) -> Unit,
    onCancel: () -> Unit // Tambahkan aksi untuk cancel
) {
    var updatedDevices by remember { mutableStateOf(devices) }
    val rooms = listOf("Kamar Tidur", "Ruang Tamu")

    Dialog(onDismissRequest = { onCancel() }) { // Gunakan onCancel saat dialog ditutup
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Device Rooms",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                updatedDevices.forEachIndexed { index, device ->
                    var selectedRoom by remember { mutableStateOf(device.room) }

                    Text(
                        text = device.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )

                    rooms.forEach { room ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRoom = room
                                    updatedDevices = updatedDevices.toMutableList().apply {
                                        this[index] = device.copy(room = selectedRoom)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .background(
                                    if (selectedRoom == room) Color(0xFFDDEEFF)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            RadioButton(
                                selected = selectedRoom == room,
                                onClick = {
                                    selectedRoom = room
                                    updatedDevices = updatedDevices.toMutableList().apply {
                                        this[index] = device.copy(room = selectedRoom)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (room == "Kamar Tidur") Icons.Default.Bed else Icons.Default.Weekend,
                                contentDescription = room,
                                tint = if (selectedRoom == room) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(room, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Cancel & Save
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Memberi jarak yang rapi antar tombol
                ) {
                    OutlinedButton(
                        onClick = { onCancel() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSave(updatedDevices) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    name: String,
    icon: ImageVector,
    initialState: Boolean = false
) {
    var isOn by remember { mutableStateOf(initialState) }
    var power by remember { mutableStateOf("10W") }
    var time by remember { mutableStateOf("02:34") }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .width(172.dp)
            .background(Color.White, RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp)
            .padding(top = 15.dp)
    ) {
        Column{
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon perangkat
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF3F3F3), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = name, tint = Color.Black)
                }

                // Switch ON/OFF
                Switch(
                    checked = isOn,
                    onCheckedChange = { state ->
                        isOn = state
                        power = if (isOn) "10W" else "0W"
                        time = if (isOn) "02:34" else "00:00"
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Green,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFFF3F3F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama Perangkat
            Text(text = name, fontWeight = FontWeight.Bold)

            // Status Perangkat
            Text(text = if (isOn) "🟢 ON  $power  ⏳$time" else "⚫ OFF", fontSize = 12.sp)

            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .size(30.dp)  // Mengurangi ukuran tombol agar lebih kecil
                    .align(Alignment.CenterHorizontally) // Agar tetap di tengah
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Details",
                    modifier = Modifier.size(20.dp) // Mengurangi ukuran ikon agar proporsional
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }

    // 🔹 Modal Bottom Sheet untuk Detail Perangkat
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            DeviceDetails(name, isOn, power, time, onToggle = {
                isOn = !isOn
                power = if (isOn) "10W" else "0W"
                time = if (isOn) "02:34" else "00:00"
            })
        }
    }
}

@Composable
fun DeviceDetails(
    name: String,
    isOn: Boolean,
    power: String,
    time: String,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Detail $name",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Informasi perangkat dengan ikon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            InfoRow(icon = Icons.Default.Bolt, label = "Daya", value = power)
            InfoRow(icon = Icons.Default.AccessTime, label = "Waktu", value = time)

            Spacer(modifier = Modifier.height(12.dp))

            // Switch ON/OFF dengan Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = "Power",
                    tint = if (isOn) Color.Green else Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Status: ${if (isOn) "ON" else "OFF"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Green,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFFF3F3F3)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grafik Penggunaan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Usage Graph", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LineChart(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    data = remember {
                        listOf(
                            Line(
                                label = "Windows",
                                values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
                                color = SolidColor(Color(0xFF23af92)),
                                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                                secondGradientFillColor = Color.Transparent,
                                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                gradientAnimationDelay = 1000,
                                drawStyle = DrawStyle.Stroke(width = 2.dp),
                            )
                        )
                    },
                    animationMode = AnimationMode.Together(delayBuilder = {
                        it * 500L
                    }),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF23af92),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}


fun getAirQualityIndex(weather: WeatherResponse?): Int {
    return weather?.main?.humidity?.div(10) ?: 50 // Contoh perhitungan sederhana atau default AQI
}

@Composable
fun WeatherCard(
    temperature: Int,
    condition: String,
    location: String,
    feelsLike: Int,
    humidity: Int,
    windSpeed: Int,
    airQualityIndex: Int
) {
    val backgroundImage = painterResource(id = getWeatherBackgroundImage(condition))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Sesuaikan tinggi card
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 🔹 Background Image
        AsyncImage(
            model = getWeatherBackgroundImage(condition),
            contentDescription = "Weather Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🔹 Overlay agar teks tetap terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // Tambahkan efek gelap transparan
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (isVector, icon) = getWeatherIcon(condition)
                        if (isVector) {
                            Icon(
                                imageVector = icon as ImageVector,
                                contentDescription = "Weather Icon",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = icon as Int),
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = condition,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$temperature°",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = location,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherInfoItem(label = "Feels Like", value = "$feelsLike°C")
                    WeatherInfoItem(label = "Humidity", value = "$humidity%")
                    WeatherInfoItem(label = "Wind", value = "$windSpeed km/h")
                    WeatherInfoItem(label = "AQI", value = airQualityIndex.toString())
                }
            }
        }
    }
}

@Composable
fun getWeatherBackgroundImage(condition: String): Int {
    return when (condition.lowercase()) {
        "clear", "sunny" -> R.drawable.sunny_bg2
        "clouds", "partly cloudy" -> R.drawable.bg_cloudy
        "rain", "light rain", "moderate rain", "heavy rain", "drizzle" -> R.drawable.bg_rainy
        "thunderstorm" -> R.drawable.bg_thunderstorm
        else -> R.drawable.unkown_bg
    }
}

@Composable
fun getWeatherIcon(condition: String): Pair<Boolean, Any> {
    return when (condition.lowercase()) {
        "clear", "sunny" -> Pair(true, Icons.Outlined.WbSunny)
        "clouds", "partly cloudy" -> Pair(true, Icons.Outlined.WbCloudy)
        "rain", "light rain", "moderate rain", "heavy rain" -> Pair(false, R.drawable.rainy_24px)
        "thunderstorm" -> Pair(true, Icons.Outlined.Bolt)
        "drizzle" -> Pair(true, Icons.Outlined.Grain)
        "snow" -> Pair(true, Icons.Outlined.AcUnit)
        "mist", "fog", "haze" -> Pair(true, Icons.Outlined.VisibilityOff)
        else -> Pair(true, Icons.Outlined.HelpOutline)
    }
}

@Composable
fun WeatherInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DeviceUsageChart(navController: NavController?, onNavigate: (String) -> Unit) {
    var selectedPieIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }
    var tooltipText by remember { mutableStateOf("") }
    var data by remember {
        mutableStateOf(
            listOf(
                Pie(label = "Android", data = 20.0, color = Color.Red, selectedColor = Color.Green),
                Pie(label = "Windows", data = 45.0, color = Color.Cyan, selectedColor = Color.Blue),
                Pie(label = "Linux", data = 35.0, color = Color.Gray, selectedColor = Color.Yellow),
            )
        )
    }

    val total = data.sumOf { it.data }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Penggunaan Perangkat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    modifier = Modifier.size(200.dp),
                    data = data,
                    onPieClick = { clickedPie ->
                        val pieIndex = data.indexOf(clickedPie)
                        if (selectedPieIndex == pieIndex) {
                            showTooltip = false
                            selectedPieIndex = null
                        } else {
                            selectedPieIndex = pieIndex
                            tooltipText = "${clickedPie.label}: ${clickedPie.data}%"
                            showTooltip = true
                        }

                        data = data.mapIndexed { index, pie -> pie.copy(selected = selectedPieIndex == index) }
                    },
                    selectedScale = 1.2f,
                    selectedPaddingDegree = 4f,
                    style = Pie.Style.Stroke()
                )

                // Tooltip muncul di atas pie chart
                if (showTooltip) {
                    Popup(alignment = Alignment.TopCenter) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black, shape = RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tooltipText,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend dengan persentase
            Column {
                data.forEach { pie ->
                    val percentage = (pie.data / total * 100).toInt() // Hitung persentase
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(pie.color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        pie.label?.let { Text(text = it, fontSize = 14.sp, modifier = Modifier.weight(1f)) }
                        Text(text = "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}