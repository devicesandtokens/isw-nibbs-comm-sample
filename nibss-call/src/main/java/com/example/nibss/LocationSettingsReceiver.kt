package com.interswitchng.smartpos.nibss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class LocationSettingsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.nibss.nibss_pos_tracker_compact.LOCATION_DISABLED" -> {
                val reason = intent.getStringExtra("disabled_reason")
                Log.w("LocationReceiver", "Location disabled: $reason")

                Toast.makeText(
                    context, "Location services disabled. Please enable location.",
                    Toast.LENGTH_LONG
                ).show()
            }

            "com.nibss.nibss_pos_tracker_compact.LOCATION_ENABLED" -> {
                Log.i("LocationReceiver", "Location services enabled")
                Toast.makeText(context, "Location services enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}