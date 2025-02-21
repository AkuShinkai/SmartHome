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
import com.example.smarthome.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.face.Face
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
    var isButtonEnabled by remember { mutableStateOf(true) } // Untuk delay button

    val minFaceSize = 250 * 250  // 30 cm (wajah lebih kecil)
    val maxFaceSize = 400 * 400  // 20 cm (wajah lebih besar)

    val onFacesDetected: (List<Face>, Bitmap) -> Unit = { faces, bitmap ->
        if (faces.isNotEmpty()) {
            val largestFace = faces.maxByOrNull { face ->
                face.boundingBox.width() * face.boundingBox.height()
            }

            if (largestFace != null) {
                val faceArea = largestFace.boundingBox.width() * largestFace.boundingBox.height()

                if (faceArea in minFaceSize..maxFaceSize) {
                    faceDetected = true
                    latestFaceBitmap = bitmap
                    latestBoundingBox = largestFace.boundingBox
                    buttonText = "Login dengan Wajah"
                    Log.d("FaceRecognition", "Wajah valid dengan bounding box: ${largestFace.boundingBox}")
                } else {
                    faceDetected = false
                    latestFaceBitmap = null
                    latestBoundingBox = null

                    buttonText = when {
                        faceArea < minFaceSize -> "Jarak terlalu jauh"
                        faceArea > maxFaceSize -> "Jarak terlalu dekat"
                        else -> "Jarak wajah tidak sesuai"
                    }

                    Log.w("FaceRecognition", buttonText)
                }
            }
        } else {
            faceDetected = false
            latestFaceBitmap = null
            latestBoundingBox = null
            buttonText = "Tidak ada wajah terdeteksi"
            Log.d("FaceRecognition", "Tidak ada wajah terdeteksi")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Face Authentication", fontSize = 18.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.matchParentSize(),
                    onFacesDetected = onFacesDetected
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (faceDetected && latestFaceBitmap != null && isButtonEnabled) {
                        isProcessing = true
                        isButtonEnabled = false // Matikan button sementara
                        buttonText = "Memproses..."

                        try {
                            faceEmbedding = faceNetModel.getFaceEmbedding(latestFaceBitmap!!, latestBoundingBox!!)
                            authenticateWithFace(faceEmbedding!!, context, navController) {
                                isProcessing = false
                                buttonText = "Login dengan Wajah"

                                // Delay sebelum button bisa ditekan lagi
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(1000) // 2 detik
                                    isButtonEnabled = true // Aktifkan button kembali
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FaceLogin", "Gagal mendapatkan embedding wajah: ${e.message}", e)
                            buttonText = "Terjadi kesalahan"
                            isProcessing = false
                            isButtonEnabled = true // Pastikan button bisa ditekan kembali
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
    onComplete: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Log.d("FaceAuth", "Memulai autentikasi wajah...")

    // Log embedding hasil scan
    Log.d("FaceAuth", "Embedding wajah hasil scan: ${faceEmbedding.joinToString()}")

    db.collection("users").get()
        .addOnSuccessListener { documents ->
            Log.d("FaceAuth", "Berhasil mengambil data pengguna dari Firestore.")

            var bestMatchEmail: String? = null
            var bestMatchPassword: String? = null
            var minDistance = Float.MAX_VALUE // Inisialisasi dengan nilai terbesar

            for (document in documents) {
                val storedEmbedding = (document.get("faceEmbedding") as? List<Double>)?.map { it.toFloat() }
                val email = document.getString("email")
                val encryptedPassword = document.getString("encryptedPassword")

                if (storedEmbedding != null && email != null && encryptedPassword != null) {
                    // Log embedding dari Firestore
                    Log.d("FaceAuth", "Embedding wajah dari Firestore untuk $email: ${storedEmbedding.joinToString()}")

                    val distance = calculateEuclideanDistance(faceEmbedding, storedEmbedding.toFloatArray())

                    Log.d("FaceAuth", "Comparing face with user $email, distance: $distance")

                    // Simpan kandidat terbaik dengan jarak terkecil
                    if (distance < minDistance) {
                        minDistance = distance
                        bestMatchEmail = email
                        bestMatchPassword = encryptedPassword
                    }
                }
            }

            if (bestMatchEmail != null && minDistance < 0.60) {
                Log.d("FaceAuth", "Wajah cocok dengan pengguna $bestMatchEmail, distance: $minDistance")

                try {
                    val decryptedPassword = AESUtil.decrypt(bestMatchPassword!!) // Dekripsi password
                    Log.d("FaceAuth", "Password berhasil didekripsi.")

                    auth.signInWithEmailAndPassword(bestMatchEmail, decryptedPassword)
                        .addOnSuccessListener {
                            Log.d("FaceAuth", "Login berhasil untuk $bestMatchEmail")
                            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                            navController?.navigate(Screen.Home.route)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FaceAuth", "Login gagal untuk $bestMatchEmail: ${e.message}", e)
                            Toast.makeText(context, "Login Gagal!", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Log.e("FaceAuth", "Gagal mendekripsi password: ${e.message}", e)
                    Toast.makeText(context, "Terjadi kesalahan saat mendekripsi password!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w("FaceAuth", "Wajah tidak dikenali di database.")
                Toast.makeText(context, "Wajah tidak dikenali!", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Log.e("FaceAuth", "Error saat mengambil data Firestore: ${e.message}", e)
            Toast.makeText(context, "Error saat login!", Toast.LENGTH_SHORT).show()
        }
        .addOnCompleteListener { onComplete() }
}

fun calculateEuclideanDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
    var sum = 0.0
    for (i in embedding1.indices) {
        val diff = embedding1[i] - embedding2[i]
        sum += diff * diff
    }
    return sqrt(sum.toFloat()) // Sama seperti np.linalg.norm() di Python
}

