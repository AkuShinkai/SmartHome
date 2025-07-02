package com.example.smarthome.ui.screens

import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smarthome.R
import com.example.smarthome.api.WeatherRepository
import com.example.smarthome.data.MqttManager
import com.example.smarthome.data.UsageData
import com.example.smarthome.data.WeatherResponse
import com.example.smarthome.data.fetchAllUsageData
import com.example.smarthome.security.ApiKey
import com.example.smarthome.ui.viewmodel.Device
import com.example.smarthome.ui.viewmodel.DeviceSchedule
import com.example.smarthome.ui.viewmodel.DeviceSensorData
import com.example.smarthome.ui.viewmodel.DeviceViewModel
import com.example.smarthome.ui.viewmodel.observeStateChange
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavController?) {

    val BackgroundColor = Color(0xFFF3F3F3)

    var selectedTab by remember { mutableStateOf("Semua Alat") }
    var showEditDialog by remember { mutableStateOf(false) }

    var showHistoryDialog by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userName by remember { mutableStateOf<String?>(null) }

    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    val repository = WeatherRepository()

    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var availableRooms by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        val dbRef = FirebaseDatabase.getInstance().reference

        val defaultDevices = listOf(
            Triple("smart_lamp", "Smart Lamp", Icons.Default.Lightbulb),
            Triple("socket2_1", "Socket 2 Lubang 1", Icons.Default.Power),
            Triple("socket2_2", "Socket 2 Lubang 2", Icons.Default.Power),
            Triple("smart_door", "Smart Door", Icons.Default.DoorFront)
        )

        val fetchedDevices = mutableListOf<Device>()
        val roomsSet = mutableSetOf<String>()

        defaultDevices.forEach { (id, name, icon) ->
            dbRef.child(id).child("room").get().addOnSuccessListener { snapshot ->
                val room = snapshot.getValue(String::class.java) ?: ""
                fetchedDevices.add(Device(id, name, icon, room))
                roomsSet.add(room)

                // Update state setelah semua selesai
                if (fetchedDevices.size == defaultDevices.size) {
                    devices = fetchedDevices
                    availableRooms = roomsSet.toList()
                }
            }
        }
    }

    val viewModel: DeviceViewModel = viewModel()
    val switch1Status = viewModel.switch1Status
    val switch2Status = viewModel.switch2Status
    val doorstatus = viewModel.switch_door
    val lampstatus = viewModel.switch_lamp
    val powerValue = viewModel.powerValue

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

        // 🔹 Filter Tab (Sederhana, Bold Saat Aktif)
        val tabs = listOf("Semua Alat") + availableRooms

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tabs) { tab ->
                Text(
                    text = tab,
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp, horizontal = 3.dp),
                    color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Filter & Tampilkan Perangkat Berdasarkan Ruangan
        val filteredDevices = if (selectedTab == "Semua Alat") devices else devices.filter { it.room == selectedTab }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 12.dp
        ) {
            filteredDevices.forEach { device ->
                val context = LocalContext.current

                val isOn = when (device.id) {
                    "socket2_1" -> switch1Status == "ON"
                    "socket2_2" -> switch2Status == "ON"
                    "smart_lamp" -> lampstatus == "ON"
                    "smart_door" -> doorstatus == "ON"
                    else -> false
                }

                DeviceCard(
                    deviceId = device.id,
                    name = device.name,
                    icon = device.icon,
                    isOn = isOn,
                    power = if (isOn) {
                        when (device.id) {
                            "socket2_1" -> "${viewModel.powerValue}W"
                            "socket2_2" -> "${viewModel.powerValue}W"
                            "smart_lamp" -> "${viewModel.powerLamp}W"
                            "smart_door" -> "${viewModel.powerDoor}W"
                            else -> "0W"
                        }
                    } else "0W",
                    onToggle = { newState ->
                        val (message, expectedPath, expectedState) = when (device.id) {
                            "socket2_1" -> Triple(
                                if (newState) "SWITCH1_ON" else "SWITCH1_OFF",
                                "socket_2_lubang/switch/status1",
                                if (newState) "ON" else "OFF"
                            )
                            "socket2_2" -> Triple(
                                if (newState) "SWITCH2_ON" else "SWITCH2_OFF",
                                "socket_2_lubang/switch/status2",
                                if (newState) "ON" else "OFF"
                            )
                            "smart_lamp" -> Triple(
                                if (newState) "LAMP_ON" else "LAMP_OFF",
                                "smart_lamp/switch/status1",
                                if (newState) "ON" else "OFF"
                            )
                            "smart_door" -> Triple(
                                if (newState) "DOOR_ON" else "DOOR_OFF",
                                "smart_door/switch/status1",
                                if (newState) "ON" else "OFF"
                            )
                            else -> Triple("UNKNOWN", "", "")
                        }

                        // Kirim perintah
                        MqttManager.publish("home/switch/command", message)

                        // 🔁 Cek apakah status berubah di RTDB dalam 5 detik
                        observeStateChange(expectedPath, expectedState, 5000L) { success ->
                            if (!success) {
                                Toast.makeText(
                                    context,
                                    "❌ Gagal mengontrol ${device.name}.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

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

@Composable
fun EditDeviceDialog(
    devices: List<Device>,
    onSave: (List<Device>) -> Unit,
    onCancel: () -> Unit
) {
    val rooms = listOf("Kamar Tidur", "Ruang Tamu", "Dapur", "Ruang Makan", "Teras")

    val distinctDevices = devices.distinctBy {
        if (it.id == "socket2_1" || it.id == "socket2_2") "socket_2_lubang" else it.id
    }.map {
        if (it.id == "socket2_1" || it.id == "socket2_2") {
            it.copy(id = "socket_2_lubang", name = "Socket 2 Lubang")
        } else it
    }

    var updatedDevices by remember { mutableStateOf(distinctDevices) }

    Dialog(onDismissRequest = { onCancel() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit Tempat Alat",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(updatedDevices) { index, device ->
                        var expanded by remember { mutableStateOf(false) }
                        var selectedRoom by remember { mutableStateOf(device.room) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(device.name, fontWeight = FontWeight.SemiBold)

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(onClick = { expanded = true }) {
                                    Text(selectedRoom)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    rooms.forEach { room ->
                                        DropdownMenuItem(
                                            text = { Text(room) },
                                            onClick = {
                                                selectedRoom = room
                                                expanded = false
                                                updatedDevices = updatedDevices.toMutableList().apply {
                                                    this[index] = device.copy(room = selectedRoom)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val dbRef = FirebaseDatabase.getInstance().reference
                            updatedDevices.forEach { device ->
                                when (device.id) {
                                    "socket_2_lubang" -> {
                                        dbRef.child("socket2_1").child("room").setValue(device.room)
                                        dbRef.child("socket2_2").child("room").setValue(device.room)
                                    }
                                    else -> {
                                        dbRef.child(device.id).child("room").setValue(device.room)
                                    }
                                }
                            }
                            onSave(updatedDevices)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    deviceId: String,
    name: String,
    icon: ImageVector,
    isOn: Boolean,
    power: String,
    onToggle: (Boolean) -> Unit
) {
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
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF3F3F3), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = name, tint = Color.Black)
                }

                Switch(
                    checked = isOn,
                    onCheckedChange = { state ->
                        onToggle(state)
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
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = if (isOn) "🟢 ON ⚡$power" else "⚫ OFF", fontSize = 11.sp)

            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Details",
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }

    if (showBottomSheet) {
        val realDeviceId = mapUiIdToDbId(deviceId)
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            DeviceDetails(
                deviceId = realDeviceId,
                name = name,
                isOn = isOn,
                onToggle = {
                    onToggle(!isOn)
                }
            )
        }
    }
}

@Composable
fun DeviceDetails(
    deviceId: String,
    name: String,
    isOn: Boolean,
    onToggle: () -> Unit
) {
    val scrollState = rememberScrollState()

    // STATE untuk sensor data
    var sensorData by remember { mutableStateOf(DeviceSensorData()) }

    // Baca data dari Firebase Realtime Database
    LaunchedEffect(deviceId, isOn) {
        val dbRef = FirebaseDatabase.getInstance().getReference("$deviceId/sensors")

        if (!isOn) {
            // Jika perangkat OFF, kosongkan data sensor
            sensorData = DeviceSensorData()
        } else {
            // Kalau ON, pasang listener untuk baca realtime
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sensorData = DeviceSensorData(
                        power = snapshot.child("power").getValue(Double::class.java) ?: 0.0,
                        current = snapshot.child("current").getValue(Double::class.java) ?: 0.0,
                        voltage = snapshot.child("voltage").getValue(Double::class.java) ?: 0.0,
                        energy = snapshot.child("energy").getValue(Double::class.java) ?: 0.0
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeviceDetails", "Database error: ${error.message}")
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // chartData tetap berdasarkan power (daily)
        val usageData by produceState(initialValue = emptyList<UsageData>(), deviceId) {
            val allData = fetchAllUsageData()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis

            val filtered = allData.filter {
                it.device == deviceId && it.timestamp.toDate().time >= startOfDay
            }
            value = filtered
        }

        val chartData = usageData.map { it.power.toDouble() }

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

            // Kartu sensor detail
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                // Baris 1: Power & Voltage
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InfoRow(
                        icon = Icons.Default.Bolt,
                        label = "Daya",
                        value = "${sensorData.power} W",
                        modifier = Modifier.weight(1f)
                    )
                    InfoRow(
                        icon = Icons.Default.FlashOn,
                        label = "Tegangan",
                        value = "${sensorData.voltage} V",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Baris 2: Current & Energy
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InfoRow(
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Arus",
                        value = "${sensorData.current} A",
                        modifier = Modifier.weight(1f)
                    )
                    InfoRow(
                        icon = Icons.Default.EnergySavingsLeaf,
                        label = "Energi",
                        value = "${sensorData.energy} Wh",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = "Power",
                        tint = if (isOn) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Status: ${if (isOn) "ON" else "OFF"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOn) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isOn,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (deviceId == "smart_lamp") {
                DeviceScheduleSection(deviceId = deviceId)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text(text = "Grafik Penggunaan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LineChart(
                        modifier = Modifier.padding(horizontal = 22.dp),
                        data = listOf(
                            Line(
                                label = name,
                                values = chartData,
                                color = SolidColor(Color(0xFF23af92)),
                                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                                secondGradientFillColor = Color.Transparent,
                                drawStyle = DrawStyle.Stroke(width = 2.dp),
                            )
                        ),
                        animationMode = AnimationMode.Together { it * 300L }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = label, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeviceScheduleSection(deviceId: String) {
    val firestore = Firebase.firestore
    var scheduleList by remember { mutableStateOf(listOf<DeviceSchedule>()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var showTimePicker by remember { mutableStateOf(false) }

    var selectedAction by remember { mutableStateOf("ON") }
    val actionOptions = listOf("ON", "OFF")
    var expanded by remember { mutableStateOf(false) }

    // 🔁 Load data dari Firestore
    LaunchedEffect(deviceId) {
        val doc = firestore.collection("deviceSchedules").document(deviceId).get().await()
        val list = doc.get("schedules") as? List<Map<String, Any>> ?: emptyList()
        scheduleList = list.map {
            DeviceSchedule(
                time = it["time"] as? String ?: "",
                action = it["action"] as? String ?: ""
            )
        }
    }

    // 🔁 Simpan data ke Firestore
    fun saveToFirestore(list: List<DeviceSchedule>) {
        val data = list.map {
            mapOf(
                "time" to it.time,
                "action" to it.action
            )
        }
        firestore.collection("deviceSchedules").document(deviceId)
            .set(mapOf("schedules" to data))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Penjadwalan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Tampilkan jadwal
        scheduleList.forEachIndexed { index, schedule ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Jadwal",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.time,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Aksi: ${schedule.action}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = {
                        val newList = scheduleList.toMutableList()
                        newList.removeAt(index)
                        scheduleList = newList
                        saveToFirestore(newList)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🕒 + ⚡️ Tambah waktu & aksi
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("%02d:%02d".format(selectedHour, selectedMinute), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier
                .weight(1f)
                .wrapContentSize(Alignment.TopStart)) {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedAction)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    actionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedAction = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                val newSchedule = DeviceSchedule(
                    time = "%02d:%02d".format(selectedHour, selectedMinute),
                    action = selectedAction
                )
                val newList = scheduleList + newSchedule
                scheduleList = newList
                saveToFirestore(newList)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            },
            selectedHour, selectedMinute, true
        ).show()
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
    val deviceLabelMap = mapOf(
        "socket_2_lubang" to "Socket 2",
        "smart_lamp" to "Lampu",
        "smart_door" to "Pintu"
    )

    fun colorForDevice(device: String): Color {
        return when (device) {
            "socket_2_lubang" -> Color(0xFFE53935)
            "smart_lamp" -> Color(0xFF3F51B5)
            "smart_door" -> Color(0xFF43A047)
            else -> Color.Gray
        }
    }

    var usageData by remember { mutableStateOf<List<UsageData>>(emptyList()) }
    var data by remember { mutableStateOf<List<Pie>>(emptyList()) }

    LaunchedEffect(Unit) {
        val allUsage = fetchAllUsageData() // Fungsi Firestore milikmu
        // Filter hanya data hari ini
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        val dailyUsage = allUsage.filter {
            it.timestamp.toDate().time >= startOfDay
        }
        usageData = dailyUsage

        // Group by device
        val grouped = dailyUsage.groupBy { it.device }
        val pieData = grouped.map { (device, entries) ->
            val label = deviceLabelMap[device] ?: device
            val total = entries.sumOf { it.power.toDouble() }
            Pie(
                label = label,
                data = total,
                color = colorForDevice(device),
                selectedColor = colorForDevice(device).copy(alpha = 0.6f)
            )
        }

        data = pieData
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
                            val percentage = if (total != 0.0) (clickedPie.data / total * 100).toInt() else 0
                            tooltipText = "${clickedPie.label}: $percentage%"
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

fun mapUiIdToDbId(uiId: String): String {
    return when (uiId) {
        "socket2_1", "socket2_2" -> "socket_2_lubang"
        else -> uiId
    }
}
