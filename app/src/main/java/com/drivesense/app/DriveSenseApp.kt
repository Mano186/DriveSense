package com.drivesense.app

import android.app.Application
import com.google.android.material.color.DynamicColors

/** Applies the user's Android 12+ wallpaper palette when the device supports it. */
class DriveSenseApp : Application() {
    override fun onCreate() { super.onCreate(); DynamicColors.applyToActivitiesIfAvailable(this) }
}
