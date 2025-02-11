package com.example.smarthome.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.R
import com.example.smarthome.ui.navigation.Screen

@Composable
fun AuthScreen(navController: NavController?) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Transparent)
    ) {
        // **Bagian Atas (Background & Icon)**
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .background(Color.Gray, shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo"
                )
            }
        }

        // **Bagian Bawah (Form & Tombol)**
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(Color.Transparent)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp)) // Tombol agak turun

            // **Tombol Login & Face Recognition (Sejajar Horizontal)**
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navController?.navigate(Screen.Login.route) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Text(text = "Login", color = Color.Black, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(15.dp))

                IconButton(
                    onClick = { navController?.navigate(Screen.FaceLogin.route) },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp)) // Mengubah dari CircleShape ke RoundedCornerShape
                        .background(Color.LightGray)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.familiar_face_and_zone_48px),
                        contentDescription = "Face Recognition",
                        tint = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp)) // Jarak ke teks lebih besar

            // **Navigasi ke Halaman Registrasi**
            Row {
                Text(text = "Belum Mempunyai Akun?", fontSize = 18.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Daftar",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController?.navigate(Screen.Register.route) }
                )
            }
        }
    }
}

// **Preview Jetpack Compose**
@Preview(showBackground = true)
@Composable
fun PreviewAuthScreen() {
    AuthScreen(navController = null)
}
