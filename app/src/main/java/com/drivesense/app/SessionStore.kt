package com.drivesense.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local-only storage. No identity, route, audio, or location is collected. */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("drivesense_sessions", Context.MODE_PRIVATE)

    fun save(session: DriveSession) {
        val existing = JSONArray(prefs.getString("sessions", "[]"))
        val sessions = JSONArray()
        val events = JSONArray()
        session.events.forEach { events.put(JSONObject().put("type", it.type).put("detail", it.detail).put("time", it.time)) }
        // JSONArray.put(index, value) replaces an existing value at that index;
        // build a new list so the latest session is inserted before older sessions.
        sessions.put(JSONObject().put("startedAt", session.startedAt).put("duration", session.durationSeconds).put("events", events))
        for (i in 0 until existing.length()) sessions.put(existing.getJSONObject(i))
        while (sessions.length() > 20) sessions.remove(sessions.length() - 1)
        prefs.edit().putString("sessions", sessions.toString()).apply()
    }

    fun all(): List<DriveSession> = runCatching {
        val array = JSONArray(prefs.getString("sessions", "[]"))
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val eventArray = item.getJSONArray("events")
            DriveSession(item.getLong("startedAt"), item.getInt("duration"), (0 until eventArray.length()).map { j ->
                val event = eventArray.getJSONObject(j)
                DriveEvent(event.getString("type"), event.getString("detail"), event.getLong("time"))
            })
        }
    }.getOrDefault(emptyList())
}
