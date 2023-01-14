package it.nicolasfarabegoli.hotwarmcold

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val RUNTIME_PERMISSION_REQUEST_CODE = 2

internal fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permissionType
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasRequiredRuntimePermissions(): Boolean {
    return hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
        hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}

internal fun Activity.requestBluetoothPermissions() {
    runOnUiThread {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            title = "Bluetooth permissions required"
            setMessage(
                "Starting from Android 12, the system requires apps to be granted " +
                    "Bluetooth access in order to scan for and connect to BLE devices."
            )
            setCancelable(false)
            setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this@requestBluetoothPermissions,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
        }
        builder.show()
    }
}

internal fun Activity.requestLocationPermission() {
    runOnUiThread {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            title = "Location permission required"
            setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                    "location access in order to scan for BLE devices."
            )
            setCancelable(false)
            setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this@requestLocationPermission,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
        }
        builder.show()
    }
}
