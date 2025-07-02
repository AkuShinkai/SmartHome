package com.example.smarthome.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthome.CameraPreview
import com.example.smarthome.model.FaceNetModel
import com.example.smarthome.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.face.Face

@Composable
fun AddFaceScreen(navController: NavController) {
    val context = LocalContext.current
    val faceNetModel = remember { FaceNetModel(context) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var faceDetected by remember { mutableStateOf(false) }
    var latestFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var latestBoundingBox by remember { mutableStateOf<Rect?>(null) }
    var faceEmbedding by remember { mutableStateOf<FloatArray?>(null) }
    var faceLabel by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Scan Wajah") }

    var showCancelDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        showCancelDialog = true
    }

    val minFaceSize = 250 * 250
    val maxFaceSize = 400 * 400

    val onFacesDetected: (List<Face>, Bitmap) -> Unit = { faces, bitmap ->
        val largestFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
        if (largestFace != null) {
            val faceArea = largestFace.boundingBox.width() * largestFace.boundingBox.height()
            when {
                faceArea < minFaceSize -> {
                    faceDetected = false
                    buttonText = "Wajah terlalu jauh"
                }
                faceArea > maxFaceSize -> {
                    faceDetected = false
                    buttonText = "Wajah terlalu dekat"
                }
                else -> {
                    faceDetected = true
                    latestFaceBitmap = bitmap
                    latestBoundingBox = largestFace.boundingBox
                    buttonText = "Scan Wajah"
                }
            }
        } else {
            faceDetected = false
            buttonText = "Tidak ada wajah terdeteksi"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tambah Wajah", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("Scan wajah baru untuk akun Anda", fontSize = 14.sp, color = Color.Gray)

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

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = faceLabel,
            onValueChange = { faceLabel = it },
            label = { Text("Label Wajah") },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (faceDetected && latestFaceBitmap != null && latestBoundingBox != null && faceLabel.isNotBlank()) {
                    isProcessing = true
                    faceEmbedding = faceNetModel.getFaceEmbedding(latestFaceBitmap!!, latestBoundingBox!!)

                    val uid = currentUser?.uid
                    if (uid != null && faceEmbedding != null) {
                        val embeddingList = faceEmbedding!!.toList()
                        val db = FirebaseFirestore.getInstance()
                        val userDocRef = db.collection("users").document(uid)

                        // Tambahkan atau update field faceEmbeddings sebagai map
                        userDocRef.get().addOnSuccessListener { document ->
                            val currentMap = document.get("faceEmbeddings") as? Map<String, List<Float>> ?: emptyMap()

                            val updatedMap = currentMap.toMutableMap()
                            updatedMap[faceLabel] = embeddingList

                            userDocRef.update("faceEmbeddings", updatedMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Wajah berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Gagal menambahkan wajah: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener {
                                    isProcessing = false
                                }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (buttonText == "Scan Wajah") Color.Blue else Color.Gray),
            shape = RoundedCornerShape(10.dp),
            enabled = buttonText == "Scan Wajah" && !isProcessing && faceLabel.isNotBlank()
        ) {
            Text(if (isProcessing) "Menyimpan..." else buttonText, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showCancelDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Batal", color = Color.Black)
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Batalkan Penambahan Wajah") },
                text = { Text("Apakah kamu yakin ingin membatalkan proses penambahan wajah?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelDialog = false
                        navController.navigate(Screen.Me.route) {
                            popUpTo("add_face_screen") { inclusive = true }
                        }
                    }) {
                        Text("Ya")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Tidak")
                    }
                }
            )
        }
    }
}
