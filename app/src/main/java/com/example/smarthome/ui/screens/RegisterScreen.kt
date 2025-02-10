package com.example.smarthome.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.R
import com.example.smarthome.ui.theme.SmartHomeTheme

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text("Register", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(5.dp))
        Text("Masukkan Informasi Dibawah Ini", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        // WRAPPER FORM
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFFD9D9D9))
                .padding(horizontal = 20.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp) // Memberikan jarak otomatis antar elemen
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InputField(value = name, onValueChange = { name = it }, label = "Name", icon = R.drawable.ic_launcher_foreground)
                InputField(value = email, onValueChange = { email = it }, label = "E-Mail", icon = R.drawable.ic_launcher_foreground, keyboardType = KeyboardType.Email)
                InputField(value = password, onValueChange = { password = it }, label = "Password", icon = R.drawable.ic_launcher_foreground, keyboardType = KeyboardType.Password)
                InputField(value = rePassword, onValueChange = { rePassword = it }, label = "Re-Enter Password", icon = R.drawable.ic_launcher_foreground, keyboardType = KeyboardType.Password)
            }

            // Checkbox dan teks syarat ketentuan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = { agreeTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Gray, // Warna saat checkbox tercentang
                        uncheckedColor = Color.Black, // Warna outline saat tidak dicentang
                        checkmarkColor = Color.White // Warna centang di dalam checkbox
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "Saya setuju dengan syarat dan ketentuan yang berlaku",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tombol Next
            Button(
                onClick = { /* Handle registration */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Navigasi ke Login
            Text(
                text = "Sudah Memiliki Akun?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate("auth_screen") {
                        popUpTo("register_screen") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(30.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp), // Membuat sudut lebih smooth
        singleLine = true, // Agar tidak multiline
        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White, // Warna background putih
            focusedBorderColor = Color.Blue, // Warna outline saat fokus
            unfocusedBorderColor = Color.LightGray, // Warna outline saat tidak fokus
            cursorColor = Color.Black // Warna kursor
        )
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    SmartHomeTheme {
        RegisterScreen(navController = rememberNavController())
    }
}
