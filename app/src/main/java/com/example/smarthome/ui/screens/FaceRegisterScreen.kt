package com.example.smarthome.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.CameraPreview
import com.example.smarthome.model.FaceNetModel
import com.example.smarthome.security.AESUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.face.Face

@Composable
fun FaceRegisterScreen(navController: NavController, name: String, email: String, password: String) {
    val context = LocalContext.current
    val faceNetModel = remember { FaceNetModel(context) }

    var faceDetected by remember { mutableStateOf(false) }
    var latestFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var latestBoundingBox by remember { mutableStateOf<Rect?>(null) }
    var faceEmbedding by remember { mutableStateOf<FloatArray?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val onFacesDetected: (List<Face>, Bitmap) -> Unit = { faces, bitmap ->
        if (faces.isNotEmpty()) {
            val largestFace = faces.maxByOrNull { face ->
                face.boundingBox.width() * face.boundingBox.height()
            }

            if (largestFace != null) {
                faceDetected = true
                latestFaceBitmap = bitmap
                latestBoundingBox = largestFace.boundingBox
                Log.d("FaceRecognition", "Wajah terbesar dipilih: ${largestFace.boundingBox}")
            }
        } else {
            faceDetected = false
            latestFaceBitmap = null
            latestBoundingBox = null
            faceEmbedding = null
            Log.d("FaceRecognition", "Tidak ada wajah terdeteksi")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 22.sp)
        Text("Scan wajah anda untuk akses login mudah", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .border(10.dp, Color(0xFFBDB4FF), CircleShape)
                .background(Color(0xFFF2F2FF)),
            contentAlignment = Alignment.Center
        ) {
            CameraPreview(
                modifier = Modifier.matchParentSize(),
                onFacesDetected = onFacesDetected
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                if (faceDetected && latestFaceBitmap != null && latestBoundingBox != null) {
                    isProcessing = true
                    faceEmbedding = faceNetModel.getFaceEmbedding(latestFaceBitmap!!, latestBoundingBox!!)

                    // Enkripsi password sebelum menyimpannya ke Firestore
                    val encryptedPassword = AESUtil.encrypt(password)

                    registerUser(email, encryptedPassword, password, name, faceEmbedding!!, context, navController) {
                        isProcessing = false
                    }
                } else {
                    Toast.makeText(context, "Tidak ada wajah terdeteksi!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (faceDetected) Color.Blue else Color.Gray),
            shape = RoundedCornerShape(10.dp),
            enabled = faceDetected && !isProcessing
        ) {
            Text(if (isProcessing) "Menyimpan..." else "Scan Wajah", color = Color.White)
        }
    }
}

// Fungsi untuk mendaftarkan user ke Firebase Authentication dan menyimpan embedding wajah
fun registerUser(
    email: String,
    encryptedPassword: String,
    password: String,
    name: String,
    faceEmbedding: FloatArray,
    context: android.content.Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                val userMap = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "encryptedPassword" to encryptedPassword, // Simpan password terenkripsi
                    "faceEmbedding" to faceEmbedding.toList()
                )

                db.collection("users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        Log.d("RegisterUser", "Registrasi berhasil: UserID = $userId")
                        Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                        navController.navigate("auth_screen") // Kembali ke login setelah registrasi
                    }
                    .addOnFailureListener { e ->
                        Log.e("RegisterUser", "Gagal menyimpan data ke Firestore", e)
                        Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener { onComplete() }
            } else {
                Log.e("RegisterUser", "Registrasi gagal", task.exception)
                Toast.makeText(context, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
}
