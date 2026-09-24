package com.drivesense.app

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.Date

class HistoryActivity : AppCompatActivity() {
    companion object { const val EXTRA_NEW_SCORE = "new_score" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val sessions = SessionStore(this).all()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad); gravity = Gravity.TOP }
        root.addView(TextView(this).apply { text = "Drive History"; textSize = 28f; setTextColor(getColor(R.color.navy)) })
        val newScore = intent.getIntExtra(EXTRA_NEW_SCORE, -1)
        root.addView(TextView(this).apply { text = if (newScore >= 0) "Latest drive safety score: $newScore / 100" else "Saved locally on this device"; textSize = 17f; setPadding(0, 10, 0, 14) })
        val list = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@HistoryActivity); adapter = SessionAdapter(sessions) }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
    }
}

class SessionAdapter(private val sessions: List<DriveSession>) : RecyclerView.Adapter<SessionAdapter.Holder>() {
    class Holder(val view: TextView) : RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): Holder {
        val p = (16 * parent.resources.displayMetrics.density).toInt()
        return Holder(TextView(parent.context).apply { textSize = 16f; setPadding(p, p, p, p); setBackgroundColor(0xFFF1F7FE.toInt()) })
    }
    override fun getItemCount() = sessions.size.coerceAtLeast(1)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (sessions.isEmpty()) { holder.view.text = "No drives saved yet. Start a monitored drive from the dashboard."; return }
        val s = sessions[position]; val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(s.startedAt))
        val counts = s.events.groupingBy { it.type }.eachCount().entries.joinToString { "${it.value} ${it.key.lowercase()}" }.ifEmpty { "No detected events" }
        holder.view.text = "$date\nSafety score: ${s.safetyScore} / 100 • ${s.durationSeconds}s\n$counts"
        val lp = holder.view.layoutParams as? android.view.ViewGroup.MarginLayoutParams
        if (lp != null) lp.bottomMargin = 12
    }
}
