package com.example.smarthome

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceDetectionOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private var boundingBoxes: List<Rect> = listOf()

    // Update bounding boxes
    fun setBoundingBoxes(faces: List<Rect>) {
        boundingBoxes = faces
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (box in boundingBoxes) {
            canvas.drawRect(box, paint) // Draw each bounding box
        }
    }
}