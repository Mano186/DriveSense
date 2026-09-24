package com.drivesense.app

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.abs
import kotlin.math.sqrt

class DriveMonitorActivity : AppCompatActivity(), SensorEventListener {
    companion object { const val EXTRA_FROM = "from"; const val EXTRA_SCORE = "score" }
    private lateinit var manager: SensorManager
    private var acceleration: Sensor? = null; private var gyro: Sensor? = null
    private lateinit var status: TextView; private lateinit var readings: TextView; private lateinit var eventCount: TextView; private lateinit var graph: AccelerationGraphView
    private val events = mutableListOf<DriveEvent>(); private var startedAt = 0L; private var running = false; private var lastEventAt = 0L
    private var latestAcceleration = 0f; private var latestRotation = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceleration = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        root.addView(TextView(this).apply { text = "Live Drive Monitor"; textSize = 28f; setTextColor(getColor(R.color.navy)) })
        status = TextView(this).apply { text = sensorMessage(); textSize = 16f; setPadding(0, 12, 0, 12) }; root.addView(status)
        readings = TextView(this).apply { text = "Linear acceleration: --\nRotation: --"; textSize = 17f }; root.addView(readings)
        graph = AccelerationGraphView(this); root.addView(graph, LinearLayout.LayoutParams(-1, (180 * resources.displayMetrics.density).toInt()).apply { topMargin = 18 })
        eventCount = TextView(this).apply { text = "Events this drive: 0"; textSize = 17f; setPadding(0, 18, 0, 10) }; root.addView(eventCount)
        val toggle = Switch(this).apply { text = "Driving mode"; textSize = 17f; setOnCheckedChangeListener { _, on -> if (on) startDrive() else stopDrive() } }; root.addView(toggle)
        root.addView(MaterialButton(this).apply { text = "FINISH & SAVE DRIVE"; setOnClickListener { if (running) stopDrive(); finishDrive() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 16 })
        root.gravity = Gravity.TOP; setContentView(root)
    }
    private fun sensorMessage() = if (acceleration == null || gyro == null) "This device is missing a required sensor." else "Ready. Turn on Driving mode when the phone is secured."
    private fun startDrive() { if (acceleration == null || gyro == null) return; running = true; startedAt = SystemClock.elapsedRealtime(); events.clear(); eventCount.text = "Events this drive: 0"; status.text = "Monitoring live sensor data…"; manager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_GAME); manager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME) }
    private fun stopDrive() { running = false; manager.unregisterListener(this); status.text = "Monitoring paused" }
    override fun onSensorChanged(event: SensorEvent) {
        if (!running) return
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val value = event.values[1]; val magnitude = sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2])
            latestAcceleration = magnitude; graph.addReading(value); updateReadings()
            if (value < -3.2f) addEvent("Hard brake", "Strong deceleration detected") else if (value > 3.2f) addEvent("Hard acceleration", "Strong acceleration detected")
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            latestRotation = abs(event.values[2]); updateReadings()
            if (latestRotation > 2.2f) addEvent("Sharp turn", "Fast phone rotation detected")
        }
    }
    private fun updateReadings() { readings.text = "Linear acceleration: %.2f m/s²\nRotation: %.2f rad/s".format(latestAcceleration, latestRotation) }
    private fun addEvent(type: String, detail: String) { val now = SystemClock.elapsedRealtime(); if (now - lastEventAt < 1500) return; lastEventAt = now; events += DriveEvent(type, detail); eventCount.text = "Events this drive: ${events.size} • Latest: $type" }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    private fun finishDrive() {
        val duration = if (startedAt == 0L) 0 else ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt()
        val session = DriveSession(System.currentTimeMillis(), duration, events.toList()); SessionStore(this).save(session)
        setResult(RESULT_OK, Intent().putExtra(EXTRA_SCORE, session.safetyScore))
        startActivity(Intent(this, HistoryActivity::class.java).putExtra(HistoryActivity.EXTRA_NEW_SCORE, session.safetyScore)); finish()
    }
    override fun onDestroy() { manager.unregisterListener(this); super.onDestroy() }
}
