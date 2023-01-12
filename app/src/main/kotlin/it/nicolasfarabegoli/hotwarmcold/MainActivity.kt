package it.nicolasfarabegoli.hotwarmcold

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import quevedo.soares.leandro.blemadeeasy.BLE
import quevedo.soares.leandro.blemadeeasy.exceptions.PermissionsDeniedException

class MainActivity : AppCompatActivity() {
    private lateinit var pulverizationManager: AndroidPulverizationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ble = BLE(this)

        lifecycleScope.launch {
            try {
                val granted = ble.verifyPermissions(
                    rationaleRequestCallback = { next ->
                        showToast("We need the bluetooth permissions!")
                        next()
                    }
                )
                // Shows UI feedback if the permissions were denied
                if (!granted) {
                    showToast("Permissions denied!")
                    return@launch
                }

                val isBluetoothActive = ble.verifyBluetoothAdapterState()
                // Shows UI feedback if the adapter is turned off
                if (!isBluetoothActive) {
                    showToast("Bluetooth adapter off!")
                    return@launch
                }

                // Checks the location services state
                val isLocationActive = ble.verifyLocationState()
                // Shows UI feedback if location services are turned off
                if (!isLocationActive) {
                    showToast("Location services off!")
                    return@launch
                }

                ble.scanAsync(
                    onUpdate = {
                        Log.i("BT", "New can: ${it.map { e -> e.name to e.rsii }}")
                    },
                    onError = {
                        Log.e("BT", "Error: $it")
                    },
                    duration = 0,
                )

                pulverizationManager =
                    AndroidPulverizationManager(this@MainActivity, lifecycle, lifecycleScope)
                lifecycle.addObserver(pulverizationManager)

                // After the user fill the text box with DEVICE_ID and press the start button, then start the platform
                // pulverizationManager.runPlatform()

            } catch (_: PermissionsDeniedException) {
                showToast("Permissions were denied!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
