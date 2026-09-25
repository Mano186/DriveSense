package com.drivesense.app

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton

object UiKit {
    fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    fun <T : View> T.card(color: Int = 0xFFF0F6FF.toInt()): T = apply {
        background = GradientDrawable().apply { setColor(color); cornerRadius = context.dp(24).toFloat() }
    }
    fun Context.title(text: String) = TextView(this).apply { this.text = text; textSize = 32f; setTextColor(getColor(R.color.navy)); letterSpacing = -.02f }
    fun Context.section(text: String) = TextView(this).apply { this.text = text; textSize = 19f; setTextColor(getColor(R.color.navy)); letterSpacing = .01f }
    fun Context.primaryButton(text: String) = MaterialButton(this).apply { this.text = text; textSize = 15f; minHeight = dp(56); cornerRadius = dp(18) }
    fun Context.secondaryButton(text: String) = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply { this.text = text; textSize = 15f; minHeight = dp(54); cornerRadius = dp(18) }
}
