package com.drivesense.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/** Animated custom score indicator used on the dashboard and drive details. */
class ScoreRingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 14f; strokeCap = Paint.Cap.ROUND; color = 0xFFDCE8F7.toInt() }
    private val progress = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 14f; strokeCap = Paint.Cap.ROUND }
    private val number = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD; color = 0xFF102A43.toInt() }
    private val caption = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; color = 0xFF52677D.toInt() }
    private var shownScore = 0f; private var targetScore = 100
    fun setScore(score: Int, animate: Boolean = true) {
        targetScore = score.coerceIn(0, 100); progress.color = if (targetScore >= 80) 0xFF167D5B.toInt() else if (targetScore >= 55) 0xFFF59E0B.toInt() else 0xFFD64949.toInt()
        if (!animate) { shownScore = targetScore.toFloat(); invalidate(); return }
        ValueAnimator.ofFloat(shownScore, targetScore.toFloat()).apply { duration = 650; addUpdateListener { shownScore = it.animatedValue as Float; invalidate() }; start() }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas); val size = minOf(width, height).toFloat(); val inset = track.strokeWidth / 2 + 3; val bounds = RectF(inset, inset, size - inset, size - inset)
        canvas.drawArc(bounds, -90f, 360f, false, track); canvas.drawArc(bounds, -90f, shownScore * 3.6f, false, progress)
        number.textSize = size * .30f; caption.textSize = size * .105f; val center = size / 2
        canvas.drawText(shownScore.toInt().toString(), center, center + size * .06f, number); canvas.drawText("SAFETY", center, center + size * .23f, caption)
    }
}
