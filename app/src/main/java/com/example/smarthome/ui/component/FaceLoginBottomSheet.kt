package com.example.smarthome.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceLoginBottomSheet(
    onDismiss: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val faceNetModel = remember { FaceNetModel(context) }

    var faceDetected by remember { mutableStateOf(false) }
    var latestFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var faceEmbedding by remember { mutableStateOf<FloatArray?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var latestBoundingBox by remember { mutableStateOf<android.graphics.Rect?>(null) }

    var targetBlinkCount by remember { mutableStateOf((1..5).random()) }
    var currentBlinkCount by remember { mutableStateOf(0) }
    var prevLeftEyeOpen by remember { mutableStateOf(1.0f) }

    var buttonText by remember { mutableStateOf("Login dengan Wajah") }
    var isButtonEnabled by remember { mutableStateOf(true) }

    val sessionManager = remember { SessionManager(context) }

    val minFaceSize = 250 * 250
    val maxFaceSize = 400 * 400

    val onFacesDetected: (List<Face>, Bitmap) -> Unit = { faces, bitmap ->
        latestFaceBitmap?.recycle()

        if (faces.isNotEmpty()) {
            val largestFace = faces.maxByOrNull { face -> face.boundingBox.width() * face.boundingBox.height() }

            if (largestFace != null) {
                val faceArea = largestFace.boundingBox.width() * largestFace.boundingBox.height()

                if (faceArea in minFaceSize..maxFaceSize) {
                    faceDetected = true
                    latestFaceBitmap = bitmap.config?.let { bitmap.copy(it, true) }
                    bitmap.recycle()
                    latestBoundingBox = largestFace.boundingBox

                    val leftEyeOpen = largestFace.leftEyeOpenProbability ?: -1f
                    Log.d("Liveness", "Probabilitas mata kiri terbuka: $leftEyeOpen")

                    // Deteksi kedipan saat transisi dari terbuka ke tertutup
                    if (leftEyeOpen < 0.2f && prevLeftEyeOpen > 0.8f) {
                        currentBlinkCount++
                        Log.d("Liveness", "Kedipan ke-$currentBlinkCount terdeteksi!")
                    }
                    prevLeftEyeOpen = leftEyeOpen

                    val blinkGoalReached = currentBlinkCount >= targetBlinkCount
                    Log.d("Liveness", "Progress kedipan: $currentBlinkCount dari $targetBlinkCount")

                    buttonText = if (blinkGoalReached) "Login dengan Wajah"
                    else "Kedipkan mata ${targetBlinkCount - currentBlinkCount}x lagi"
                } else {
                    faceDetected = false
                    latestBoundingBox = null
                    buttonText = if (faceArea < minFaceSize) "Jarak terlalu jauh" else "Jarak terlalu dekat"
                }
            }
        } else {
            faceDetected = false
            latestBoundingBox = null
            buttonText = "Tidak ada wajah terdeteksi"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Face Authentication", fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.size(250.dp).clip(CircleShape).background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.matchParentSize(),
                    onFacesDetected = onFacesDetected
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    if (faceDetected && latestFaceBitmap != null && isButtonEnabled && currentBlinkCount >= targetBlinkCount) {
                        isProcessing = true
                        isButtonEnabled = false
                        buttonText = "Memproses..."

                        try {
                            faceEmbedding = faceNetModel.getFaceEmbedding(latestFaceBitmap!!, latestBoundingBox!!)
                            latestFaceBitmap?.recycle()
                            latestFaceBitmap = null

                            authenticateWithFace(
                                faceEmbedding!!,
                                context,
                                navController,
                                sessionManager,
                                coroutineScope
                            ) {
                                isProcessing = false
                                buttonText = "Login dengan Wajah"
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    isButtonEnabled = true
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("FaceLogin", "Error mendapatkan embedding wajah: ${e.message}", e)
                            buttonText = "Terjadi kesalahan"
                            isProcessing = false
                            isButtonEnabled = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(10.dp),
                enabled = isButtonEnabled && buttonText == "Login dengan Wajah" && !isProcessing
            ) {
                Text(buttonText)
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Batal
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(Color.Gray)
            ) {
                Text("Batal", color = Color.White)
            }
        }
    }
}

fun authenticateWithFace(
    faceEmbedding: FloatArray,
    context: Context,
    navController: NavController?,
    sessionManager: SessionManager,
    coroutineScope: CoroutineScope,
    onComplete: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val currentEmail = currentUser?.email

    if (userId == null || currentEmail == null) {
        Log.e("FaceAuth", "User belum login atau userId/email null")
        Toast.makeText(context, "User belum login, gunakan metode lain", Toast.LENGTH_SHORT).show()
        onComplete()
        return
    }

    Log.d("FaceAuth", "User terautentikasi sebagai $currentEmail ($userId)")

    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            Log.d("FaceAuth", "Dokumen user diambil untuk $userId")
            val data = document.data
            Log.d("FaceAuth", "Data dokumen lengkap: $data")

            val rawEmbeddings = data?.get("faceEmbeddings")
            Log.d("FaceAuth", "Raw embeddings: $rawEmbeddings (${rawEmbeddings?.javaClass?.name})")

            if (rawEmbeddings is Map<*, *>) {
                val embeddingsMap = mutableMapOf<String, List<Double>>()

                for ((label, vector) in rawEmbeddings) {
                    Log.d("FaceAuth", "🔍 Label: $label, vector: $vector")
                    if (label is String && vector is List<*>) {
                        val floatList = vector.mapNotNull {
                            when (it) {
                                is Double -> it
                                is Number -> it.toDouble()
                                else -> null
                            }
                        }

                        if (floatList.size == 512) {
                            embeddingsMap[label] = floatList
                            Log.d("FaceAuth", "Berhasil parsing embedding untuk label $label")
                        } else {
                            Log.w("FaceAuth", "⚠Panjang embedding untuk $label tidak valid: ${floatList.size}")
                        }
                    }
                }

                Log.d("FaceAuth", "Total parsed embeddings: ${embeddingsMap.size}")

                // Lanjutkan proses perbandingan
                var matched = false
                var bestDistance = Float.MAX_VALUE
                var bestLabel: String? = null

                for ((label, embeddingList) in embeddingsMap) {
                    val storedEmbedding = embeddingList.map { it.toFloat() }.toFloatArray()
                    val distance = calculateEuclideanDistance(faceEmbedding, storedEmbedding)

                    Log.d("FaceAuth", "Membandingkan dengan '$label', distance: $distance")

                    if (distance < 0.75 && distance < bestDistance) {
                        matched = true
                        bestDistance = distance
                        bestLabel = label
                    }
                }

                if (matched) {
                    Log.d("FaceAuth", "Wajah cocok dengan label '$bestLabel', distance: $bestDistance")
                    Toast.makeText(context, "Wajah dikenali, UI berhasil dibuka", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch {
                        sessionManager.saveSession(true, currentEmail)
                        Log.d("FaceAuth", "Session disimpan")
                    }
                    navController?.navigate(Screen.Home.route)
                } else {
                    Log.w("FaceAuth", "Tidak ada wajah yang cocok ditemukan")
                    Toast.makeText(context, "Wajah tidak dikenali!", Toast.LENGTH_SHORT).show()
                }

            } else {
                Log.w("FaceAuth", "faceEmbeddings tidak bisa dikonversi jadi Map, tipe: ${rawEmbeddings?.javaClass?.name}")
                Toast.makeText(context, "Data wajah tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Log.e("FaceAuth", "Gagal mengambil data Firestore: ${it.message}", it)
            Toast.makeText(context, "Gagal memuat data wajah.", Toast.LENGTH_SHORT).show()
        }
        .addOnCompleteListener { onComplete() }
}

fun calculateEuclideanDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
    var sum = 0.0
    for (i in embedding1.indices) {
        val diff = embedding1[i] - embedding2[i]
        sum += diff * diff
    }
    return sqrt(sum.toFloat())
}