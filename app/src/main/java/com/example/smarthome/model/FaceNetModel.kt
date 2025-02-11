package com.example.smarthome.model

import android.content.Context
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
    fun getFaceEmbedding(face: FloatArray): FloatArray {
        val inputBuffer = ByteBuffer.allocateDirect(160 * 160 * 3 * 4) // 160x160x3 (RGB) * 4 bytes per float
        inputBuffer.order(ByteOrder.nativeOrder())

        for (value in face) {
            inputBuffer.putFloat(value)
        }

        val outputArray = Array(1) { FloatArray(512) } // Output embedding 128 dimensi
        interpreter.run(inputBuffer, outputArray)

        return outputArray[0]
    }
}
