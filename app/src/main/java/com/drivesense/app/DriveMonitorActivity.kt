package com.drivesense.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.abs
import kotlin.math.sqrt

class DriveMonitorActivity : AppCompatActivity(), SensorEventListener {
    companion object { const val EXTRA_FROM = "from"; const val EXTRA_SCORE = "score" }
    private lateinit var manager: SensorManager
    private var acceleration: Sensor? = null; private var gyro: Sensor? = null
    private lateinit var status: TextView; private lateinit var readings: TextView; private lateinit var summary: TextView; private lateinit var timer: TextView; private lateinit var graph: AccelerationGraphView; private lateinit var liveButton: MaterialButton
    private val events = mutableListOf<DriveEvent>(); private val handler = Handler(Looper.getMainLooper())
    private var startedAt = 0L; private var running = false; private var lastEventAt = 0L; private var demoMode = false
    private var latestAcceleration = 0f; private var latestRotation = 0f; private var accelerationThreshold = 3.2f; private var turnThreshold = 2.2f
    private val ticker = object : Runnable { override fun run() { if (running) { updateSummary(); handler.postDelayed(this, 1000) } } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceleration = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(18)) }
        root.addView(TextView(this).apply { text = "Live drive monitor"; textSize = 28f; setTextColor(getColor(R.color.navy)); letterSpacing = -.02f })
        status = cardText(sensorMessage(), 16f); root.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        timer = TextView(this).apply { text = "Drive time: 00:00"; textSize = 20f; setPadding(0, dp(15), 0, dp(4)) }; root.addView(timer)
        summary = TextView(this).apply { text = "Safety score: 100 / 100 • Events: 0"; textSize = 17f; setTextColor(getColor(R.color.navy)) }; root.addView(summary)
        readings = TextView(this).apply { text = "Linear acceleration: --\nRotation: --"; textSize = 16f; setPadding(0, dp(12), 0, 0) }; root.addView(readings)
        graph = AccelerationGraphView(this); root.addView(graph, LinearLayout.LayoutParams(-1, dp(150)).apply { topMargin = dp(12) })
        addThresholdControl(root, "Acceleration/braking sensitivity", 17) { accelerationThreshold = 1.5f + it / 10f }
        addThresholdControl(root, "Turning sensitivity", 11) { turnThreshold = 1.1f + it / 10f }
        root.addView(TextView(this).apply { text = "LIVE SENSOR CONTROL"; textSize = 13f; letterSpacing = .08f; setPadding(0, dp(16), 0, dp(6)) })
        liveButton = MaterialButton(this).apply { textSize = 16f; minHeight = dp(68); cornerRadius = dp(20); setOnClickListener { if (running && !demoMode) stopDrive() else { if (running) stopDrive(); startDrive(false) } } }
        root.addView(liveButton, LinearLayout.LayoutParams(-1, -2))
        updateLiveButton()
        root.addView(MaterialButton(this).apply { text = "RUN SAFE DEMO EVENTS"; setOnClickListener { if (!running) startDrive(true); runDemo() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(MaterialButton(this).apply { text = "FINISH & SAVE DRIVE"; setOnClickListener { if (running) stopDrive(); finishDrive() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(TextView(this).apply { text = "Demo Mode uses sample values only; it does not use device sensors."; textSize = 13f; setPadding(0, dp(8), 0, 0) })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
    private fun addThresholdControl(root: LinearLayout, title: String, initial: Int, apply: (Int) -> Unit) {
        val label = TextView(this).apply { textSize = 14f; setPadding(0, dp(12), 0, 0) }
        val bar = SeekBar(this).apply { max = 35; progress = initial; setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { apply(p); label.text = "$title: %.1f".format(if (title.startsWith("Acceleration")) accelerationThreshold else turnThreshold) }; override fun onStartTrackingTouch(s: SeekBar?) = Unit; override fun onStopTrackingTouch(s: SeekBar?) = Unit }) }
        root.addView(label); root.addView(bar); apply(initial); label.text = "$title: %.1f".format(if (title.startsWith("Acceleration")) accelerationThreshold else turnThreshold)
    }
    private fun sensorMessage() = if (acceleration == null || gyro == null) "A required sensor is unavailable. You can still use Safe Demo Mode." else "Ready. Secure the phone, then turn on Driving mode."
    private fun startDrive(demo: Boolean) { if (!demo && (acceleration == null || gyro == null)) { status.text = "A required sensor is unavailable. Use Safe Demo Mode."; return }; running = true; demoMode = demo; startedAt = SystemClock.elapsedRealtime(); events.clear(); lastEventAt = 0; updateSummary(); status.text = if (demo) "Demo Mode is running with sample driving events." else "Monitoring live sensor data…"; status.setBackgroundColor(if (demo) 0xFFFFF3CD.toInt() else getColor(R.color.pale_blue)); if (!demo) { manager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_GAME); manager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME) }; updateLiveButton(); handler.removeCallbacks(ticker); handler.post(ticker) }
    private fun stopDrive() { running = false; manager.unregisterListener(this); handler.removeCallbacks(ticker); status.text = "Monitoring paused"; status.setBackgroundColor(getColor(R.color.pale_blue)); updateLiveButton() }
    private fun updateLiveButton() { if (!::liveButton.isInitialized) return; if (running && !demoMode) { liveButton.text = "●  LIVE SENSORS ACTIVE  ·  TAP TO STOP"; liveButton.setBackgroundColor(0xFF167D5B.toInt()) } else { liveButton.text = "START LIVE SENSOR MONITOR"; liveButton.setBackgroundColor(getColor(R.color.blue)) } }
    private fun runDemo() { if (!demoMode) return; listOf("Hard acceleration" to "Sample strong acceleration", "Sharp turn" to "Sample fast rotation", "Hard brake" to "Sample strong deceleration").forEachIndexed { index, item -> handler.postDelayed({ if (running && demoMode) { latestAcceleration = if (index == 2) 4.1f else 3.8f; latestRotation = if (index == 1) 2.8f else .3f; graph.addReading(if (index == 2) -4.1f else 3.8f); updateReadings(); addEvent(item.first, item.second, true) } }, index * 1800L) } }
    override fun onSensorChanged(event: SensorEvent) { if (!running || demoMode) return; if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) { val value = event.values[1]; latestAcceleration = sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]); graph.addReading(value); updateReadings(); if (value < -accelerationThreshold) addEvent("Hard brake", "Strong deceleration detected") else if (value > accelerationThreshold) addEvent("Hard acceleration", "Strong acceleration detected") } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) { latestRotation = abs(event.values[2]); updateReadings(); if (latestRotation > turnThreshold) addEvent("Sharp turn", "Fast phone rotation detected") } }
    private fun updateReadings() { readings.text = "Linear acceleration: %.2f m/s²\nRotation: %.2f rad/s".format(latestAcceleration, latestRotation) }
    private fun addEvent(type: String, detail: String, bypassCooldown: Boolean = false) { val now = SystemClock.elapsedRealtime(); if (!bypassCooldown && now - lastEventAt < 1500) return; lastEventAt = now; events += DriveEvent(type, detail); updateSummary() }
    private fun updateSummary() { val elapsed = if (startedAt == 0L) 0 else ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt(); val session = DriveSession(System.currentTimeMillis(), elapsed, events); timer.text = "Drive time: %02d:%02d".format(elapsed / 60, elapsed % 60); summary.text = "Safety score: ${session.safetyScore} / 100 • Events: ${events.size}"; summary.setTextColor(if (session.safetyScore >= 80) Color.rgb(20, 110, 75) else Color.rgb(185, 72, 0)) }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    private fun finishDrive() { val duration = if (startedAt == 0L) 0 else ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt(); val session = DriveSession(System.currentTimeMillis(), duration, events.toList()); SessionStore(this).save(session); setResult(RESULT_OK, Intent().putExtra(EXTRA_SCORE, session.safetyScore)); startActivity(Intent(this, HistoryActivity::class.java).putExtra(HistoryActivity.EXTRA_NEW_SCORE, session.safetyScore)); finish() }
    private fun cardText(text: String, size: Float) = TextView(this).apply { this.text = text; textSize = size; setPadding(dp(14), dp(12), dp(14), dp(12)); setBackgroundColor(getColor(R.color.pale_blue)) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); manager.unregisterListener(this); super.onDestroy() }
}
