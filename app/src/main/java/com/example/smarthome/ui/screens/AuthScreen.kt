package com.example.smarthome.ui.screens

import android.content.Context
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import com.example.smarthome.R
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.component.FaceLoginBottomSheet
import com.example.smarthome.ui.component.LoginBottomSheet
import com.example.smarthome.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

val PrimaryColor = Color(0xFF2AABD5)
val SecondaryColor = Color(0xFF54BCDE)
val BackgroundColor = Color(0xFFFFFFFF)
val ButtonColor = Color(0xFF1A91C1)
val TextColor = Color(0xFF005A80)

@Composable
fun AuthScreen(navController: NavController?) {
    var showLoginSheet by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var showFaceLoginSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .background(brush = Brush.linearGradient(
                    colors = listOf(Color(0xff7e02d6), Color( 0xFF2AABD5), Color(0xFF54BCDE), Color( 0xFF2AABD5),Color(0xff7e02d6))
                ), shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(BackgroundColor)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showLoginSheet = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Text(text = "Login", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(15.dp))

                IconButton(
                    onClick = { showFaceLoginSheet = true },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ButtonColor)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.familiar_face_and_zone_48px),
                        contentDescription = "Face Recognition",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Row {
                Text(text = "Belum Mempunyai Akun?", fontSize = 18.sp, color = TextColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Daftar",
                    fontSize = 18.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController?.navigate(Screen.Register.route) }
                )
            }
        }
    }

    if (showLoginSheet) {
        LoginBottomSheet(
            onDismiss = { showLoginSheet = false },
            onLogin = { email, password ->
                val validationError = validateLogin(email, password)
                if (validationError != null) {
                    loginError = validationError
                    return@LoginBottomSheet
                }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        scope.launch {
                            sessionManager.saveSession(true, email)
                        }
                        Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        showNotification(context, "Smart Home", "Selamat datang di aplikasi!")
                        navController?.navigate(Screen.Home.route)
                        showLoginSheet = false
                    }
                    .addOnFailureListener {
                        loginError = it.localizedMessage
                    }
            }
        )
    }

    if (showFaceLoginSheet) {
        FaceLoginBottomSheet(
            onDismiss = { showFaceLoginSheet = false },
            navController = navController ?: return
        )
    }

    loginError?.let {
        AlertDialog(
            onDismissRequest = { loginError = null },
            title = { Text("Login Gagal") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { loginError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

fun validateLogin(email: String, password: String): String? {
    if (email.isEmpty() || password.isEmpty()) {
        return "Email dan Password tidak boleh kosong."
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return "Format email tidak valid."
    }
    if (password.length < 6) {
        return "Password minimal 6 karakter."
    }
    return null
}

fun showNotification(context: Context, title: String, message: String) {
    val notificationId = 1
    val channelId = "smarthome_notifications"

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
}
