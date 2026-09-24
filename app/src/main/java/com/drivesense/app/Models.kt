package com.drivesense.app

data class DriveEvent(val type: String, val detail: String, val time: Long = System.currentTimeMillis())

data class DriveSession(
    val startedAt: Long,
    val durationSeconds: Int,
    val events: List<DriveEvent>
) {
    val safetyScore: Int get() = (100 - events.map {
        when (it.type) { "Hard brake", "Hard acceleration" -> 8; "Sharp turn" -> 6; else -> 0 }
    }.sum()).coerceAtLeast(0)
}
