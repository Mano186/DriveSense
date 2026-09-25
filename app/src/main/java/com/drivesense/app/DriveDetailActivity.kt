package com.drivesense.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.drivesense.app.UiKit.card
import com.drivesense.app.UiKit.dp
import com.drivesense.app.UiKit.section
import com.drivesense.app.UiKit.title
import java.text.DateFormat
import java.util.Date

/** Shows one locally saved drive. The session index is passed by Intent from HistoryActivity. */
class DriveDetailActivity : AppCompatActivity() {
    companion object { const val EXTRA_SESSION_INDEX = "session_index" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(28), dp(20), dp(28)) }
        val index = intent.getIntExtra(EXTRA_SESSION_INDEX, -1); val session = SessionStore(this).all().getOrNull(index)
        content.addView(title("Drive details"))
        if (session == null) content.addView(TextView(this).apply { text = "This saved drive is no longer available."; textSize = 17f; setPadding(0, dp(20), 0, 0) })
        else {
            val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.startedAt))
            val scoreColor = if (session.safetyScore >= 80) 0xFFE6F6EE.toInt() else 0xFFFFEEE7.toInt()
            val scoreCard = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(14), dp(18), dp(14)) }.card(scoreColor)
            scoreCard.addView(ScoreRingView(this).apply { setScore(session.safetyScore, false) }, LinearLayout.LayoutParams(dp(110), dp(110)))
            scoreCard.addView(TextView(this).apply { text = "${session.scoreLabel.uppercase()}\n${session.safetyScore} / 100  ·  ${session.durationSeconds} sec\n$date"; textSize = 17f; setPadding(dp(14), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
            content.addView(scoreCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
            content.addView(section("Coaching"), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24); bottomMargin = dp(8) })
            content.addView(TextView(this).apply { text = session.coachingTip; textSize = 17f; setPadding(dp(18), dp(18), dp(18), dp(18)) }.card(), LinearLayout.LayoutParams(-1, -2))
            content.addView(section("Detected events"), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24); bottomMargin = dp(8) })
            val details = session.events.joinToString("\n\n") { "${it.type.uppercase()}\n${it.detail}" }.ifEmpty { "No events detected during this drive. Nice work." }
            content.addView(TextView(this).apply { text = details; textSize = 16f; setPadding(dp(18), dp(18), dp(18), dp(18)) }.card(), LinearLayout.LayoutParams(-1, -2))
        }
        setContentView(ScrollView(this).apply { addView(content) })
    }
}
