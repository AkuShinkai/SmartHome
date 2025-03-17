package com.example.smarthome.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.R
import com.example.smarthome.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {

    val PrimaryColor = Color(0xFF2AABD5)
    val SecondaryColor = Color(0xFF54BCDE)
    val BackgroundColor = Color(0xFFFFFFFF)
    val ButtonColor = Color(0xFF1A91C1)
    val TextColor = Color(0xFF005A80)

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showTermsDialog by remember { mutableStateOf(false) }
    var hasReadTerms by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Register", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(5.dp))
        Text("Masukkan Informasi Dibawah Ini", fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(BackgroundColor)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            InputField(value = name, onValueChange = { name = it }, label = "Name", icon = R.drawable.id_card_48px)
            InputField(value = email, onValueChange = { email = it }, label = "E-Mail", icon = R.drawable.baseline_alternate_email_24, keyboardType = KeyboardType.Email)
            PasswordField(value = password, onValueChange = { password = it }, label = "Password", icon = R.drawable.key_48px)
            PasswordField(value = rePassword, onValueChange = { rePassword = it }, label = "Re-Enter Password", icon = R.drawable.vpn_key_alert_48px)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = null, // Nonaktifkan perubahan langsung dari pengguna
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Gray,
                        uncheckedColor = Color.Black,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "Saya setuju dengan syarat dan ketentuan yang berlaku",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
            }
            if (showTermsDialog) {
                val scrollState = rememberScrollState()
                var isScrolledToBottom by remember { mutableStateOf(false) }

                // Cek jika pengguna telah scroll sampai bawah
                LaunchedEffect(scrollState.value) {
                    isScrolledToBottom = scrollState.value >= scrollState.maxValue
                }

                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = { Text("Syarat dan Ketentuan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp)
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            val termsText = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("1. Penggunaan Wajah untuk Login\n")
                                }
                                append("- Data wajah Anda digunakan untuk autentikasi dan disimpan dalam bentuk terenkripsi.\n")
                                append("- Aplikasi hanya membandingkan wajah Anda dengan data yang telah didaftarkan sebelumnya.\n")
                                append("- Kami tidak membagikan atau menjual data wajah Anda kepada pihak ketiga.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("2. Akses Kamera\n")
                                }
                                append("- Aplikasi memerlukan akses kamera untuk menangkap gambar wajah Anda saat login atau registrasi.\n")
                                append("- Data yang diambil hanya digunakan untuk autentikasi dan tidak disimpan permanen tanpa izin Anda.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("3. Akses Galeri\n")
                                }
                                append("- Jika Anda memilih mengunggah gambar wajah dari galeri, aplikasi akan meminta izin akses penyimpanan.\n")
                                append("- Data yang diunggah hanya digunakan untuk verifikasi wajah.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("4. Akses Internet\n")
                                }
                                append("- Aplikasi memerlukan koneksi internet untuk autentikasi dan penyimpanan data fitur.\n")
                                append("- Pastikan koneksi stabil untuk pengalaman optimal.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("5. Akses Lokasi\n")
                                }
                                append("- Aplikasi dapat meminta akses lokasi untuk meningkatkan keamanan atau menyesuaikan layanan.\n")
                                append("- Informasi lokasi tidak akan dibagikan tanpa izin eksplisit.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("6. Tanggung Jawab Pengguna\n")
                                }
                                append("- Anda bertanggung jawab atas keamanan akun dan tidak boleh membagikan akses login.\n")
                                append("- Penyalahgunaan aplikasi dapat mengakibatkan pembatasan atau penghentian layanan.\n\n")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("7. Pembaruan Syarat dan Ketentuan\n")
                                }
                                append("- Kami dapat memperbarui syarat dan ketentuan sewaktu-waktu.\n")
                                append("- Anda akan diberitahu jika ada perubahan signifikan.\n")
                            }
                            Text(
                                text = termsText,
                                fontSize = 14.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Left,
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showTermsDialog = false
                                hasReadTerms = true
                                agreeTerms = true // Setuju, checkbox akan aktif
                            },
                            modifier = Modifier.padding(8.dp),
                            enabled = isScrolledToBottom // Hanya bisa ditekan jika sudah scroll ke bawah
                        ) {
                            Text("Setuju")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showTermsDialog = false
                                hasReadTerms = true
                                agreeTerms = false // Tolak, checkbox tetap tidak aktif
                            },
                            modifier = Modifier.padding(8.dp),
                            enabled = isScrolledToBottom // Hanya bisa ditekan jika sudah scroll ke bawah
                        ) {
                            Text("Tolak")
                        }
                    }
                )
            }

            Button(
                onClick = {
                    if (!email.endsWith("@gmail.com")) {
                        showToast(context, "Email harus menggunakan @gmail.com!")
                    } else if (password.length < 8) {
                        showToast(context, "Password minimal 8 karakter!")
                    } else if (validateForm(name, email, password, rePassword, agreeTerms)) {
                        isLoading = true
                        checkEmailExists(email, context) { exists ->
                            isLoading = false
                            if (exists) {
                                showToast(context, "Email sudah digunakan!")
                            } else {
                                navController.navigate("face_register_screen/$name/$email/$password")
                            }
                        }
                    } else {
                        showToast(context, "Pastikan semua data terisi dengan benar!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
            ) {
                Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Sudah Memiliki Akun",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Fungsi untuk menampilkan Toast
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

// Fungsi cek email di Firebase
fun checkEmailExists(email: String, context: Context, onResult: (Boolean) -> Unit) {
    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                onResult(signInMethods.isNotEmpty())
            } else {
                showToast(context, "Terjadi kesalahan saat memeriksa email: ${task.exception?.message}")
                onResult(false)
            }
        }
}

// Fungsi Validasi Form
fun validateForm(
    name: String,
    email: String,
    password: String,
    rePassword: String,
    agreeTerms: Boolean
): Boolean {
    val isEmailValid = email.endsWith("@gmail.com") && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.length >= 8

    return name.isNotBlank() &&
            isEmailValid &&
            isPasswordValid &&
            password == rePassword &&
            agreeTerms
}

// Input Field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String, icon: Int, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(painter = painterResource(id = icon), contentDescription = label, modifier = Modifier.size(24.dp), tint = Color.Black) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black,
            unfocusedTextColor = Color.Gray,
            focusedTextColor = Color.Black,
            focusedLabelColor = Color.Black
        )
    )
}

// Password Field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, icon: Int) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(painter = painterResource(id = icon), contentDescription = label, modifier = Modifier.size(24.dp), tint = Color.Black) },
        trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(painter = painterResource(id = if (passwordVisible) R.drawable.visibility_24px else R.drawable.visibility_off_24px), contentDescription = null, tint = Color.Gray)
        }},
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black,
            unfocusedTextColor = Color.Gray,
            focusedTextColor = Color.Black,
            focusedLabelColor = Color.Black
        )
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController)
}
