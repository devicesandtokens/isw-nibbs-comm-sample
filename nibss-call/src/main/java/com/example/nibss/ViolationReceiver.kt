package com.interswitchng.smartpos.nibss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ViolationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.pos.tracker.ACTION_MESSAGE" -> {
                val violationMessage = intent.getStringExtra("message")
                Log.d("ViolationReceiver", "Message received: $violationMessage")

                handleViolation(context, violationMessage)
            }
        }
    }

    private fun handleViolation(context: Context?, message: String?) {
        message?.let { msg ->
            when {
                msg.contains("Re-assignment") -> {
                    // Handle device reassignment
                    showDeviceReassignmentDialog(context)
                    notifyAdministrator("Device reassignment detected")
                }

                msg.contains("Out of Bounds") -> {
                    // Handle location violation
//                    showLocationViolationAlert()
//                    logLocationViolation()
                }

                msg.contains("integrity") -> {
                    // Handle app tampering
//                    showSecurityAlert()
//                    disableNonEssentialFeatures()
                }

                else -> {
                    // Generic violation handling
                    showGenericViolationDialog(context, msg)
                }
            }
        }
    }

    private fun showDeviceReassignmentDialog(context: Context?) {
        AlertDialog.Builder(context ?: return)
            .setTitle("Device Assignment Error")
            .setMessage("This device is not authorized for your merchant account. Please contact support.")
            .setPositiveButton("Contact Support") { _, _ ->
                // Open support contact
            }
            .setCancelable(false)
            .show()
    }

    private fun showGenericViolationDialog(context: Context?, message: String) {
        AlertDialog.Builder(context ?: return)
            .setTitle("Violation Detected")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun notifyAdministrator(violation: String) {
        // Send notification to your backend
        // BackendService.notifyAdmin(violation, deviceId, timestamp)
    }
}