package com.drivesense.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.drivesense.app.UiKit.card
import com.drivesense.app.UiKit.dp
import com.drivesense.app.UiKit.primaryButton
import com.drivesense.app.UiKit.secondaryButton
import com.drivesense.app.UiKit.section
import com.drivesense.app.UiKit.title

class MainActivity : AppCompatActivity() {
    private lateinit var summary: TextView
    private lateinit var scoreRing: ScoreRingView
    private val monitorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getIntExtra(DriveMonitorActivity.EXTRA_SCORE, -1)?.takeIf { it >= 0 }?.let { Toast.makeText(this, "Drive saved · Safety score: $it", Toast.LENGTH_LONG).show() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(28), dp(20), dp(28)) }
        val hero = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(24), dp(22), dp(22)) }.card(0xFFE1F0FF.toInt())
        hero.addView(title("DriveSense"))
        hero.addView(TextView(this).apply { text = "Drive with awareness, not distraction."; textSize = 17f; setPadding(0, dp(6), 0, dp(14)) })
        hero.addView(TextView(this).apply { text = "PRIVATE BY DESIGN  •  ON-DEVICE ONLY"; textSize = 12f; letterSpacing = .10f; setPadding(0, dp(8), 0, 0) })
        content.addView(hero, LinearLayout.LayoutParams(-1, -2))
        content.addView(section("Your driving snapshot"), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24); bottomMargin = dp(8) })
        val scoreCard = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(14), dp(18), dp(14)) }.card()
        scoreRing = ScoreRingView(this); scoreCard.addView(scoreRing, LinearLayout.LayoutParams(dp(112), dp(112)))
        summary = TextView(this).apply { textSize = 16f; setPadding(dp(14), 0, 0, 0) }; scoreCard.addView(summary, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(scoreCard, LinearLayout.LayoutParams(-1, -2))
        content.addView(primaryButton("START LIVE MONITOR").apply { setOnClickListener { monitorLauncher.launch(Intent(this@MainActivity, DriveMonitorActivity::class.java).putExtra(DriveMonitorActivity.EXTRA_FROM, "Dashboard")) } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24) })
        content.addView(secondaryButton("VIEW DRIVE HISTORY").apply { setOnClickListener { startActivity(Intent(this@MainActivity, HistoryActivity::class.java)) } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        content.addView(TextView(this).apply { text = "Powered by linear acceleration + gyroscope\nFor awareness and coursework demonstration only."; textSize = 13f; setPadding(dp(4), dp(22), dp(4), 0) })
        setContentView(ScrollView(this).apply { addView(content) })
    }
    override fun onResume() { super.onResume(); updateDashboardSummary() }
    private fun updateDashboardSummary() {
        val sessions = SessionStore(this).all()
        summary.text = if (sessions.isEmpty()) { scoreRing.setScore(100); "READY FOR YOUR FIRST DRIVE\nUse Safe Demo Mode to preview the complete experience." } else {
            val average = sessions.map { it.safetyScore }.average().toInt(); val latest = sessions.first()
            scoreRing.setScore(latest.safetyScore); "${sessions.size} SAVED DRIVE${if (sessions.size == 1) "" else "S"}\nAverage safety score  $average / 100\nLatest drive  ${latest.scoreLabel} · ${latest.safetyScore} / 100"
        }
    }
}
