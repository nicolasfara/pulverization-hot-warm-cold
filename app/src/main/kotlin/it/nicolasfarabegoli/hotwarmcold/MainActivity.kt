package it.nicolasfarabegoli.hotwarmcold

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var pulverizationManager: AndroidPulverizationManager
    private lateinit var btHandler: BluetoothHandler
    private val rssiTextView by lazy {
        findViewById<TextView>(R.id.rssiLabel)
    }
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val isBluetoothEnabled: Boolean
        get() {
            val btAdapter = bluetoothManager.adapter ?: return false
            return btAdapter.isEnabled
        }
    private val enableBluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> startLogic()
                else -> askToEnableBluetooth()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothManager.adapter != null) {
            if (!isBluetoothEnabled) {
                askToEnableBluetooth()
            }
            startLogic()
        } else {
            Log.e(this::class.simpleName, "This device has not bluetooth hardware")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::btHandler.isInitialized) {
            lifecycleScope.launch { btHandler.stop() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startLogic() {
        checkPermissions {
            btHandler = BluetoothHandler.getInstance(applicationContext).apply { start() }
            lifecycleScope.launch(Dispatchers.Main) {
                btHandler.rssiFlow().collect {
                    rssiTextView.text = "RSSI: $it"
                }
            }
            pulverizationManager =
                AndroidPulverizationManager(lifecycle, lifecycleScope, btHandler.rssiFlow())
            lifecycle.addObserver(pulverizationManager)
            pulverizationManager.runPlatform()
            lifecycleScope.launch(Dispatchers.Main) {
                pulverizationManager.neighboursRssi.collect {
                    // TODO(Update UI with new RSSI neighbour's values)
                }
            }
        }
    }

    private fun askToEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothRequest.launch(enableBtIntent)
    }
}
