package com.example.smarthome.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.widget.ImageView
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceNetModel(private val context: Context) {
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

    fun getFaceEmbedding(bitmap: Bitmap, boundingBox: Rect, imageView: ImageView? = null): FloatArray {
        val x = boundingBox.left.coerceIn(0, bitmap.width)
        val y = boundingBox.top.coerceIn(0, bitmap.height)
        val width = boundingBox.width().coerceAtMost(bitmap.width - x)
        val height = boundingBox.height().coerceAtMost(bitmap.height - y)

        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Bounding box tidak valid! width: $width, height: $height")
        }

        val croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height)
        debugSaveImage(context, croppedFace, "cropped.png")

        val grayscaleFace = croppedFace.toGrayscale()
        debugSaveImage(context, grayscaleFace, "grayscale.png")

        val resizedFace = Bitmap.createScaledBitmap(grayscaleFace, 160, 160, false)
        debugSaveImage(context, resizedFace, "resized.png")

        croppedFace.recycle()
        grayscaleFace.recycle()

        imageView?.setImageBitmap(resizedFace)

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

        resizedFace.recycle()

        val outputArray = Array(1) { FloatArray(512) }
        interpreter.run(inputBuffer, outputArray)

        return outputArray[0]
    }

    private fun Bitmap.toGrayscale(): Bitmap {
        return copy(Bitmap.Config.ARGB_8888, true).apply {
            val canvas = Canvas(this)
            val paint = Paint()
            val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(this, 0f, 0f, paint)
        }
    }

    private fun debugSaveImage(context: Context, bitmap: Bitmap, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("Saved image: ${file.absolutePath}")
    }
}
