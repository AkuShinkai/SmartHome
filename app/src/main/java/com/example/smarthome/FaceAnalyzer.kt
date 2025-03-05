package com.example.smarthome

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class FaceAnalyzer(
    private val context: Context, // Untuk penyimpanan gambar
    private val imageView: ImageView?, // Untuk menampilkan gambar jika diperlukan
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
                Log.d("FaceAnalyzer", "Rotasi gambar: $rotationDegrees derajat")

                // Konversi ImageProxy ke Bitmap dan rotasi sesuai derajat kamera
                val bitmap = imageProxy.toBitmapOptimized()
                    ?.rotateBitmap(rotationDegrees)
                    ?.toGrayscale()

                if (bitmap == null) {
                    Log.e("FaceAnalyzer", "Bitmap gagal dikonversi dari ImageProxy")
                    imageProxy.close()
                    return
                }

                // Simpan gambar asli setelah konversi untuk debugging
                saveBitmapToStorage(bitmap, "debug_face")

                // Tampilkan gambar di ImageView jika ada
                imageView?.setImageBitmap(bitmap)

                // Deteksi wajah
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        Log.d("FaceAnalyzer", "Jumlah wajah terdeteksi: ${faces.size}")
                        faces.forEachIndexed { index, face ->
                            Log.d("FaceAnalyzer", "Wajah ke-$index: BoundingBox = ${face.boundingBox}")

                            // Crop wajah dari gambar
                            val croppedFace = cropFaceFromBitmap(bitmap, face)
                            saveBitmapToStorage(croppedFace, "face_cropped_$index") // Simpan hasil crop
                        }
                        onFacesDetected(faces, bitmap)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceAnalyzer", "Gagal mendeteksi wajah: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                Log.e("FaceAnalyzer", "MediaImage null")
                imageProxy.close()
            }
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Error dalam analyze: ${e.message}")
            imageProxy.close()
        }
    }

    // 🔹 Konversi ImageProxy ke Bitmap dengan Optimasi
    private fun ImageProxy.toBitmapOptimized(): Bitmap? {
        return try {
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
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 75, out)

            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Gagal mengubah ImageProxy ke Bitmap: ${e.message}")
            null
        }
    }

    // 🔹 Fungsi untuk merotasi bitmap
    private fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    // 🔹 Fungsi untuk mengubah bitmap menjadi grayscale
    private fun Bitmap.toGrayscale(): Bitmap {
        return copy(Bitmap.Config.ARGB_8888, true).apply {
            val canvas = Canvas(this)
            val paint = Paint()
            val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(this, 0f, 0f, paint)
        }
    }

    // 🔹 Fungsi untuk crop wajah dari bitmap berdasarkan bounding box
    private fun cropFaceFromBitmap(bitmap: Bitmap, face: Face): Bitmap {
        val boundingBox = face.boundingBox
        val x = boundingBox.left.coerceAtLeast(0)
        val y = boundingBox.top.coerceAtLeast(0)
        val width = boundingBox.width().coerceAtMost(bitmap.width - x)
        val height = boundingBox.height().coerceAtMost(bitmap.height - y)

        return Bitmap.createBitmap(bitmap, x, y, width, height)
    }

    // 🔹 Fungsi untuk menyimpan bitmap ke penyimpanan
    private fun saveBitmapToStorage(bitmap: Bitmap, name: String) {
        try {
            val file = File(context.getExternalFilesDir(null), "$name.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            Log.d("FaceAnalyzer", "Bitmap disimpan di: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Gagal menyimpan bitmap: ${e.message}")
        }
    }
}
