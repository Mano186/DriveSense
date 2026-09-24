package com.drivesense.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private val monitorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getIntExtra(DriveMonitorActivity.EXTRA_SCORE, -1)?.takeIf { it >= 0 }?.let {
            Toast.makeText(this, "Drive saved. Safety score: $it", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "DriveSense"
        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad); gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(TextView(this).apply { text = "DriveSense"; textSize = 32f; setTextColor(getColor(R.color.navy)) })
        root.addView(TextView(this).apply { text = "A private, sensor-based driving awareness app"; textSize = 17f; setPadding(0, 8, 0, 28) })
        val privacy = TextView(this).apply { text = "Privacy first: no GPS, camera, microphone, account, or cloud. Your sessions stay on this device."; textSize = 15f; setPadding(pad, pad, pad, pad); setBackgroundColor(getColor(R.color.pale_blue)) }
        root.addView(privacy, LinearLayout.LayoutParams(-1, -2))
        root.addView(Space(this), LinearLayout.LayoutParams(1, pad))
        root.addView(MaterialButton(this).apply { text = "START DRIVE MONITOR"; setOnClickListener { monitorLauncher.launch(Intent(this@MainActivity, DriveMonitorActivity::class.java).putExtra(DriveMonitorActivity.EXTRA_FROM, "Dashboard")) } }, LinearLayout.LayoutParams(-1, -2))
        root.addView(MaterialButton(this).apply { text = "VIEW DRIVE HISTORY"; setOnClickListener { startActivity(Intent(this@MainActivity, HistoryActivity::class.java)) } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 12 })
        root.addView(TextView(this).apply { text = "Uses linear acceleration for acceleration/braking and gyroscope rotation for turns."; textSize = 14f; setPadding(0, 30, 0, 0) })
        setContentView(root)
    }
}
