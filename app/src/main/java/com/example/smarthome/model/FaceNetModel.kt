package com.example.smarthome.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceNetModel(context: Context) {
    private val interpreter: Interpreter

    init {
        val modelFile = loadModelFile(context, "facenet.tflite")
        interpreter = Interpreter(modelFile)
    }

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

    fun getFaceEmbedding(bitmap: Bitmap, boundingBox: Rect): FloatArray {
        val x = boundingBox.left.coerceIn(0, bitmap.width)
        val y = boundingBox.top.coerceIn(0, bitmap.height)
        val width = boundingBox.width().coerceAtMost(bitmap.width - x)
        val height = boundingBox.height().coerceAtMost(bitmap.height - y)

        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Bounding box tidak valid! width: $width, height: $height")
        }

        val croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height)

        // 🔹 Konversi ke grayscale dengan metode yang lebih efisien
        val grayscaleFace = croppedFace.toGrayscale()

        // 🔹 Resize dengan opsi filtering untuk efisiensi memori
        val resizedFace = Bitmap.createScaledBitmap(grayscaleFace, 160, 160, false)

        // 🔹 Pastikan membebaskan bitmap yang tidak dipakai lagi
        croppedFace.recycle()
        grayscaleFace.recycle()

        val inputBuffer = ByteBuffer.allocateDirect(160 * 160 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val pixel = resizedFace.getPixel(x, y)
                val gray = (pixel shr 16 and 0xFF) / 255f

                inputBuffer.putFloat(gray)
                inputBuffer.putFloat(gray)
                inputBuffer.putFloat(gray)
            }
        }

        // 🔹 Bebaskan memori setelah digunakan
        resizedFace.recycle()

        val outputArray = Array(1) { FloatArray(512) }
        interpreter.run(inputBuffer, outputArray)

        return outputArray[0]
    }

    // 🔹 Fungsi untuk konversi grayscale tanpa duplikasi memori besar
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
