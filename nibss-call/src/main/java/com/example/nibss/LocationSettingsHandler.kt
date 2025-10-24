package com.interswitchng.smartpos.nibss

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.nibss.nibss_pos_tracker_compact.core.listeners.LocationDisabledReason
import com.nibss.nibss_pos_tracker_compact.core.listeners.LocationSettingsListener

class LocationSettingsHandler(private val context: Context) : LocationSettingsListener {
    override fun onLocationDisabled(reason: LocationDisabledReason) {
        Log.w("LocationSettings", "Location disabled: $reason")

        val message = when (reason) {
            LocationDisabledReason.GPS_DISABLED ->
                "GPS is disabled. Please enable GPS for accurate location tracking."

            LocationDisabledReason.NETWORK_DISABLED ->
                "Network location is disabled. Please enable network location."

            LocationDisabledReason.ALL_PROVIDERS_DISABLED ->
                "Location services are disabled. Please enable location services to continue using the POS system."

            LocationDisabledReason.PERMISSION_DENIED ->
                "Location permission denied. Please grant location permission."
        }
        showLocationSettingsDialog(message)
    }

    override fun onLocationEnabled() {
        Log.i("LocationSettings", "Location services enabled")
        Toast.makeText(context, "Location services enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationPermissionDenied() {
        Log.w("LocationSettings", "Location permission denied")
        showLocationSettingsDialog("Location permission is required for POS compliance.")

        val intent = Intent("com.interswitchng.interswitchpossdkdemo.REQUEST_LOCATION_PERMISSION")
        context.sendBroadcast(intent)
    }

    private fun showLocationSettingsDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Location Required")
            .setMessage(message)
            .setPositiveButton("Settings") { _, _ ->
                openLocationSettings()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}