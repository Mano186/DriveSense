package com.drivesense.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.Date
import com.drivesense.app.UiKit.card
import com.drivesense.app.UiKit.dp
import com.drivesense.app.UiKit.title

class HistoryActivity : AppCompatActivity() {
    companion object { const val EXTRA_NEW_SCORE = "new_score" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = dp(20)
        val sessions = SessionStore(this).all()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad); gravity = Gravity.TOP }
        root.addView(title("Drive history"))
        val newScore = intent.getIntExtra(EXTRA_NEW_SCORE, -1)
        root.addView(TextView(this).apply { text = if (newScore >= 0) "Latest score  $newScore / 100" else "Private, on-device drive records"; textSize = 16f; setPadding(0, 8, 0, 14) })
        val list = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@HistoryActivity); adapter = SessionAdapter(sessions) { index -> startActivity(Intent(this@HistoryActivity, DriveDetailActivity::class.java).putExtra(DriveDetailActivity.EXTRA_SESSION_INDEX, index)) } }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
    }
}

class SessionAdapter(private val sessions: List<DriveSession>, private val onSessionClick: (Int) -> Unit) : RecyclerView.Adapter<SessionAdapter.Holder>() {
    class Holder(val view: TextView) : RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): Holder {
        val p = (16 * parent.resources.displayMetrics.density).toInt()
        return Holder(TextView(parent.context).apply { textSize = 16f; setPadding(p, p, p, p) }.card())
    }
    override fun getItemCount() = sessions.size.coerceAtLeast(1)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (sessions.isEmpty()) { holder.view.text = "No drives saved yet. Start a monitored drive from the dashboard."; return }
        val s = sessions[position]; val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(s.startedAt))
        val eventDetails = s.events.joinToString(separator = "\n") { "• ${it.type}: ${it.detail}" }.ifEmpty { "• No detected events — smooth drive." }
        holder.view.text = "$date\n${s.scoreLabel} • Safety score: ${s.safetyScore} / 100 • ${s.durationSeconds}s\n$eventDetails\n\nTap for drive coaching"
        holder.view.setTextColor(if (s.safetyScore >= 80) 0xFF146E4B.toInt() else 0xFFB94800.toInt())
        holder.view.setOnClickListener { onSessionClick(position) }
        holder.view.setPadding(holder.view.paddingLeft, holder.view.paddingTop, holder.view.paddingRight, holder.view.paddingBottom + 4)
    }
}
