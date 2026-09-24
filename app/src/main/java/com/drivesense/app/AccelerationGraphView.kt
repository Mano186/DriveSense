package com.drivesense.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/** A small CustomView that draws recent linear-acceleration readings. */
class AccelerationGraphView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val readings = ArrayDeque<Float>()
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(24, 119, 209); strokeWidth = 5f; style = Paint.Style.STROKE }
    private val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(210, 225, 240); strokeWidth = 2f }
    fun addReading(value: Float) { if (readings.size == 60) readings.removeFirst(); readings.addLast(value.coerceIn(-8f, 8f)); invalidate() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mid = height / 2f
        canvas.drawColor(Color.rgb(250, 252, 255)); canvas.drawLine(0f, mid, width.toFloat(), mid, grid)
        if (readings.size < 2) return
        val path = Path(); readings.forEachIndexed { i, value ->
            val x = i * width.toFloat() / 59f; val y = mid - value / 8f * (height * .42f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }; canvas.drawPath(path, line)
    }
}
