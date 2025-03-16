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
import com.example.smarthome.security.AESUtil
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

    var buttonText by remember { mutableStateOf("Login dengan Wajah") }
    var isButtonEnabled by remember { mutableStateOf(true) }

    val sessionManager = remember { SessionManager(context) }

    val minFaceSize = 250 * 250
    val maxFaceSize = 400 * 400

    val onFacesDetected: (List<Face>, Bitmap) -> Unit = { faces, bitmap ->
        latestFaceBitmap?.recycle() // Pastikan membebaskan bitmap lama

        if (faces.isNotEmpty()) {
            val largestFace = faces.maxByOrNull { face -> face.boundingBox.width() * face.boundingBox.height() }

            if (largestFace != null) {
                val faceArea = largestFace.boundingBox.width() * largestFace.boundingBox.height()

                if (faceArea in minFaceSize..maxFaceSize) {
                    faceDetected = true
                    latestFaceBitmap =
                        bitmap.config?.let { bitmap.copy(it, true) } // Simpan copy untuk diproses
                    bitmap.recycle() // Bebaskan yang asli
                    latestBoundingBox = largestFace.boundingBox
                    buttonText = "Login dengan Wajah"
                    Log.d("FaceRecognition", "Wajah valid dengan bounding box: ${largestFace.boundingBox}")
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(20.dp),
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
                    if (faceDetected && latestFaceBitmap != null && isButtonEnabled) {
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
                                coroutineScope // Kirim coroutineScope ke fungsi
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
                enabled = isButtonEnabled && buttonText == "Login dengan Wajah" && !isProcessing
            ) {
                Text(buttonText)
            }
        }
    }
}

fun authenticateWithFace(
    faceEmbedding: FloatArray,
    context: Context,
    navController: NavController?,
    sessionManager: SessionManager,
    coroutineScope: CoroutineScope, // Tambahkan parameter ini
    onComplete: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    Log.d("FaceAuth", "Memulai autentikasi wajah...")
    Log.d("FaceAuth", "Embedding wajah hasil scan: ${faceEmbedding.joinToString()}")

    db.collection("users").get()
        .addOnSuccessListener { documents ->
            var bestMatchEmail: String? = null
            var bestMatchPassword: String? = null
            var minDistance = Float.MAX_VALUE

            for (document in documents) {
                val storedEmbedding = (document.get("faceEmbedding") as? List<Double>)?.map { it.toFloat() }
                val email = document.getString("email")
                val encryptedPassword = document.getString("encryptedPassword")

                if (storedEmbedding != null && email != null && encryptedPassword != null) {
                    Log.d("FaceAuth", "Embedding wajah dari Firestore untuk $email: ${storedEmbedding.joinToString()}")
                    val distance = calculateEuclideanDistance(faceEmbedding, storedEmbedding.toFloatArray())
                    Log.d("FaceAuth", "Comparing face with user $email, distance: $distance")
                    if (distance < minDistance) {
                        minDistance = distance
                        bestMatchEmail = email
                        bestMatchPassword = encryptedPassword
                    }
                }
                onComplete()
            }

            if (bestMatchEmail != null && minDistance < 0.65) {
                Log.d("FaceAuth", "Wajah cocok dengan pengguna $bestMatchEmail, distance: $minDistance")
                try {
                    val decryptedPassword = AESUtil.decrypt(bestMatchPassword!!)
                    Log.d("FaceAuth", "Password berhasil didekripsi.")
                    auth.signInWithEmailAndPassword(bestMatchEmail, decryptedPassword)
                        .addOnSuccessListener {
                            Log.d("FaceAuth", "Login berhasil untuk $bestMatchEmail")
                            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                            coroutineScope.launch {
                                sessionManager.saveSession(true, bestMatchEmail)
                            }

                            navController?.navigate(Screen.Home.route)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Login Gagal!", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saat mendekripsi password!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Wajah tidak dikenali!", Toast.LENGTH_SHORT).show()
            }
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