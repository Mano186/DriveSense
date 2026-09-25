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
    val scoreLabel: String get() = when (safetyScore) { in 90..100 -> "Excellent"; in 75..89 -> "Good"; in 55..74 -> "Needs attention"; else -> "High risk" }
    val coachingTip: String get() = when {
        events.isEmpty() -> "Smooth drive recorded. Keep maintaining steady inputs."
        events.count { it.type == "Hard brake" } >= 2 -> "Leave more following distance to reduce sudden braking."
        events.count { it.type == "Sharp turn" } >= 2 -> "Slow down before turns and keep the phone securely mounted."
        else -> "Use gentle acceleration and braking for a smoother drive."
    }
}
