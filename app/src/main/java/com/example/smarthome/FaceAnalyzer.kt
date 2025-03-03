package com.example.smarthome

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream

class FaceAnalyzer(
    private val onFacesDetected: (List<Face>, Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                // Konversi ImageProxy ke Bitmap (Optimized)
                val bitmap = imageProxy.toBitmapOptimized()?.toGrayscale()

                if (bitmap == null) {
                    imageProxy.close()
                    return
                }

                detector.process(image)
                    .addOnSuccessListener { faces ->
                        onFacesDetected(faces, bitmap)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceAnalyzer", "Gagal mendeteksi wajah: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close() // Pastikan selalu ditutup
                    }
            } else {
                imageProxy.close()
            }
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Error dalam analyze: ${e.message}")
            imageProxy.close()
        }
    }

    // 🔹 Fungsi untuk mengonversi ImageProxy ke Bitmap dengan Optimasi
    private fun ImageProxy.toBitmapOptimized(): Bitmap? {
        try {
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()

            // Kurangi kualitas ke 75% untuk menghemat memori
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 75, out)

            val imageBytes = out.toByteArray()

            // Gunakan inSampleSize untuk mengurangi ukuran bitmap (agar lebih ringan)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2  // Kurangi ukuran gambar (1/4 dari ukuran asli)
            }

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Gagal mengubah ImageProxy ke Bitmap: ${e.message}")
            return null
        }
    }

    // 🔹 Fungsi untuk mengubah Bitmap menjadi Grayscale TANPA membuat salinan besar
    private fun Bitmap.toGrayscale(): Bitmap {
        return copy(Bitmap.Config.ARGB_8888, true).apply {
            val canvas = Canvas(this)
            val paint = Paint()
            val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(this, 0f, 0f, paint)
        }
    }
}
