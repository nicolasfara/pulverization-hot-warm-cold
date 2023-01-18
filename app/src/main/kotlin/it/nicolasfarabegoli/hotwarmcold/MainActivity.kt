package it.nicolasfarabegoli.hotwarmcold

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val startButton: Button by lazy { findViewById(R.id.startButton) }
    private val deviceIdText: EditText by lazy { findViewById(R.id.deviceIdText) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.neighbourRssi) }
    private val listAdapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.isEnabled = false
        deviceIdText.doOnTextChanged { text, _, _, _ ->
            startButton.isEnabled = text.toString().trim().isNotEmpty()
        }

        startButton.setOnClickListener {
            Log.i("StartPlatform", "Start the pulverization platform")
            startLogic()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = listAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothManager.adapter != null) {
            if (!isBluetoothEnabled) {
                askToEnableBluetooth()
            }
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
                AndroidPulverizationManager(
                    lifecycle,
                    lifecycleScope,
                    deviceIdText.text.toString(),
                    btHandler.rssiFlow()
                )
            lifecycle.addObserver(pulverizationManager)
            pulverizationManager.runPlatform()
            lifecycleScope.launch {
                pulverizationManager.neighboursRssi.collect {
                    Log.i("Act", "Neighbours: $it")
                    listAdapter.onUpdateItems(it)
                }
            }
        }
    }

    private fun askToEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothRequest.launch(enableBtIntent)
    }
}
