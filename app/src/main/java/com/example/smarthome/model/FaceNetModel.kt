package com.example.smarthome.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceNetModel(context: Context) {
    private val interpreter: Interpreter

    init {
        // Load model dari assets atau dari storage
        val modelFile = loadModelFile(context, "facenet.tflite")
        interpreter = Interpreter(modelFile)
    }

    // Fungsi untuk memuat model dari assets
    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = assetFileDescriptor.createInputStream()
        val byteArray = inputStream.readBytes()
        return ByteBuffer.allocateDirect(byteArray.size).apply {
            order(ByteOrder.nativeOrder())
            put(byteArray)
            rewind()
        }
    }

    // Fungsi untuk mendapatkan embedding wajah
    fun getFaceEmbedding(bitmap: Bitmap, boundingBox: Rect): FloatArray {
        // Validasi bounding box agar tidak keluar dari batas bitmap
        val x = boundingBox.left.coerceIn(0, bitmap.width)
        val y = boundingBox.top.coerceIn(0, bitmap.height)
        val width = boundingBox.width().coerceAtMost(bitmap.width - x)
        val height = boundingBox.height().coerceAtMost(bitmap.height - y)

        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Bounding box tidak valid! width: $width, height: $height")
        }

        val croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height)

        // Resize gambar ke 160x160 sebelum dikirim ke model
        val resizedFace = Bitmap.createScaledBitmap(croppedFace, 160, 160, true)

        val inputBuffer = ByteBuffer.allocateDirect(160 * 160 * 3 * 4) // 160x160x3 (RGB) * 4 bytes per float
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val pixel = resizedFace.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        val outputArray = Array(1) { FloatArray(512) }
        interpreter.run(inputBuffer, outputArray)

        return outputArray[0]
    }
}
