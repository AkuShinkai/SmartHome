package com.example.smarthome

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FaceDetectionScreen()
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceDetectionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner

    // State untuk menyimpan bounding box wajah
    val boundingBoxes = remember { mutableStateOf(listOf<Rect>()) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Buat referensi PreviewView
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    // Fungsi untuk memulai kamera
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Setup Preview dan hubungkan ke PreviewView
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Setup Image Analysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val faceDetector = FaceDetection.getClient()

            imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                    faceDetector.process(image)
                        .addOnSuccessListener { faces ->
                            boundingBoxes.value = faces.map { face ->
                                face.boundingBox
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(context))
    }

    // Layout CameraX Preview dan Overlay Bounding Box
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(previewView, modifier = Modifier.fillMaxSize())
        BoundingBoxOverlay(boundingBoxes.value)
    }
}

@Composable
fun CameraPreview(previewView: androidx.camera.view.PreviewView, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
fun BoundingBoxOverlay(faces: List<Rect>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / 480f // Lebar default gambar kamera
        val scaleY = size.height / 640f // Tinggi default gambar kamera

        faces.forEach { box ->
            val mirroredLeft = size.width - (box.right * scaleX) // Membalik koordinat horizontal
            val mirroredRight = size.width - (box.left * scaleX)
            val top = box.top * scaleY
            val bottom = box.bottom * scaleY

            drawRect(
                color = Color.Red,
                topLeft = androidx.compose.ui.geometry.Offset(mirroredLeft, top),
                size = androidx.compose.ui.geometry.Size(mirroredRight - mirroredLeft, bottom - top),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}
